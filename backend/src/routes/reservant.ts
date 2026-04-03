// Role : Gerer les routes liees aux reservants et contacts.
import { Router } from 'express'
import pool from '../db/database.js'
import { requireRole } from '../middleware/require-role.js'

const router = Router();
const RESERVANT_DELETE_ROLES = ['admin', 'super-organizer'];

type ErrorBody = { error: string; details?: string[] }
type DatabaseErrorResponse = { status: number; body: ErrorBody }

const buildErrorBody = (error: string, details?: string | string[]): ErrorBody => {
    const normalizedDetails = (Array.isArray(details) ? details : details ? [details] : [])
        .map((item) => item?.trim())
        .filter((item): item is string => Boolean(item));

    return normalizedDetails.length > 0
        ? { error, details: normalizedDetails }
        : { error };
};

const parseEntityId = (value: string) => {
    const parsedValue = Number(value);
    return Number.isInteger(parsedValue) && parsedValue > 0 ? parsedValue : null;
};

async function reservantExists(reservantId: number) {
    const { rowCount } = await pool.query(
        'SELECT 1 FROM reservant WHERE id = $1',
        [reservantId],
    );

    return rowCount !== null && rowCount > 0;
}

// Role : Normaliser les erreurs Postgres vers des reponses HTTP propres.
// Preconditions : err est une erreur issue d'une requete pg.
// Postconditions : Retourne une reponse HTTP ou null si non geree.
function mapReservantDbError(err: any): DatabaseErrorResponse | null {
    const code = err?.code as string | undefined;
    const constraint = err?.constraint as string | undefined;
    const detail = err?.detail as string | undefined;

    if (code === '23505') {
        if (constraint === 'reservant_email_key') {
            return { status: 409, body: { error: 'Un réservant avec cet email existe déjà' } };
        }
        if (constraint === 'reservant_name_key') {
            return { status: 409, body: { error: 'Un réservant avec ce nom existe déjà' } };
        }
        return { status: 409, body: buildErrorBody('Conflit de duplication', detail) };
    }
    if (code === '23503') {
        if (constraint === 'reservant_editor_id_fkey') {
            return { status: 400, body: { error: "Éditeur inexistant" } };
        }
        return { status: 400, body: buildErrorBody('Référence inexistante', detail) };
    }
    if (code === '23502') {
        return { status: 400, body: buildErrorBody('Champ requis manquant', detail) };
    }
    if (code === '23514') {
        return { status: 400, body: buildErrorBody('Violation de contrainte', detail) };
    }
    if (code === '22P02') {
        return { status: 400, body: buildErrorBody('Format invalide', detail) };
    }

    return null;
}

// Role : Garantir l'existence d'un workflow pour un reservant.
// Preconditions : reservantId est valide, festivalId est optionnel.
// Postconditions : Retourne l'id du workflow existant ou cree.
async function ensureWorkflow(reservantId: number, festivalId?: number) {
    let targetFestivalId = festivalId;
    if (!targetFestivalId) {
        const { rows: festivalRows } = await pool.query('SELECT id FROM festival ORDER BY id ASC LIMIT 1');
        if (festivalRows.length === 0) {
            throw new Error('Aucun festival disponible pour créer un workflow');
        }
        targetFestivalId = festivalRows[0].id;
    }

    const { rows: existing } = await pool.query(
        'SELECT id FROM suivi_workflow WHERE reservant_id = $1 ORDER BY id DESC LIMIT 1',
        [reservantId],
    );

    if (existing.length > 0) {
        return existing[0].id;
    }

    const { rows: created } = await pool.query(
        `INSERT INTO suivi_workflow (reservant_id, festival_id, state)
         VALUES ($1, $2, 'Pas_de_contact')
         RETURNING id`,
        [reservantId, targetFestivalId],
    );

    return created[0].id;
}

