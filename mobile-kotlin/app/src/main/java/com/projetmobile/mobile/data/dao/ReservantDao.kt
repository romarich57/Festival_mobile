package com.projetmobile.mobile.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.projetmobile.mobile.data.room.ReservantRoomEntity
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Rôle : DAO (Data Access Object) Room dédié à la gestion locale des réservants
 * (éditeurs ou créateurs). Permet de lire, insérer, mettre à jour et préparer la suppression
 * des entités tout en prenant en compte leur statut de synchronisation (Offline-First).
 * 
 * Précondition : La base de données Room doit être instanciée de manière valide.
 * Postcondition : Offre une interface permettant d'interagir avec la table 'reservants'.
 */
@Dao
interface ReservantDao {

    /**
     * Rôle : Fournit un flux (Flow) réactif de tous les réservants valides (non supprimés) 
     * triés par ordre alphabétique. Exclut les éléments en attente de synchronisation de suppression.
     * 
     * Précondition : La table 'reservants' est accessible.
     * Postcondition : Émet une liste mise à jour à chaque modification de la table.
     */
    @Query(
        """
        SELECT * FROM reservants
        WHERE syncStatus != '${SyncStatus.PENDING_DELETE}'
          AND NOT (syncStatus = '${SyncStatus.ERROR}' AND retryAction = '${SyncRetryAction.DELETE}')
        ORDER BY name ASC
        """,
    )
    fun observeAll(): Flow<List<ReservantRoomEntity>>

    /**
     * Rôle : Fournit un flux réactif pour observer un réservant spécifique en fournissant son identifiant.
     * 
     * Précondition : Un ID de réservant est fourni.
     * Postcondition : Émet l'entité correspondante si elle existe, ou null sinon, et se met à jour réactivement.
     */
    @Query("SELECT * FROM reservants WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<ReservantRoomEntity?>

    /**
     * Rôle : Récupère ponctuellement (one-shot) un réservant selon son ID local ou distant.
     * 
     * Précondition : La fonction est appelée dans une coroutine valide.
     * Postcondition : Retourne directement l'objet s'il est trouvé.
     */
    @Query("SELECT * FROM reservants WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ReservantRoomEntity?

    /**
     * Rôle : Recherche tous les réservants stockés localement nécessitant 
     * une synchronisation avec le serveur (création, modification ou suppression en attente).
     * 
     * Précondition : Utilisé principalement par le Synchronizer en arrière-plan.
     * Postcondition : Renvoie la liste des opérations en attente (statut PENDING_* ou retryAction non null).
     */
    @Query(
        """
        SELECT * FROM reservants
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_CREATE}', '${SyncStatus.PENDING_UPDATE}', '${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun getPending(): List<ReservantRoomEntity>

    /**
     * Rôle : Récupère la totalité des réservants stockés dans la base sans aucun filtre de synchronisation.
     * 
     * Précondition : Exécution au sein d'une coroutine.
     * Postcondition : Retourne la liste brute complète issue de la table locale.
     */
    @Query("SELECT * FROM reservants")
    suspend fun getAll(): List<ReservantRoomEntity>

    /**
     * Rôle : Compte le nombre exact d'actions de synchronisation en attente concernant les réservants.
     * 
     * Précondition : Utilisé pour l'interface utilisateur (badges ronds) ou le système de log.
     * Postcondition : Retourne un entier représentant le total des actions hors-ligne non synchronisées.
     */
    @Query(
        """
        SELECT COUNT(*)
        FROM reservants
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_CREATE}', '${SyncStatus.PENDING_UPDATE}', '${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun countPendingWork(): Int

    /**
     * Rôle : Insère ou met à jour une liste complète de réservants dans la base locale (upsert).
     * 
     * Précondition : Les objets entités sont formés correctement et possèdent un ID.
     * Postcondition : Les lignes de la table locale sont créées ou écrasées.
     */
    @Upsert
    suspend fun upsertAll(reservants: List<ReservantRoomEntity>)

    /**
     * Rôle : Insère ou met à jour un réservant spécifique (upsert).
     * 
     * Précondition : L'entité fournie contient un ID approprié.
     * Postcondition : Le réservant est mémorisé/mis-à-jour avec sa version locale la plus récente.
     */
    @Upsert
    suspend fun upsert(reservant: ReservantRoomEntity)

    /**
     * Rôle : Efface définitivement l'enregistrement d'un réservant de la base locale via son ID.
     * 
     * Précondition : Réservant identifié par 'id'.
     * Postcondition : La donnée de ce réservant disparaît de la base locale.
     */
    @Query("DELETE FROM reservants WHERE id = :id")
    suspend fun deleteById(id: Int)

    /**
     * Rôle : Permet de marquer un réservant existant vers un état de suppression locale
     * pour qu'il soit traité par le Job de synchronisation ultérieurement. Ne l'efface pas de suite.
     * 
     * Précondition : Le réservant ciblé existe en base.
     * Postcondition : État modifié à PENDING_DELETE ; les flux réactifs l'excluront de l'affichage.
     */
    @Query(
        """
        UPDATE reservants
        SET syncStatus = '${SyncStatus.PENDING_DELETE}',
            retryAction = '${SyncRetryAction.DELETE}',
            lastSyncErrorMessage = NULL
        WHERE id = :id
        """,
    )
    suspend fun markForDeletion(id: Int)

    /**
     * Rôle : Met à jour purement technique de l'état de synchronisation d'un réservant.
     * 
     * Précondition : Une tentative de synchronisation (succès ou erreur) a eu lieu.
     * Postcondition : L'enregistrement reflète l'avancement/le statut d'erreur du syncer.
     */
    @Query(
        """
        UPDATE reservants
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
