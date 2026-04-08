/**
 * Rôle du fichier :
 * Interface DAO définissant les ponts de communication avec la table `games` locale de Room.
 * C'est cette interface qui gère toutes les recherches de jeux, le filtrage par titre, et
 * la persistance hors-ligne (Offline-First) comme le compte des jeux en attente d'être poussés
 * sur le serveur.
 */
package com.projetmobile.mobile.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.projetmobile.mobile.data.room.GameRoomEntity
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Interface d'accès aux données de jeux du festival.
 * Les méthodes retournant un [Flow] constituent la Source de Vérité Unique (SSOT) :
 * l'UI observe ces flux réactifs et se met à jour automatiquement à chaque modification locale.
 */
@Dao
interface GameDao {

    /** 
     * Rôle :
     * Diffuser la liste complète de tous les jeux de société enregistrés, triée par nom de jeu.
     * 
     * Précondition : La base est initialisée et lisible.
     * Postcondition : Un Flux (Flow) émettant la liste des entités `GameRoomEntity` en excluant les jeux 
     * supprimés ou en instance de suppression.
     */
    @Query(
        """
        SELECT * FROM games
        WHERE syncStatus != '${SyncStatus.PENDING_DELETE}'
          AND NOT (syncStatus = '${SyncStatus.ERROR}' AND retryAction = '${SyncRetryAction.DELETE}')
        ORDER BY title ASC
        """,
    )
    fun observeAll(): Flow<List<GameRoomEntity>>

    /** 
     * Rôle :
     * Procurer un flux de résultats de recherche de jeux filtrés dynamiquement par leur titre.
     * 
     * Précondition : [search] Un texte de recherche pouvant être vide.
     * Postcondition : Renvoie un Flow avec tous les jeux dont le titre contient la sous-chaîne `search` 
     * (insensible à la casse grâce à `LOWER()`).
     */
    @Query("""
        SELECT * FROM games
        WHERE syncStatus != '${SyncStatus.PENDING_DELETE}'
          AND NOT (syncStatus = '${SyncStatus.ERROR}' AND retryAction = '${SyncRetryAction.DELETE}')
          AND (:search = '' OR LOWER(title) LIKE '%' || LOWER(:search) || '%')
        ORDER BY title ASC
    """)
    fun observeByTitle(search: String): Flow<List<GameRoomEntity>>

    /** 
     * Rôle :
     * Suivre l'état ou les requêtes d'un jeu de société unique.
     * 
     * Précondition : [id] L'entier identifiant le jeu spécifique.
     * Postcondition : Flux de l'entité de jeu, qui sera `null` s'il est inconnu ou supprimé entre temps.
     */
    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<GameRoomEntity?>

    /** 
     * Rôle :
     * Lire statiquement les propriétés complètes d'un jeu lors d'opérations asynchrones (Worker, API Sync).
     * 
     * Précondition : [id] Pointeur identifiant de la ligne SQL.
     * Postcondition : Un objet simple [GameRoomEntity] unique sans observeration réactive.
     */
    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): GameRoomEntity?

    /** 
     * Rôle :
     * Collecter tous les jeux créés, modifiés ou supprimés localement par l'utilisateur, 
     * mais non validés par le backend distant.
     * 
     * Précondition : La base intègre la colonne `syncStatus` ou un `retryAction` explicite.
     * Postcondition : Retourne la liste stricte des tâches SQLite devant être poussées (Push) sur API.
     */
    @Query(
        """
        SELECT * FROM games
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_CREATE}', '${SyncStatus.PENDING_UPDATE}', '${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun getPending(): List<GameRoomEntity>

    /** 
     * Rôle :
     * Analyser et retourner le catalogue absolu des jeux de la base locale, incluant les brouillons.
     * 
     * Précondition : Table `games` existante.
     * Postcondition : Une liste en mémoire `List<GameRoomEntity>`.
     */
    @Query("SELECT * FROM games")
    suspend fun getAll(): List<GameRoomEntity>

    /** 
     * Rôle :
     * Extraire de manière optimisée la quantité de charge de travail restante (tâches hors ligne).
     * 
     * Précondition : Base en lecture.
     * Postcondition : Un nombre entier `Int` permettant par exemple d'afficher un indicateur d'action à synchroniser.
     */
    @Query(
        """
        SELECT COUNT(*)
        FROM games
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_CREATE}', '${SyncStatus.PENDING_UPDATE}', '${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun countPendingWork(): Int

    /** 
     * Rôle :
     * Enregistrer de nouveaux jeux et substituer ceux existants s'ils ont le même ID.
     * 
     * Précondition : [games] tableau d'entités valides.
     * Postcondition : La base locale reflète désormais le paramètre fourni (Upsert = Update + Insert).
     */
    @Upsert
    suspend fun upsertAll(games: List<GameRoomEntity>)

    /** 
     * Rôle :
     * Sauvegarder ou écraser les données d'un seul jeu spécifique.
     * 
     * Précondition : [game] Un objet jeu structuré.
     * Postcondition : Base Room modifiée au niveau ligne.
     */
    @Upsert
    suspend fun upsert(game: GameRoomEntity)

    /** 
     * Rôle :
     * Éliminer physiquement de la base le jeu ciblé.
     * 
     * Précondition : Un [id] existant ou non. S'il n'existe pas, aucune erreur levée, l'opération s'ignore.
     * Postcondition : Disparition de la donnée du système de fichiers SQLite.
     */
    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteById(id: Int)

    /** 
     * Rôle :
     * Affecter l'état "Brouillon / Suppression en Attente", bloquant l'affichage du jeu sans purger ses données.
     * 
     * Précondition : Un [id] correspondant.
     * Postcondition : SQL Update local de `syncStatus` et des marqueurs `retryAction`. 
     * (Déclenchera une éviction des vues utilisant `observeAll()`).
     */
    @Query(
        """
        UPDATE games
        SET syncStatus = '${SyncStatus.PENDING_DELETE}',
            retryAction = '${SyncRetryAction.DELETE}',
            lastSyncErrorMessage = NULL
        WHERE id = :id
        """,
    )
    suspend fun markForDeletion(id: Int)

    /** 
     * Rôle :
     * Clôturer ou relancer l'enregistrement d'un envoi de requête HTTP sur la synchronisation d'un jeu.
     * 
     * Précondition : Les marqueurs nécessaires (Id, Statut visé, et erreurs).
     * Postcondition : Maintient en cohérence la mémoire hors-connexion.
     */
    @Query(
        """
        UPDATE games
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
