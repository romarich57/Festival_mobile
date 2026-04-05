// Role : Gérer les routes des zones de plan.
import { Router } from 'express'
import pool from '../db/database.js'

const router = Router();

// Role : Recuperer les allocations simples d'une reservation.
// Preconditions : reservation_id est valide.
// Postconditions : Retourne les allocations ou une erreur.
router.get('/reservation/:reservation_id/allocations', async (req, res) => {
    const reservationId = Number(req.params.reservation_id);
    if (!Number.isFinite(reservationId)) {
        return res.status(400).json({ error: 'Identifiant invalide' });
    }

    try {
        const { rows } = await pool.query(
            `SELECT rzp.id, rzp.reservation_id, rzp.zone_plan_id, rzp.nb_tables, rzp.nb_chaises, rzp.taille_table
             FROM reservation_zone_plan rzp
             WHERE rzp.reservation_id = $1
             ORDER BY rzp.zone_plan_id, rzp.id`,
            [reservationId]
        );
        res.json(rows);
    } catch (err) {
        console.error('Erreur lors de la récupération des allocations simples:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Creer un placement simple (INSERT, pas upsert).
// Preconditions : reservation_id et zone_plan_id sont valides.
// Postconditions : Retourne le placement créé avec son id ou une erreur.
router.post('/reservation/:reservation_id/allocations/:zone_plan_id', async (req, res) => {
    const reservationId = Number(req.params.reservation_id);
    const zonePlanId = Number(req.params.zone_plan_id);
    const nbTables = Number(req.body?.nb_tables ?? 0);
    const nbChaises = Number(req.body?.nb_chaises ?? 0);
    const tailleTable = req.body?.taille_table ?? 'aucun';

    if (!Number.isFinite(reservationId) || !Number.isFinite(zonePlanId)) {
        return res.status(400).json({ error: 'Identifiant invalide' });
    }
    if ((nbTables <= 0 && nbChaises <= 0) || nbTables < 0 || nbChaises < 0) {
        return res.status(400).json({ error: 'nb_tables ou nb_chaises doit être positif' });
    }
    if (!['standard', 'grande', 'mairie', 'aucun'].includes(tailleTable)) {
        return res.status(400).json({ error: 'taille_table invalide' });
    }

    const client = await pool.connect();
    try {
        await client.query('BEGIN');

        // Valider réservation + zone plan + même festival
        const { rows: validationRows } = await client.query(
            `SELECT r.festival_id AS reservation_festival, r.reservant_id,
                    zp.festival_id AS zone_festival, zp.id_zone_tarifaire, zp.nb_tables AS zone_plan_capacity
             FROM reservation r
             JOIN zone_plan zp ON zp.id = $2
             WHERE r.id = $1`,
            [reservationId, zonePlanId]
        );

        if (validationRows.length === 0) {
            await client.query('ROLLBACK');
            return res.status(404).json({ error: 'Réservation ou zone de plan introuvable' });
        }

        if (validationRows[0].reservation_festival !== validationRows[0].zone_festival) {
            await client.query('ROLLBACK');
            return res.status(400).json({
                error: 'La zone de plan ne correspond pas au festival de la réservation'
            });
        }

        const festivalId = validationRows[0].reservation_festival;
        const zoneTarifaireId = validationRows[0].id_zone_tarifaire;
        const zonePlanCapacity = Number(validationRows[0].zone_plan_capacity);

        // Vérifier que le réservant a réservé dans la zone tarifaire liée
        const { rows: rztRows } = await client.query(
            `SELECT nb_tables_reservees FROM reservation_zones_tarifaires
             WHERE reservation_id = $1 AND zone_tarifaire_id = $2`,
            [reservationId, zoneTarifaireId]
        );
        if (rztRows.length === 0) {
            await client.query('ROLLBACK');
            return res.status(400).json({
                error: 'Le réservant n\'a pas de tables réservées dans la zone tarifaire liée à cette zone de plan'
            });
        }

        // Vérifier la capacité de la zone de plan (tables restantes)
        const { rows: allocRows } = await client.query(
            `SELECT
                COALESCE((SELECT SUM(rzp.nb_tables) FROM reservation_zone_plan rzp WHERE rzp.zone_plan_id = $1), 0)
                + COALESCE((SELECT SUM(ja.nb_tables_occupees * ja.nb_exemplaires) FROM jeux_alloues ja WHERE ja.zone_plan_id = $1), 0)
                AS total_allocated`,
            [zonePlanId]
        );
        const totalAllocated = Number(allocRows[0]?.total_allocated || 0);
        const tablesRestantes = zonePlanCapacity - totalAllocated;

        if (nbTables > tablesRestantes) {
            await client.query('ROLLBACK');
            return res.status(400).json({
                error: `Pas assez de tables dans cette zone de plan. Disponibles: ${tablesRestantes}, Demandées: ${nbTables}`
            });
        }

        // Vérifier le stock de tables par type au niveau du festival
        if (tailleTable !== 'aucun') {
            const stockCol = `stock_tables_${tailleTable}`;
            const { rows: festivalStockRows } = await client.query(
                `SELECT ${stockCol} as stock FROM festival WHERE id = $1`,
                [festivalId]
            );
            const totalStock = Number(festivalStockRows[0]?.stock || 0);

            // Compter les tables occupées par ce type : jeux_alloues + reservation_zone_plan
            const { rows: occupiedJeuxRows } = await client.query(
                `SELECT COALESCE(SUM(ja.nb_tables_occupees * ja.nb_exemplaires), 0) as occupied
                 FROM jeux_alloues ja
                 JOIN reservation r ON r.id = ja.reservation_id
                 WHERE r.festival_id = $1 AND ja.zone_plan_id IS NOT NULL AND ja.taille_table_requise = $2`,
                [festivalId, tailleTable]
            );
            const { rows: occupiedSimpleRows } = await client.query(
                `SELECT COALESCE(SUM(rzp.nb_tables), 0) as occupied
                 FROM reservation_zone_plan rzp
                 JOIN zone_plan zp ON zp.id = rzp.zone_plan_id
                 WHERE zp.festival_id = $1 AND rzp.taille_table = $2`,
                [festivalId, tailleTable]
            );
            const totalOccupied = Number(occupiedJeuxRows[0]?.occupied || 0) + Number(occupiedSimpleRows[0]?.occupied || 0);
            if (totalOccupied + nbTables > totalStock) {
                await client.query('ROLLBACK');
                return res.status(400).json({
                    error: `Pas assez de tables ${tailleTable} disponibles au festival. Stock: ${totalStock}, Occupées: ${totalOccupied}`
                });
            }
        }

        // Vérifier les chaises
        const { rows: festivalRows } = await client.query(
            `SELECT stock_chaises FROM festival WHERE id = $1`,
            [festivalId]
        );
        const totalChaises = Number(festivalRows[0]?.stock_chaises || 0);

        const { rows: alloueesRows } = await client.query(
            `SELECT (
                COALESCE((SELECT SUM(rzp.nb_chaises) FROM reservation_zone_plan rzp JOIN zone_plan zp ON rzp.zone_plan_id = zp.id WHERE zp.festival_id = $1), 0)
                + COALESCE((SELECT SUM(ja.nb_chaises) FROM jeux_alloues ja JOIN reservation r ON ja.reservation_id = r.id WHERE r.festival_id = $1 AND ja.zone_plan_id IS NOT NULL), 0)
            ) as total_allouees`,
            [festivalId]
        );

        const totalAllouees = Number(alloueesRows[0]?.total_allouees || 0);
        const maxChaises = Math.max(0, totalChaises - totalAllouees);

        if (nbChaises > maxChaises) {
            await client.query('ROLLBACK');
            return res.status(400).json({
                error: `Pas assez de chaises disponibles. Disponibles: ${maxChaises}, Demandées: ${nbChaises}`
            });
        }

        // INSERT (pas d'upsert) pour permettre les placements multiples
        const { rows } = await client.query(
            `INSERT INTO reservation_zone_plan (reservation_id, zone_plan_id, nb_tables, nb_chaises, taille_table)
             VALUES ($1, $2, $3, $4, $5)
             RETURNING id, reservation_id, zone_plan_id, nb_tables, nb_chaises, taille_table`,
            [reservationId, zonePlanId, nbTables, nbChaises, tailleTable]
        );

        await client.query('COMMIT');
        res.status(201).json(rows[0]);
    } catch (err) {
        await client.query('ROLLBACK');
        console.error('Erreur lors de la création du placement:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    } finally {
        client.release();
    }
});

// Role : Supprimer un placement simple par son ID unique.
// Preconditions : allocation_id est valide.
// Postconditions : Retourne un message de suppression ou une erreur.
router.delete('/allocations/:allocation_id', async (req, res) => {
    const allocationId = Number(req.params.allocation_id);

    if (!Number.isFinite(allocationId)) {
        return res.status(400).json({ error: 'Identifiant invalide' });
    }

    try {
        const { rowCount } = await pool.query(
            `DELETE FROM reservation_zone_plan WHERE id = $1`,
            [allocationId]
        );
        if (rowCount === 0) {
            return res.status(404).json({ error: 'Allocation introuvable' });
        }
        res.json({ message: 'Allocation supprimée avec succès' });
    } catch (err) {
        console.error('Erreur lors de la suppression de l\'allocation:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Recuperer le total des allocations simples par zone pour un festival.
// Preconditions : festival_id est valide.
// Postconditions : Retourne les totaux ou une erreur.
router.get('/festival/:festival_id/allocations-simple', async (req, res) => {
    const festivalId = Number(req.params.festival_id);
    if (!Number.isFinite(festivalId)) {
        return res.status(400).json({ error: 'Identifiant invalide' });
    }

    try {
        const { rows } = await pool.query(
            `SELECT rzp.zone_plan_id, 
                    COALESCE(SUM(rzp.nb_tables), 0) AS nb_tables,
                    COALESCE(SUM(rzp.nb_chaises), 0) AS nb_chaises
             FROM reservation_zone_plan rzp
             JOIN reservation r ON r.id = rzp.reservation_id
             WHERE r.festival_id = $1
             GROUP BY rzp.zone_plan_id
             ORDER BY rzp.zone_plan_id`,
            [festivalId]
        );
        res.json(rows);
    } catch (err) {
        console.error('Erreur lors de la récupération des allocations simples par festival:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Recuperer le total des allocations globales par zone pour un festival.
// Preconditions : festival_id est valide.
// Postconditions : Retourne les totaux globaux ou une erreur.
router.get('/festival/:festival_id/allocations-global', async (req, res) => {
    const festivalId = Number(req.params.festival_id);
    if (!Number.isFinite(festivalId)) {
        return res.status(400).json({ error: 'Identifiant invalide' });
    }

    try {
        const { rows } = await pool.query(
            `SELECT zp.id AS zone_plan_id,
                    (COALESCE(rzp_sum.nb_tables, 0) + COALESCE(ja_sum.nb_tables_jeux, 0)) AS nb_tables,
                    COALESCE(rzp_sum.nb_chaises, 0) AS nb_chaises
             FROM zone_plan zp
             LEFT JOIN (
                SELECT zone_plan_id, SUM(nb_tables) AS nb_tables, SUM(nb_chaises) AS nb_chaises
                FROM reservation_zone_plan
                GROUP BY zone_plan_id
             ) rzp_sum ON rzp_sum.zone_plan_id = zp.id
             LEFT JOIN (
                SELECT zone_plan_id, SUM(nb_tables_occupees) AS nb_tables_jeux
                FROM jeux_alloues
                WHERE zone_plan_id IS NOT NULL
                GROUP BY zone_plan_id
             ) ja_sum ON ja_sum.zone_plan_id = zp.id
             WHERE zp.festival_id = $1
             ORDER BY zp.id`,
            [festivalId]
        );
        res.json(rows);
    } catch (err) {
        console.error('Erreur lors de la récupération des allocations globales par festival:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Recuperer les allocations simples d'une zone de plan.
// Preconditions : zone_plan_id est valide.
// Postconditions : Retourne les allocations ou une erreur.
router.get('/:zone_plan_id/allocations-simples', async (req, res) => {
    const zonePlanId = Number(req.params.zone_plan_id);
    if (!Number.isFinite(zonePlanId)) {
        return res.status(400).json({ error: 'Identifiant invalide' });
    }

    try {
        const { rows } = await pool.query(
            `SELECT rzp.id,
                    rzp.reservation_id,
                    rzp.zone_plan_id,
                    rzp.nb_tables,
                    rzp.nb_chaises,
                    rzp.taille_table,
                    res.name AS reservant_name
             FROM reservation_zone_plan rzp
             JOIN reservation r ON r.id = rzp.reservation_id
             JOIN reservant res ON res.id = r.reservant_id
             WHERE rzp.zone_plan_id = $1
             ORDER BY res.name ASC, rzp.id ASC`,
            [zonePlanId]
        );
        res.json(rows);
    } catch (err) {
        console.error('Erreur lors de la récupération des allocations simples de zone:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Recuperer toutes les zones de plan d'un festival avec tables allouees.
// Preconditions : festival_id est valide.
// Postconditions : Retourne la liste des zones avec nb_tables_allocated ou une erreur.
router.get('/:festival_id', async (req, res) => {
    const { festival_id } = req.params;
    try {
        const { rows } = await pool.query(
            `SELECT zp.id, zp.name, zp.festival_id, zp.id_zone_tarifaire, zp.nb_tables,
                    zt.name as zone_tarifaire_name, zt.price_per_table, zt.m2_price,
                    (
                        COALESCE((SELECT SUM(rzp.nb_tables) FROM reservation_zone_plan rzp WHERE rzp.zone_plan_id = zp.id), 0)
                        + COALESCE((SELECT SUM(ja.nb_tables_occupees * ja.nb_exemplaires) FROM jeux_alloues ja WHERE ja.zone_plan_id = zp.id), 0)
                    )::int AS nb_tables_allocated
             FROM zone_plan zp
             LEFT JOIN zone_tarifaire zt ON zp.id_zone_tarifaire = zt.id
             WHERE zp.festival_id = $1 
             ORDER BY zp.id ASC`, 
            [festival_id]
        );
        res.json(rows);
    } catch (err) {
        console.error('Erreur lors de la récupération des zones de plan:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Recuperer le contexte complet pour l'onglet Plan d'une reservation.
// Preconditions : reservation_id et festival_id sont valides.
// Postconditions : Retourne zones, jeux, stocks, zones tarifaires reservees, placements de tous les réservants.
router.get('/reservation/:reservation_id/context/:festival_id', async (req, res) => {
    const reservationId = Number(req.params.reservation_id);
    const festivalId = Number(req.params.festival_id);
    if (!Number.isFinite(reservationId) || !Number.isFinite(festivalId)) {
        return res.status(400).json({ error: 'Identifiant invalide' });
    }

    try {
        // 1. Zones de plan avec tables allouees
        const { rows: zones } = await pool.query(
            `SELECT zp.id, zp.name, zp.festival_id, zp.id_zone_tarifaire, zp.nb_tables,
                    zt.name as zone_tarifaire_name, zt.price_per_table, zt.m2_price,
                    (
                        COALESCE((SELECT SUM(rzp.nb_tables) FROM reservation_zone_plan rzp WHERE rzp.zone_plan_id = zp.id), 0)
                        + COALESCE((SELECT SUM(ja.nb_tables_occupees * ja.nb_exemplaires) FROM jeux_alloues ja WHERE ja.zone_plan_id = zp.id), 0)
                    )::int AS nb_tables_allocated
             FROM zone_plan zp
             LEFT JOIN zone_tarifaire zt ON zp.id_zone_tarifaire = zt.id
             WHERE zp.festival_id = $1
             ORDER BY zp.id ASC`,
            [festivalId]
        );

        // 2. Jeux alloues du reservant (non encore places dans une zone de plan)
        const { rows: unplacedGames } = await pool.query(
            `SELECT ja.id AS allocation_id, ja.game_id, ja.nb_tables_occupees, ja.nb_exemplaires,
                    ja.nb_chaises, ja.taille_table_requise, ja.zone_plan_id,
                    g.title AS game_title, g.type AS game_type
             FROM jeux_alloues ja
             JOIN games g ON g.id = ja.game_id
             WHERE ja.reservation_id = $1
             ORDER BY g.title ASC`,
            [reservationId]
        );

        // 3. Allocations simples du reservant courant
        const { rows: simpleAllocations } = await pool.query(
            `SELECT id, reservation_id, zone_plan_id, nb_tables, nb_chaises, taille_table
             FROM reservation_zone_plan
             WHERE reservation_id = $1
             ORDER BY zone_plan_id, id`,
            [reservationId]
        );

        // 4. Zones tarifaires reservees par ce reservant
        const { rows: reservedZonesTarifaires } = await pool.query(
            `SELECT rzt.zone_tarifaire_id, rzt.nb_tables_reservees, zt.name as zone_name
             FROM reservation_zones_tarifaires rzt
             JOIN zone_tarifaire zt ON zt.id = rzt.zone_tarifaire_id
             WHERE rzt.reservation_id = $1`,
            [reservationId]
        );

        // 5. Stock tables et chaises du festival
        const { rows: festivalRows } = await pool.query(
            `SELECT stock_tables_standard, stock_tables_grande, stock_tables_mairie, stock_chaises FROM festival WHERE id = $1`,
            [festivalId]
        );
        const festival = festivalRows[0] || {};

        // 6. Tables occupees par type (jeux_alloues + reservation_zone_plan)
        const { rows: occupiedJeuxRows } = await pool.query(
            `SELECT ja.taille_table_requise AS table_type,
                    COALESCE(SUM(ja.nb_tables_occupees * ja.nb_exemplaires), 0)::int AS occupied
             FROM jeux_alloues ja
             JOIN reservation r ON r.id = ja.reservation_id
             WHERE r.festival_id = $1 AND ja.zone_plan_id IS NOT NULL AND ja.taille_table_requise != 'aucun'
             GROUP BY ja.taille_table_requise`,
            [festivalId]
        );
        const { rows: occupiedSimpleRows } = await pool.query(
            `SELECT rzp.taille_table AS table_type,
                    COALESCE(SUM(rzp.nb_tables), 0)::int AS occupied
             FROM reservation_zone_plan rzp
             JOIN zone_plan zp ON zp.id = rzp.zone_plan_id
             WHERE zp.festival_id = $1 AND rzp.taille_table != 'aucun'
             GROUP BY rzp.taille_table`,
            [festivalId]
        );
        const occupiedByType: Record<string, number> = {};
        for (const row of occupiedJeuxRows) {
            occupiedByType[row.table_type] = (occupiedByType[row.table_type] || 0) + Number(row.occupied);
        }
        for (const row of occupiedSimpleRows) {
            occupiedByType[row.table_type] = (occupiedByType[row.table_type] || 0) + Number(row.occupied);
        }

        // 7. Chaises totales allouees
        const { rows: chaisesRows } = await pool.query(
            `SELECT (
                COALESCE((SELECT SUM(rzp.nb_chaises) FROM reservation_zone_plan rzp JOIN zone_plan zp ON rzp.zone_plan_id = zp.id WHERE zp.festival_id = $1), 0)
                + COALESCE((SELECT SUM(ja.nb_chaises) FROM jeux_alloues ja JOIN reservation r ON ja.reservation_id = r.id WHERE r.festival_id = $1 AND ja.zone_plan_id IS NOT NULL), 0)
            )::int as total_chaises_allouees`,
            [festivalId]
        );

        // 8. Tous les placements de toutes les réservations par zone (pour affichage détaillé)
        const { rows: allPlacements } = await pool.query(
            `SELECT rzp.id, rzp.reservation_id, rzp.zone_plan_id, rzp.nb_tables, rzp.nb_chaises, rzp.taille_table,
                    res.name AS reservant_name
             FROM reservation_zone_plan rzp
             JOIN reservation r ON r.id = rzp.reservation_id
             JOIN reservant res ON res.id = r.reservant_id
             JOIN zone_plan zp ON zp.id = rzp.zone_plan_id
             WHERE zp.festival_id = $1
             ORDER BY rzp.zone_plan_id, res.name, rzp.id`,
            [festivalId]
        );

        // 9. Jeux placés dans les zones (pour affichage détaillé avec nom du réservant et du jeu)
        const { rows: allGamePlacements } = await pool.query(
            `SELECT ja.id AS allocation_id, ja.reservation_id, ja.zone_plan_id,
                    ja.nb_tables_occupees, ja.nb_exemplaires, ja.nb_chaises,
                    ja.taille_table_requise,
                    g.title AS game_title,
                    res.name AS reservant_name
             FROM jeux_alloues ja
             JOIN games g ON g.id = ja.game_id
             JOIN reservation r ON r.id = ja.reservation_id
             JOIN reservant res ON res.id = r.reservant_id
             WHERE ja.zone_plan_id IS NOT NULL AND r.festival_id = $1
             ORDER BY ja.zone_plan_id, res.name, g.title`,
            [festivalId]
        );

        // 10. Tables disponibles par zone tarifaire (pour limite de création)
        const { rows: ztAvailableRows } = await pool.query(
            `SELECT zt.id AS zone_tarifaire_id,
                    zt.nb_tables AS total_tables,
                    COALESCE(SUM(zp.nb_tables), 0)::int AS tables_used
             FROM zone_tarifaire zt
             LEFT JOIN zone_plan zp ON zp.id_zone_tarifaire = zt.id
             WHERE zt.festival_id = $1
             GROUP BY zt.id, zt.nb_tables`,
            [festivalId]
        );
        const ztAvailable: Record<number, number> = {};
        for (const row of ztAvailableRows) {
            ztAvailable[row.zone_tarifaire_id] = Number(row.total_tables) - Number(row.tables_used);
        }

        res.json({
            zones,
            unplaced_games: unplacedGames,
            simple_allocations: simpleAllocations,
            reserved_zones_tarifaires: reservedZonesTarifaires,
            stock: {
                tables_standard: { total: Number(festival.stock_tables_standard || 0), occupied: occupiedByType['standard'] || 0 },
                tables_grande: { total: Number(festival.stock_tables_grande || 0), occupied: occupiedByType['grande'] || 0 },
                tables_mairie: { total: Number(festival.stock_tables_mairie || 0), occupied: occupiedByType['mairie'] || 0 },
                chaises: { total: Number(festival.stock_chaises || 0), allocated: Number(chaisesRows[0]?.total_chaises_allouees || 0) },
            },
            all_placements: allPlacements,
            all_game_placements: allGamePlacements,
            zt_available_tables: ztAvailable,
        });
    } catch (err) {
        console.error('Erreur lors de la récupération du contexte zone plan:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});


// Role : Creer une nouvelle zone de plan.
// Preconditions : Les champs requis sont fournis.
// Postconditions : Retourne la zone creee ou une erreur.
router.post('/', async (req, res) => {
    const { name, festival_id, id_zone_tarifaire, nb_tables } = req.body;
    
    if (!name || !festival_id || !id_zone_tarifaire || nb_tables === undefined) {
        return res.status(400).json({ error: 'Champs obligatoires manquants (name, festival_id, id_zone_tarifaire, nb_tables)' });
    }
    
    if (nb_tables < 0) {
        return res.status(400).json({ error: 'Le nombre de tables doit être positif' });
    }
    
    const client = await pool.connect();
    try {
        await client.query('BEGIN');
        
        // Vérifier que le festival existe
        const { rows: festivalRows } = await client.query(
            'SELECT id FROM festival WHERE id = $1',
            [festival_id]
        );
        
        if (festivalRows.length === 0) {
            await client.query('ROLLBACK');
            return res.status(404).json({ error: 'Festival non trouvé' });
        }
        
        // Vérifier que la zone tarifaire existe et appartient au même festival
        const { rows: zoneTarifaireRows } = await client.query(
            'SELECT id, festival_id, nb_tables FROM zone_tarifaire WHERE id = $1',
            [id_zone_tarifaire]
        );
        
        if (zoneTarifaireRows.length === 0) {
            await client.query('ROLLBACK');
            return res.status(404).json({ error: 'Zone tarifaire non trouvée' });
        }
        
        if (zoneTarifaireRows[0].festival_id !== parseInt(festival_id)) {
            await client.query('ROLLBACK');
            return res.status(400).json({ error: 'La zone tarifaire n\'appartient pas au festival spécifié' });
        }

        // Vérifier les tables disponibles dans la zone tarifaire
        const ztTotalTables = Number(zoneTarifaireRows[0].nb_tables);
        const { rows: usedTablesRows } = await client.query(
            `SELECT COALESCE(SUM(zp.nb_tables), 0)::int AS tables_used
             FROM zone_plan zp
             WHERE zp.id_zone_tarifaire = $1`,
            [id_zone_tarifaire]
        );
        const tablesUsed = Number(usedTablesRows[0]?.tables_used || 0);
        const tablesAvailable = ztTotalTables - tablesUsed;

        if (nb_tables > tablesAvailable) {
            await client.query('ROLLBACK');
            return res.status(400).json({
                error: `Pas assez de tables dans cette zone tarifaire. Disponibles: ${tablesAvailable}, Demandées: ${nb_tables}`
            });
        }
        
        // Créer la zone de plan
        const { rows } = await client.query(
            `INSERT INTO zone_plan (name, festival_id, id_zone_tarifaire, nb_tables)
             VALUES ($1, $2, $3, $4)
             RETURNING id, name, festival_id, id_zone_tarifaire, nb_tables`,
            [name, festival_id, id_zone_tarifaire, nb_tables]
        );
        
        await client.query('COMMIT');
        res.status(201).json({ 
            message: 'Zone de plan créée avec succès', 
            zone_plan: rows[0] 
        });
        
    } catch (err) {
        await client.query('ROLLBACK');
        console.error('Erreur lors de la création de la zone de plan:', err);
        
        // Gestion des erreurs spécifiques
        if (err instanceof Error && err.message.includes('duplicate key')) {
            return res.status(409).json({ error: 'Une zone de plan avec ce nom existe déjà' });
        }
        
        res.status(500).json({ 
            error: 'Erreur serveur', 
            details: err instanceof Error ? err.message : 'Erreur inconnue' 
        });
    } finally {
        client.release();
    }
});



// Role : Supprimer une zone de plan par ID.
// Preconditions : id est valide.
// Postconditions : Retourne un message de suppression ou une erreur.
router.delete('/:id', async (req, res) => {
    const { id } = req.params;
    
    const client = await pool.connect();
    try {
        await client.query('BEGIN');
        
        // Vérifier s'il y a des jeux alloués ou des réservations simples liées à cette zone de plan
        const { rows: jeuxAllouesRows } = await client.query(
            'SELECT COUNT(*) as count FROM jeux_alloues WHERE zone_plan_id = $1',
            [id]
        );
        const { rows: reservationsRows } = await client.query(
            'SELECT COUNT(*) as count FROM reservation_zone_plan WHERE zone_plan_id = $1',
            [id]
        );
        
        if (parseInt(jeuxAllouesRows[0].count) > 0 || parseInt(reservationsRows[0].count) > 0) {
            await client.query('ROLLBACK');
            return res.status(400).json({ 
                error: 'Impossible de supprimer cette zone de plan car elle contient des allocations' 
            });
        }
        
        // Supprimer la zone de plan
        const { rowCount } = await client.query(
            'DELETE FROM zone_plan WHERE id = $1',
            [id]
        );
        
        await client.query('COMMIT');
        
        if (rowCount === 0) {
            return res.status(404).json({ error: 'Zone de plan non trouvée' });
        }
        
        res.json({ message: 'Zone de plan supprimée avec succès' });
        
    } catch (err) {
        await client.query('ROLLBACK');
        console.error('Erreur lors de la suppression de la zone de plan:', err);
        res.status(500).json({ 
            error: 'Erreur serveur', 
            details: err instanceof Error ? err.message : 'Erreur inconnue' 
        });
    } finally {
        client.release();
    }
});

// Role : Recuperer les jeux alloues d'une zone de plan.
// Preconditions : id est valide.
// Postconditions : Retourne les jeux alloues ou une erreur.
router.get('/:id/jeux-alloues', async (req, res) => {
    const { id } = req.params;
    
    try {
        const { rows } = await pool.query(
            `SELECT
                ja.id AS allocation_id,
                ja.reservation_id,
                ja.game_id,
                ja.nb_tables_occupees,
                ja.nb_exemplaires,
                ja.nb_chaises,
                ja.zone_plan_id,
                ja.taille_table_requise,
                g.title,
                g.type,
                g.editor_id,
                e.name AS editor_name,
                g.min_age,
                g.authors,
                g.min_players,
                g.max_players,
                g.prototype,
                g.duration_minutes,
                g.theme,
                g.description,
                g.image_url,
                g.rules_video_url,
                r.reservant_id,
                res.name AS reservant_name,
                COALESCE(
                    json_agg(DISTINCT jsonb_build_object('id', m.id, 'name', m.name, 'description', m.description))
                        FILTER (WHERE m.id IS NOT NULL),
                    '[]'
                ) AS mechanisms
            FROM jeux_alloues ja
            JOIN games g ON g.id = ja.game_id
            LEFT JOIN editor e ON e.id = g.editor_id
            LEFT JOIN game_mechanism gm ON gm.game_id = g.id
            LEFT JOIN mechanism m ON m.id = gm.mechanism_id
            LEFT JOIN reservation r ON r.id = ja.reservation_id
            LEFT JOIN reservant res ON res.id = r.reservant_id
            WHERE ja.zone_plan_id = $1
            GROUP BY
                ja.id, ja.reservation_id, ja.game_id, ja.nb_tables_occupees, ja.nb_exemplaires, ja.nb_chaises,
                ja.zone_plan_id, ja.taille_table_requise,
                g.id, g.title, g.type, g.editor_id, e.name, g.min_age, g.authors,
                g.min_players, g.max_players, g.prototype, g.duration_minutes, g.theme,
                g.description, g.image_url, g.rules_video_url, r.reservant_id, res.name
            ORDER BY g.title ASC`,
            [id]
        );
        
        res.json(rows);
    } catch (err) {
        console.error('Erreur lors de la récupération des jeux alloués de la zone de plan:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Recuperer les jeux non alloues pour un festival.
// Preconditions : festival_id est valide, reservationId optionnel.
// Postconditions : Retourne les jeux non alloues ou une erreur.
router.get('/festival/:festival_id/jeux-non-alloues', async (req, res) => {
    const { festival_id } = req.params;
    const reservationId = req.query.reservationId ? Number(req.query.reservationId) : null;

    if (reservationId !== null && !Number.isFinite(reservationId)) {
        return res.status(400).json({ error: 'Identifiant de réservation invalide' });
    }

    const params: Array<number> = [Number(festival_id)];
    const reservationFilter = reservationId !== null ? 'AND ja.reservation_id = $2' : '';
    if (reservationId !== null) {
        params.push(reservationId);
    }

    try {
        const { rows } = await pool.query(
            `SELECT
                ja.id AS allocation_id,
                ja.reservation_id,
                ja.game_id,
                ja.nb_tables_occupees,
                ja.nb_exemplaires,
                ja.nb_chaises,
                ja.zone_plan_id,
                ja.taille_table_requise,
                g.title,
                g.type,
                g.editor_id,
                e.name AS editor_name,
                g.min_age,
                g.authors,
                g.min_players,
                g.max_players,
                g.prototype,
                g.duration_minutes,
                g.theme,
                g.description,
                g.image_url,
                g.rules_video_url,
                r.reservant_id,
                res.name AS reservant_name,
                COALESCE(
                    json_agg(DISTINCT jsonb_build_object('id', m.id, 'name', m.name, 'description', m.description))
                        FILTER (WHERE m.id IS NOT NULL),
                    '[]'
                ) AS mechanisms,
                COALESCE(
                    (SELECT json_agg(DISTINCT rzt.zone_tarifaire_id)
                     FROM reservation_zones_tarifaires rzt
                     WHERE rzt.reservation_id = r.id),
                    '[]'
                ) AS zones_tarifaires_reservees
            FROM jeux_alloues ja
            JOIN games g ON g.id = ja.game_id
            JOIN reservation r ON r.id = ja.reservation_id
            LEFT JOIN editor e ON e.id = g.editor_id
            LEFT JOIN game_mechanism gm ON gm.game_id = g.id
            LEFT JOIN mechanism m ON m.id = gm.mechanism_id
            LEFT JOIN reservant res ON res.id = r.reservant_id
            WHERE r.festival_id = $1 AND ja.zone_plan_id IS NULL
            ${reservationFilter}
            GROUP BY
                ja.id, ja.reservation_id, ja.game_id, ja.nb_tables_occupees, ja.nb_exemplaires, ja.nb_chaises,
                ja.zone_plan_id, ja.taille_table_requise,
                g.id, g.title, g.type, g.editor_id, e.name, g.min_age, g.authors,
                g.min_players, g.max_players, g.prototype, g.duration_minutes, g.theme,
                g.description, g.image_url, g.rules_video_url, r.reservant_id, res.name, r.id
            ORDER BY g.title ASC`,
            params
        );
        
        res.json(rows);
    } catch (err) {
        console.error('Erreur lors de la récupération des jeux non alloués:', err);
        res.status(500).json({ error: 'Erreur serveur' });
    }
});

// Role : Mettre a jour une zone de plan.
// Preconditions : id est valide et champs requis fournis.
// Postconditions : Retourne un message de mise a jour ou une erreur.
router.put('/:id', async (req, res) => {
    const { id } = req.params;
    const { name, id_zone_tarifaire, nb_tables } = req.body;

    if (!name || !id_zone_tarifaire || nb_tables === undefined) {
        return res.status(400).json({ error: 'Champs obligatoires manquants (name, id_zone_tarifaire, nb_tables)' });
    }

    if (nb_tables < 0) {
        return res.status(400).json({ error: 'Le nombre de tables doit être positif' });
    }

    try {
        const { rowCount } = await pool.query(
            `UPDATE zone_plan
             SET name = $1, id_zone_tarifaire = $2, nb_tables = $3
             WHERE id = $4`,
            [name, id_zone_tarifaire, nb_tables, id]
        );

        if (rowCount === 0) {
            return res.status(404).json({ error: 'Zone de plan non trouvée' });
        }

        res.json({ message: 'Zone de plan mise à jour avec succès' });

    } catch (err) {
        console.error('Erreur lors de la mise à jour de la zone de plan:', err);
        res.status(500).json({ 
            error: 'Erreur serveur', 
            details: err instanceof Error ? err.message : 'Erreur inconnue' 
        });
    }
});


export default router;