// Role : Lister tous les reservants.
// Preconditions : Aucune.
// Postconditions : Retourne la liste des reservants.
router.get('/', async (_req, res) => {
    try {
        const { rows } = await pool.query(
            'SELECT id, name, email, type, editor_id, phone_number, address, siret, notes FROM reservant ORDER BY name ASC'
        );
        res.json(rows);
    } catch (err) {
        console.error('Erreur lors de la récupération des réservants:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Prévisualiser les dependances supprimees lors d'une suppression.
// Preconditions : id est valide.
// Postconditions : Retourne la liste des elements supprimes par cascade.
router.get('/:id/delete-summary', async (req, res) => {
    const { id } = req.params;
    const reservantId = parseEntityId(id);

    if (reservantId == null) {
        return res.status(400).json({ error: 'Identifiant de réservant invalide' });
    }

    try {
        if (!(await reservantExists(reservantId))) {
            return res.status(404).json({ error: 'Réservant non trouvé' });
        }

        const contactsResult = await pool.query(
            `SELECT id, name, email
             FROM contact
             WHERE reservant_id = $1
             ORDER BY priority ASC, name ASC`,
            [reservantId],
        );

        const workflowsResult = await pool.query(
            `SELECT sw.id, sw.festival_id, sw.state, f.name AS festival_name
             FROM suivi_workflow sw
             LEFT JOIN festival f ON f.id = sw.festival_id
             WHERE sw.reservant_id = $1
             ORDER BY sw.id DESC`,
            [reservantId],
        );

        const reservationsResult = await pool.query(
            `SELECT r.id,
                    r.festival_id,
                    r.statut_paiement,
                    f.name AS festival_name,
                    CASE
                      WHEN r.reservant_id = $1 THEN 'reservant'
                      ELSE 'represented_editor'
                    END AS relation
             FROM reservation r
             LEFT JOIN festival f ON f.id = r.festival_id
             WHERE r.reservant_id = $1 OR r.represented_editor_id = $1
             ORDER BY r.id DESC`,
            [reservantId],
        );

        res.json({
            reservant_id: reservantId,
            contacts: contactsResult.rows,
            workflows: workflowsResult.rows,
            reservations: reservationsResult.rows,
        });
    } catch (err) {
        console.error('Erreur lors du chargement du résumé de suppression:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Obtenir le detail d'un reservant.
// Preconditions : id est valide.
// Postconditions : Retourne le reservant ou une erreur.
router.get('/:id', async (req, res) => {
    const { id } = req.params;
    const reservantId = parseEntityId(id);

    if (reservantId == null) {
        return res.status(400).json({ error: 'Identifiant de réservant invalide' });
    }

    try {
        const { rows } = await pool.query(
            'SELECT id, name, email, type, editor_id, phone_number, address, siret, notes FROM reservant WHERE id = $1',
            [reservantId]
        );
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Réservant non trouvé' });
        }
        res.json(rows[0]);
    } catch (err) {
        console.error('Erreur lors de la récupération du réservant:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Creer un reservant.
// Preconditions : name, email, type sont fournis.
// Postconditions : Cree le reservant ou retourne une erreur.
router.post('/', async (req, res) => {
    const { name, email, type, editor_id, phone_number, address, siret, notes } = req.body;

    if (!name || !email || !type) {
        return res.status(400).json({ error: 'Champs obligatoires manquants (name, email, type)' });
    }

    // Vérifier que type est valide
    const validTypes = ['editeur', 'prestataire', 'boutique', 'animateur', 'association'];
    if (!validTypes.includes(type)) {
        return res.status(400).json({ error: 'Type invalide. Valeurs autorisées: editeur, prestataire, boutique, animateur, association' });
    }

    try {
        const { rows: conflictRows } = await pool.query(
            'SELECT name, email FROM reservant WHERE name = $1 OR email = $2',
            [name, email],
        );
        if (conflictRows.length > 0) {
            const nameTaken = conflictRows.some((row) => row.name === name);
            const emailTaken = conflictRows.some((row) => row.email === email);
            if (nameTaken && emailTaken) {
                return res.status(409).json({ error: 'Nom et email déjà utilisés' });
            }
            if (nameTaken) {
                return res.status(409).json({ error: 'Nom déjà utilisé' });
            }
            if (emailTaken) {
                return res.status(409).json({ error: 'Un réservant avec cet email existe déjà' });
            }
        }

        const { rows } = await pool.query(
            `INSERT INTO reservant (name, email, type, editor_id, phone_number, address, siret, notes)
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
             RETURNING id, name, email, type, editor_id, phone_number, address, siret, notes`,
            [name, email, type, editor_id || null, phone_number || null, address || null, siret || null, notes || null]
        );
        res.status(201).json(rows[0]);
    } catch (err: any) {
        const mappedError = mapReservantDbError(err);
        if (mappedError) {
            return res.status(mappedError.status).json(mappedError.body);
        }
        console.error('Erreur lors de la création du réservant:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Mettre a jour un reservant.
// Preconditions : id est valide, payload coherent.
// Postconditions : Met a jour le reservant ou retourne une erreur.
router.put('/:id', async (req, res) => {
    const { id } = req.params;
    const { name, email, type, editor_id, phone_number, address, siret, notes } = req.body;
    const reservantId = parseEntityId(id);

    if (reservantId == null) {
        return res.status(400).json({ error: 'Identifiant de réservant invalide' });
    }

    try {
        const { rowCount: reservantRowCount } = await pool.query(
            'SELECT 1 FROM reservant WHERE id = $1',
            [reservantId],
        );

        if (reservantRowCount === 0) {
            return res.status(404).json({ error: 'Réservant non trouvé' });
        }

        const { rows: conflictRows } = await pool.query(
            'SELECT id, name, email FROM reservant WHERE (name = $1 OR email = $2) AND id <> $3',
            [name, email, reservantId],
        );
        if (conflictRows.length > 0) {
            const nameTaken = conflictRows.some((row) => row.name === name);
            const emailTaken = conflictRows.some((row) => row.email === email);
            if (nameTaken && emailTaken) {
                return res.status(409).json({ error: 'Nom et email déjà utilisés' });
            }
            if (nameTaken) {
                return res.status(409).json({ error: 'Nom déjà utilisé' });
            }
            if (emailTaken) {
                return res.status(409).json({ error: 'Un réservant avec cet email existe déjà' });
            }
        }

        const { rows, rowCount } = await pool.query(
            `UPDATE reservant
             SET name = $1, email = $2, type = $3, editor_id = $4, phone_number = $5, address = $6, siret = $7, notes = $8
             WHERE id = $9
             RETURNING id, name, email, type, editor_id, phone_number, address, siret, notes`,
            [name, email, type, editor_id || null, phone_number || null, address || null, siret || null, notes || null, reservantId]
        );

        res.json(rows[0]);
    } catch (err: any) {
        const mappedError = mapReservantDbError(err);
        if (mappedError) {
            return res.status(mappedError.status).json(mappedError.body);
        }
        console.error('Erreur lors de la mise à jour du réservant:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Supprimer un reservant.
// Preconditions : id est valide.
// Postconditions : Supprime le reservant ou retourne une erreur.
router.delete('/:id', requireRole(RESERVANT_DELETE_ROLES), async (req, res) => {
    const { id } = req.params;
    const reservantId = parseEntityId(id);

    if (reservantId == null) {
        return res.status(400).json({ error: 'Identifiant de réservant invalide' });
    }

    try {
        const { rowCount } = await pool.query(
            'DELETE FROM reservant WHERE id = $1',
            [reservantId]
        );

        if (rowCount === 0) {
            return res.status(404).json({ error: 'Réservant non trouvé' });
        }

        res.json({ message: 'Réservant supprimé avec succès' });
    } catch (err: any) {
        console.error('Erreur lors de la suppression du réservant:', err);
        // Gestion des contraintes de clé étrangère
        if (err.code === '23503') {
            return res.status(409).json({
                error: 'Impossible de supprimer ce réservant car il est référencé par d\'autres entités (contacts, workflows, réservations)'
            });
        }
        res.status(500).json(buildErrorBody('Erreur serveur', err.message));
    }
});

// Role : Mettre a jour l'etat de workflow d'un reservant (R3).
// Preconditions : id est valide, workflowState autorise.
// Postconditions : Met a jour le workflow et retourne le reservant.
router.patch('/:id/workflow', async (req, res) => {
    const { id } = req.params;
    const { workflowState, festivalId } = req.body;

    const validStates = [
        'Pas_de_contact',
        'Contact_pris',
        'Discussion_en_cours',
        'Sera_absent',
        'Considere_absent',
        'Reservation_confirmee',
        'Facture',
        'Facture_payee',
    ];

    if (!validStates.includes(workflowState)) {
        return res.status(400).json({ error: 'Etat de workflow invalide' });
    }

    try {
        const workflowId = await ensureWorkflow(Number(id), festivalId);
        await pool.query(
            'UPDATE suivi_workflow SET state = $1 WHERE id = $2',
            [workflowState, workflowId],
        );

        const { rows } = await pool.query(
            `SELECT r.id, r.name, r.email, r.type, r.editor_id, r.phone_number, r.address, r.siret, r.notes,
                    sw.state AS workflow_state, sw.liste_jeux_demandee, sw.liste_jeux_obtenue, sw.jeux_recus, sw.presentera_jeux
             FROM reservant r
             LEFT JOIN suivi_workflow sw ON sw.reservant_id = r.id
             WHERE r.id = $1
             ORDER BY sw.id DESC
             LIMIT 1`,
            [id],
        );

        res.json(rows[0]);
    } catch (err) {
        console.error('Erreur lors de la mise à jour du workflow:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Mettre a jour les indicateurs de workflow (R3).
// Preconditions : id est valide.
// Postconditions : Met a jour les flags et retourne le reservant.
router.patch('/:id/workflow/flags', async (req, res) => {
    const { id } = req.params;
    const { liste_jeux_demandee, liste_jeux_obtenue, jeux_recus, presentera_jeux, festivalId } = req.body;

    try {
        const workflowId = await ensureWorkflow(Number(id), festivalId);
        await pool.query(
            `UPDATE suivi_workflow
             SET liste_jeux_demandee = COALESCE($1, liste_jeux_demandee),
                 liste_jeux_obtenue = COALESCE($2, liste_jeux_obtenue),
                 jeux_recus = COALESCE($3, jeux_recus),
                 presentera_jeux = COALESCE($4, presentera_jeux)
             WHERE id = $5`,
            [liste_jeux_demandee, liste_jeux_obtenue, jeux_recus, presentera_jeux, workflowId],
        );

        const { rows } = await pool.query(
            `SELECT r.id, r.name, r.email, r.type, r.editor_id, r.phone_number, r.address, r.siret, r.notes,
                    sw.state AS workflow_state, sw.liste_jeux_demandee, sw.liste_jeux_obtenue, sw.jeux_recus, sw.presentera_jeux
             FROM reservant r
             LEFT JOIN suivi_workflow sw ON sw.reservant_id = r.id
             WHERE r.id = $1
             ORDER BY sw.id DESC
             LIMIT 1`,
            [id],
        );

        res.json(rows[0]);
    } catch (err) {
        console.error('Erreur lors de la mise à jour des flags de workflow:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Lister les contacts d'un reservant (R2).
// Preconditions : id est valide.
// Postconditions : Retourne la liste des contacts.
router.get('/:id/contacts', async (req, res) => {
    const { id } = req.params;
    const reservantId = parseEntityId(id);

    if (reservantId == null) {
        return res.status(400).json({ error: 'Identifiant de réservant invalide' });
    }

    try {
        if (!(await reservantExists(reservantId))) {
            return res.status(404).json({ error: 'Réservant non trouvé' });
        }

        const { rows } = await pool.query(
            `SELECT id, name, email, phone_number, job_title, priority
             FROM contact
             WHERE reservant_id = $1
             ORDER BY priority ASC, name ASC`,
            [reservantId],
        );
        res.json(rows);
    } catch (err) {
        console.error('Erreur lors de la récupération des contacts:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Ajouter un contact pour un reservant (R2).
// Preconditions : Champs obligatoires fournis.
// Postconditions : Cree le contact et retourne les donnees.
router.post('/:id/contacts', async (req, res) => {
    const { id } = req.params;
    const { name, email, phone_number, job_title, priority } = req.body;
    const reservantId = parseEntityId(id);

    if (reservantId == null) {
        return res.status(400).json({ error: 'Identifiant de réservant invalide' });
    }

    if (!name || !email || !phone_number || !job_title || priority === undefined) {
        return res.status(400).json({ error: 'Champs obligatoires manquants pour le contact' });
    }

    try {
        if (!(await reservantExists(reservantId))) {
            return res.status(404).json({ error: 'Réservant non trouvé' });
        }

        const { rows } = await pool.query(
            `INSERT INTO contact (name, email, phone_number, job_title, reservant_id, priority)
             VALUES ($1, $2, $3, $4, $5, $6)
             RETURNING id, name, email, phone_number, job_title, priority`,
            [name, email, phone_number, job_title, reservantId, priority],
        );
        res.status(201).json(rows[0]);
    } catch (err) {
        console.error('Erreur lors de la création du contact:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Recuperer la timeline des contacts (R2).
// Preconditions : id est valide.
// Postconditions : Retourne la timeline des contacts.
router.get('/:id/contacts/timeline', async (req, res) => {
    const { id } = req.params;
    try {
        const { rows } = await pool.query(
            `SELECT sc.id,
                    c.id as contact_id,
                    sw.reservant_id,
                    sw.festival_id,
                    c.name as contact_name,
                    c.email as contact_email,
                    c.phone_number as contact_phone_number,
                    c.job_title as contact_job_title,
                    c.priority as contact_priority,
                    sc.date_contact
             FROM suivi_contact sc
             JOIN contact c ON sc.contact_id = c.id
             JOIN suivi_workflow sw ON sc.workflow_id = sw.id
             WHERE sw.reservant_id = $1
             ORDER BY sc.date_contact DESC`,
            [id],
        );
        res.json(rows);
    } catch (err) {
        console.error('Erreur lors de la récupération de la timeline des contacts:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Ajouter un evenement de contact dans la timeline (R2).
// Preconditions : contactId est fourni, id est valide.
// Postconditions : Cree l'evenement et retourne les donnees.
router.post('/:id/contacts/events', async (req, res) => {
    const { id } = req.params;
    const { contactId, dateContact } = req.body;
    const reservantId = parseEntityId(id);
    const parsedContactId = parseEntityId(String(contactId));

    if (reservantId == null) {
        return res.status(400).json({ error: 'Identifiant de réservant invalide' });
    }

    if (!contactId) {
        return res.status(400).json({ error: 'contactId requis' });
    }

    if (parsedContactId == null) {
        return res.status(400).json({ error: 'Identifiant de contact invalide' });
    }

    if (dateContact) {
        const parsedDate = new Date(dateContact);
        if (Number.isNaN(parsedDate.getTime())) {
            return res.status(400).json({ error: 'dateContact invalide' });
        }
    }

    try {
        const { rows: contactRows } = await pool.query(
            'SELECT reservant_id FROM contact WHERE id = $1',
            [parsedContactId],
        );

        if (contactRows.length === 0) {
            return res.status(404).json({ error: 'Contact introuvable' });
        }

        if (contactRows[0].reservant_id !== reservantId) {
            return res.status(409).json({ error: 'Ce contact n’appartient pas à ce réservant' });
        }

        const { rows: workflowRows } = await pool.query(
            'SELECT id, festival_id FROM suivi_workflow WHERE reservant_id = $1 ORDER BY id DESC LIMIT 1',
            [reservantId],
        );

        if (workflowRows.length === 0) {
            return res.status(404).json({ error: 'Workflow introuvable pour enregistrer le contact' });
        }

        const workflowId = workflowRows[0].id;
        const contactDate = dateContact ? new Date(dateContact) : new Date();

        const { rows: inserted } = await pool.query(
            `INSERT INTO suivi_contact (contact_id, workflow_id, date_contact)
             VALUES ($1, $2, $3)
             RETURNING id, contact_id, workflow_id, date_contact`,
            [parsedContactId, workflowId, contactDate],
        );

        const { rows } = await pool.query(
            `SELECT sc.id,
                    c.id as contact_id,
                    sw.reservant_id,
                    sw.festival_id,
                    c.name as contact_name,
                    c.email as contact_email,
                    c.phone_number as contact_phone_number,
                    c.job_title as contact_job_title,
                    c.priority as contact_priority,
                    sc.date_contact
             FROM suivi_contact sc
             JOIN contact c ON sc.contact_id = c.id
             JOIN suivi_workflow sw ON sc.workflow_id = sw.id
             WHERE sc.id = $1`,
            [inserted[0].id],
        );

        res.status(201).json(rows[0]);
    } catch (err) {
        console.error('Erreur lors de l\'ajout d\'un événement de contact:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Supprimer un contact (R2).
// Preconditions : id et contactId sont valides.
// Postconditions : Supprime le contact ou retourne une erreur.
router.delete('/:id/contacts/:contactId', async (req, res) => {
    const { id, contactId } = req.params;
    const reservantId = parseEntityId(id);
    const parsedContactId = parseEntityId(contactId);

    if (reservantId == null) {
        return res.status(400).json({ error: 'Identifiant de réservant invalide' });
    }

    if (parsedContactId == null) {
        return res.status(400).json({ error: 'Identifiant de contact invalide' });
    }

    try {
        const { rows } = await pool.query(
            'SELECT reservant_id FROM contact WHERE id = $1',
            [parsedContactId],
        );

        if (rows.length === 0) {
            return res.status(404).json({ error: 'Contact introuvable' });
        }

        if (rows[0].reservant_id !== reservantId) {
            return res.status(409).json({ error: 'Ce contact n’appartient pas à ce réservant' });
        }

        await pool.query('DELETE FROM contact WHERE id = $1', [parsedContactId]);
        res.json({ message: 'Contact supprimé' });
    } catch (err) {
        console.error('Erreur lors de la suppression du contact:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Supprimer un evenement de contact (timeline) (R2).
// Preconditions : id et eventId sont valides.
// Postconditions : Supprime l'evenement ou retourne une erreur.
router.delete('/:id/contacts/events/:eventId', async (req, res) => {
    const { id, eventId } = req.params;
    const reservantId = parseEntityId(id);
    const parsedEventId = parseEntityId(eventId);

    if (reservantId == null) {
        return res.status(400).json({ error: 'Identifiant de réservant invalide' });
    }

    if (parsedEventId == null) {
        return res.status(400).json({ error: 'Identifiant d’événement invalide' });
    }

    try {
        const { rows } = await pool.query(
            `SELECT sc.id, sw.reservant_id
             FROM suivi_contact sc
             JOIN suivi_workflow sw ON sc.workflow_id = sw.id
             WHERE sc.id = $1`,
            [parsedEventId],
        );

        if (rows.length === 0) {
            return res.status(404).json({ error: 'Événement de contact introuvable' });
        }

        if (rows[0].reservant_id !== reservantId) {
            return res.status(409).json({ error: 'Cet événement n’appartient pas à ce réservant' });
        }

        await pool.query('DELETE FROM suivi_contact WHERE id = $1', [parsedEventId]);
        res.json({ message: 'Evénement supprimé' });
    } catch (err) {
        console.error('Erreur lors de la suppression d\'un événement de contact:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

export default router;
