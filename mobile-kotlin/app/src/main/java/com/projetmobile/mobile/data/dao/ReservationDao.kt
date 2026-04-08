/**
 * Rôle du fichier :
 * Interface DAO définissant les requêtes SQLite gérant les opérations CRUD sur la base
 * de données locale (Room) spécifiquement pour la table des réservations (`reservations`).
 * Ce fichier est responsable de l'extraction, la mise à jour, l'insertion 
 * ou la notification de suspension de données pour le client hors-ligne en lien avec 
 * le workflow des réservations de stands sur un festival.
 */
package com.projetmobile.mobile.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.projetmobile.mobile.data.room.ReservationRoomEntity
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Composant Room gérant la communication applicative vers la table physique `reservations`.
 */
@Dao
interface ReservationDao {

    /** 
     * Rôle :
     * Suivre de manière réactive toutes les réservations attachées à un festival donné, 
     * en ignorant celles que l'utilisateur local vient de jeter (Soft Delete / Error de delete).
     * 
     * Précondition : Un `festivalId` (Entier) identifiant le festival ciblé depuis la Vue.
     * Postcondition : Un Flux (Flow) émettant une liste d'entités `ReservationRoomEntity` triée 
     * alphabétiquement sur le nom de l'exposant (`reservantName`).
     */
    @Query("""
        SELECT * FROM reservations
        WHERE festivalId = :festivalId
          AND syncStatus != '${SyncStatus.PENDING_DELETE}'
          AND NOT (syncStatus = '${SyncStatus.ERROR}' AND retryAction = '${SyncRetryAction.DELETE}')
        ORDER BY reservantName ASC
    """)
    fun observeByFestival(festivalId: Int): Flow<List<ReservationRoomEntity>>

    /** 
     * Rôle :
     * Obtenir une mise à jour continue (observer) d'un enregistrement unique spécifique 
     * lorsque des actions affectent ce champ.
     * 
     * Précondition : Fournir l'`id` de la réservation ciblée.
     * Postcondition : Emet la ligne de réservation formatée, ou un signal Null si elle n'est pas ou plus en table.
     */
    @Query("SELECT * FROM reservations WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<ReservationRoomEntity?>

    /** 
     * Rôle :
     * Rechercher une réservation immédiatement sans y attacher d'observateur (lecture statique pour worker).
     * 
     * Précondition : Fournir un `id` défini.
     * Postcondition : Extrait l'entité si trouvée en base, `null` sinon.
     */
    @Query("SELECT * FROM reservations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ReservationRoomEntity?

    /** 
     * Rôle :
     * Détecter toutes les lignes (tuples) de cette table qui ne sont pas validées par le serveur, 
     * que l'action locale (Create, Update ou Delete) soit en attente ou en erreur confirmée (retryAction).
     * 
     * Précondition : Le schéma de persistance doit supporter les colonnes `retryAction` et `syncStatus`.
     * Postcondition : Renvoie une liste d'éléments asynchrones à synchroniser.
     */
    @Query(
        """
        SELECT * FROM reservations
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_CREATE}', '${SyncStatus.PENDING_UPDATE}', '${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun getPending(): List<ReservationRoomEntity>

    /** 
     * Rôle :
     * Obtenir un vidage complet (y compris les en cours de suppression) des réservations d'un évènement.
     * 
     * Précondition : L'id du festival `festivalId`.
     * Postcondition : Retourne la liste brutes des items correspondants.
     */
    @Query("SELECT * FROM reservations WHERE festivalId = :festivalId")
    suspend fun getAllByFestival(festivalId: Int): List<ReservationRoomEntity>

    /** 
     * Rôle :
     * Calculer un compteur du nombe total de travaux hors ligne (CRUDS hors ligne en attente)
     * à envoyer au Backend sur les Réservations.
     * 
     * Précondition : Aucune hormis l'accès au service Room.
     * Postcondition : Un entier du nombre de requêtes suspendues.
     */
    @Query(
        """
        SELECT COUNT(*)
        FROM reservations
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_CREATE}', '${SyncStatus.PENDING_UPDATE}', '${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun countPendingWork(): Int

    /** 
     * Rôle :
     * Insérer ou rafraichir un lot complet de nouvelles infos fraîchement reçues de l'API.
     * 
     * Précondition : Une liste valides d'objets `ReservationRoomEntity`.
     * Postcondition : L'ensemble de la liste est implémenté / écrasé en base.
     */
    @Upsert
    suspend fun upsertAll(reservations: List<ReservationRoomEntity>)

    /** 
     * Rôle :
     * Même action que `upsertAll` mais localisée sur une seule entité.
     * 
     * Précondition : L'entité paramètre configurée.
     * Postcondition : Modification actée au niveau Row.
     */
    @Upsert
    suspend fun upsert(reservation: ReservationRoomEntity)

    /** 
     * Rôle :
     * La destruction atomique d'une ligne d'Id fourni de la table des réservations.
     * (Différent de markForDeletion, ici on vide l'espace disque).
     * 
     * Précondition : Fournir le pointeur d'`id`.
     * Postcondition : La suppression est immédiate, impactant aussitôt les `observeByFestival`.
     */
    @Query("DELETE FROM reservations WHERE id = :id")
    suspend fun deleteById(id: Int)

    /** 
     * Rôle :
     * Cacher une réservation existante en signalant à l'application 
     * que l'utilisateur a cliqué sur "Supprimer" alors qu'il n'a pas accès à l'API distante.
     * 
     * Précondition : Identifier `id` correspondant à la ligne à cacher.
     * Postcondition : Remplacement partiel (Status) de la ligne modifiée.
     */
    @Query(
        """
        UPDATE reservations
        SET syncStatus = '${SyncStatus.PENDING_DELETE}',
            retryAction = '${SyncRetryAction.DELETE}',
            lastSyncErrorMessage = NULL
        WHERE id = :id
        """,
    )
    suspend fun markForDeletion(id: Int)

    /** 
     * Rôle :
     * Après chaque tentative de fond (Push du sync Worker), ce script attribue l'état définitif d'un record 
     * (Erreur, ou Synced), l'identifiant pour la suite ou rapportant le message utilisateur.
     * 
     * Précondition : Paramètres d'identifiant `id`, statut de synchronisation `status`, action en retard `retryAction` etc.
     * Postcondition : La ligne porte le sceau de son dernier test réseau.
     */
    @Query(
        """
        UPDATE reservations
        SET syncStatus = :status,
            retryAction = :retryAction,
            lastSyncErrorMessage = :lastSyncErrorMessage
        WHERE id = :id
        """,
    )
    suspend fun updateSyncState(
        id: Int,
        status: String,
        retryAction: String?,
        lastSyncErrorMessage: String?,
    )
}
