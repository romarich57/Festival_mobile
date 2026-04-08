package com.projetmobile.mobile.data.repository.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantContact
import com.projetmobile.mobile.data.entity.reservants.ReservantContactDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantDeleteSummary
import com.projetmobile.mobile.data.entity.reservants.ReservantDetail
import com.projetmobile.mobile.data.entity.reservants.ReservantDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantEditorOption
import com.projetmobile.mobile.data.entity.reservants.ReservantListItem
import kotlinx.coroutines.flow.Flow

/**
 * Rôle : Contrôleur contractuel de l'accès aux données des "Réservants" (les entités
 * louant des espaces au festival, ex: Éditeurs, Auteurs, Boutiques).
 * 
 * Précondition : Modules de base de données (Room) et réseau injectés.
 * Postcondition : Structure les flux observables et les modérations offline-first.
 */
interface ReservantsRepository {

    // ── Observation Room (SSOT) ──────────────────────────────────────────────

    /**
     * Rôle : Obtenir la collection réactive totale de tous les réservants.
     * 
     * Précondition : La base locale Room est accessible.
     * Postcondition : Un Flux listant des [ReservantListItem].
     */
    fun observeReservants(): Flow<List<ReservantListItem>>

    /**
     * Rôle : Scruter spécifiquement la fiche d'un réservant.
     * 
     * Précondition : Connaître son ID.
     * Postcondition : Emet un [ReservantDetail] ou null s'il a été effacé.
     */
    fun observeReservant(reservantId: Int): Flow<ReservantDetail?>

    // ── Déclenchement réseau ────────────────────────────────────────────────

    /**
     * Rôle : Forcer le téléchargement global des réservants depuis le back-end pour peupler Room.
     * 
     * Précondition : L'appareil est en ligne.
     * Postcondition : Maintien de l'Offline-first, avec suppression des entités n'existant plus côté serveur.
     */
    suspend fun refreshReservants(): Result<List<ReservantListItem>>

    /**
     * Rôle : Interroger l'API pour rapatrier un profil de réservant unitaire vers Room.
     * 
     * Précondition : ID du réservant.
     * Postcondition : Upsert de la table et retour du résultat.
     */
    suspend fun getReservant(reservantId: Int): Result<ReservantDetail>

    // ── Écriture offline-first ───────────────────────────────────────────────

    /**
     * Rôle : Ajoute un réservant inédit avec ID négatif en local (PENDING_CREATE).
     * 
     * Précondition : Le formulaire DTO métier formatté.
     * Postcondition : Sauvegarde immédiate. Worker de Sync réveillé.
     */
    suspend fun createReservant(draft: ReservantDraft): Result<ReservantDetail>

    /**
     * Rôle : Effectue une modification du modèle métier du réservant (PENDING_UPDATE local).
     * 
     * Précondition : L'ID cible et les variables actualisées.
     * Postcondition : Local écrasé, Worker asynchrone sollicité.
     */
    suspend fun updateReservant(
        reservantId: Int,
        draft: ReservantDraft,
    ): Result<ReservantDetail>

    /**
     * Rôle : Anticipe la suppression, renseigne sur les entités/fichiers qui seront en cascade supprimés.
     * 
     * Précondition : ID d'un réservant existant.
     * Postcondition : Donne une évaluation des dégâts (ex: jeux associés à supprimer).
     */
    suspend fun getDeleteSummary(reservantId: Int): Result<ReservantDeleteSummary>

    /**
     * Rôle : Enregistre ou finalise une suppression (PENDING_DELETE si synchronisé avant).
     * 
     * Précondition : Réservant cible existant.
     * Postcondition : Si virtuel, supprimé directement de la DB mobile, sinon déclanche le Worker.
     */
    suspend fun deleteReservant(reservantId: Int): Result<String>

    // ── Contacts (réseau direct) ─────────────────────────────────────────────

    /**
     * Rôle : Requête la liste stricte des compagnies "Éditeur".
     * 
     * Précondition : Endpoint d'API atteignable.
     * Postcondition : Dictionnaire des éditeurs.
     */
    suspend fun getEditors(): Result<List<ReservantEditorOption>>

    /**
     * Rôle : Rapporte la chaîne d'interlocuteurs enregistrée pour ce réservant.
     * 
     * Précondition : ID de l'usager cible.
     * Postcondition : Collection d'historique de relance/contacts.
     */
    suspend fun getContacts(reservantId: Int): Result<List<ReservantContact>>

    /**
     * Rôle : Attache une nouvelle mention relationnelle au dossier du réservant (ex: Appel, mail).
     * 
     * Précondition : Le profil existe, réseau ok (pas d'Offline First ici).
     * Postcondition : Nouveau contact confirmé côté serveur.
     */
    suspend fun addContact(
        reservantId: Int,
        draft: ReservantContactDraft,
    ): Result<ReservantContact>
}
