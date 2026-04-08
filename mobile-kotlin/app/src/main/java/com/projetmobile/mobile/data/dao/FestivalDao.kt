/**
 * Rôle du fichier :
 * Définit l'interface DAO (Data Access Object) pour la table `festivals`.
 * C'est le contrat qui expose les différentes méthodes pour lire, insérer,
 * modifier ou supprimer des festivals directement depuis la base de données Room (SQLite).
 * Il intègre des flux réactifs (`Flow`) pour que l'interface se mette à jour dès qu'une
 * donnée change en base de données, gérant aussi les données en attente de synchronisation.
 */
package com.projetmobile.mobile.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.projetmobile.mobile.data.room.FestivalRoomEntity
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Interface d'accès aux données pour l'entité Festival.
 * Traite principalement des requêtes de type lecture pour l'affichage,
 * avec une prise en charge de la suppression différée en mode hors-ligne.
 */
@Dao
interface FestivalDao {

    /** 
     * Rôle :
     * Observer la liste de tous les festivals valides de la base.
     * 
     * Précondition : La base de données Room doit être accessible et en lecture.
     * Postcondition : Retourne un flux continu (Flow) contenant la liste des festivals ordonnés 
     * par date de début décroissante, en filtrant (excluant) ceux qui sont marqués pour suppression.
     */
    @Query(
        """
        SELECT * FROM festivals
        WHERE syncStatus != '${SyncStatus.PENDING_DELETE}'
          AND NOT (syncStatus = '${SyncStatus.ERROR}' AND retryAction = '${SyncRetryAction.DELETE}')
        ORDER BY startDate DESC
        """,
    )
    fun observeAll(): Flow<List<FestivalRoomEntity>>

    /** 
     * Rôle :
     * Surveiller et retourner un évènement unique d'un festival selon son ID.
     * 
     * Précondition : Un `id` Entier existant (ou non) dans la base.
     * Postcondition : Retourne un flux réactif contenant le festival trouvé ou `null` dynamique.
     */
    @Query("SELECT * FROM festivals WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<FestivalRoomEntity?>

    /** 
     * Rôle :
     * Récupérer un festival par son ID sous forme de tâche suspendue (lecture unique non-réactive).
     * 
     * Précondition : L'`id` Entier du festival ciblé.
     * Postcondition : Retourne le festival complet s'il existe, sinon `null`.
     */
    @Query("SELECT * FROM festivals WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): FestivalRoomEntity?

    /** 
     * Rôle :
     * Récupérer toute la liste de festivals (tâche suspendue).
     * 
     * Précondition : La base SQLite est intègre.
     * Postcondition : Retourne une liste de toutes les entrées contenues dans la table `festivals`.
     */
    @Query("SELECT * FROM festivals")
    suspend fun getAll(): List<FestivalRoomEntity>

    /** 
     * Rôle :
     * Insérer ou écraser (Upload/Insert => Upsert) une liste complète de festivals d'un seul coup.
     * 
     * Précondition : Une liste d'entités [FestivalRoomEntity].
     * Postcondition : Les entrées sont sauvegardées en locale sans doublon d'ID.
     */
    @Upsert
    suspend fun upsertAll(festivals: List<FestivalRoomEntity>)

    /** 
     * Rôle :
     * Insérer ou Mettre à jour en local un unique festival.
     * 
     * Précondition : Un objet entité [FestivalRoomEntity].
     * Postcondition : Mise à jour de la ligne ciblée en base de données.
     */
    @Upsert
    suspend fun upsert(festival: FestivalRoomEntity)

    /** 
     * Rôle :
     * Lister les festivals qui requièrent une synchronisation réseau (parce qu'ils ont un statut
     * d'action en attente, souvent suite à une action faite hors-ligne).
     * 
     * Précondition : Le Worker SyncBackground lance ce calcul et la connexion locale SQLite doit fonctionner.
     * Postcondition : Retourne une liste des objets `Festival` marqués en erreur ou en attente.
     */
    @Query(
        """
        SELECT * FROM festivals
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun getPending(): List<FestivalRoomEntity>

    /** 
     * Rôle :
     * Compter le nombre de travaux de synchronisation en attente sur la table festivals.
     * 
     * Précondition : Base de données accessible.
     * Postcondition : Renvoie un entier représentant le total de requêtes en retard.
     */
    @Query(
        """
        SELECT COUNT(*)
        FROM festivals
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun countPendingWork(): Int

    /** 
     * Rôle :
     * Supprimer définitivement et physiquement le festival de la base SQLite.
     * 
     * Précondition : Un entier `id` d'un festival.
     * Postcondition : L'élément est purgé du téléphone.
     */
    @Query("DELETE FROM festivals WHERE id = :id")
    suspend fun deleteById(id: Int)

    /** 
     * Rôle :
     * Effectuer une modification purement métadonnée : on indique au local qu'on ne veut 
     * plus ce festival, sans pour autant le détruire avant qu'elle soit actée par le serveur.
     * C'est le principe du "Soft Delete".
     * 
     * Précondition : Un `id` existant d'un festival que l'on veut jeter.
     * Postcondition : Le tuple en SQL a ses colonnes de "sync" basculées sur l'état "Suppression En Attente".
     */
    @Query(
        """
        UPDATE festivals
        SET syncStatus = '${SyncStatus.PENDING_DELETE}',
            retryAction = '${SyncRetryAction.DELETE}',
            lastSyncErrorMessage = NULL
        WHERE id = :id
        """,
    )
    suspend fun markForDeletion(id: Int)

    /** 
     * Rôle :
     * Réécrire les statuts de validation/erreur après une tentative réseau de synchronisation d'un festival.
     * 
     * Précondition : 
     * - `id` intègre à actualiser.
     * - `status` le nouveau code (SYCNED, ERROR, etc).
     * - Les éventuels `retryAction` et `lastSyncErrorMessage`.
     * Postcondition : Les paramètres annexes de l'entité sont mis à jour dans la base.
     */
    @Query(
        """
        UPDATE festivals
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
