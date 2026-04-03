package com.projetmobile.mobile.data.repository.games

import com.projetmobile.mobile.data.entity.games.EditorOption
import com.projetmobile.mobile.data.entity.games.GameDetail
import com.projetmobile.mobile.data.entity.games.GameDraft
import com.projetmobile.mobile.data.entity.games.GameFilters
import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.games.GameTypeOption
import com.projetmobile.mobile.data.entity.games.MechanismOption
import com.projetmobile.mobile.data.entity.games.PagedResult
import kotlinx.coroutines.flow.Flow

interface GamesRepository {

    // ── Observation Room (SSOT) — Offline-first ──────────────────────────────

    /**
     * Flux live des jeux depuis Room, filtré par titre.
     * L'UI observe ce Flow : toute modification locale ou réseau est reflétée automatiquement.
     */
    fun observeGames(titleSearch: String = ""): Flow<List<GameListItem>>

    /** Flux live d'un jeu par ID depuis Room (null si absent). */
    fun observeGame(gameId: Int): Flow<GameDetail?>

    // ── Déclenchement réseau (Network-Bound Resource) ────────────────────────

    /**
     * Récupère les jeux depuis le réseau et les stocke dans Room.
     * L'UI ne lit pas ce résultat directement : elle observe [observeGames].
     */
    suspend fun refreshGames(
        filters: GameFilters,
        page: Int,
        limit: Int,
    ): Result<PagedResult<GameListItem>>

    /** Récupère un jeu depuis le réseau et met à jour Room. */
    suspend fun getGame(gameId: Int): Result<GameDetail>

    // ── Écriture offline-first ───────────────────────────────────────────────

    /**
     * Crée un jeu : sauvegarde immédiatement dans Room (PENDING_CREATE),
     * puis planifie [GameSyncWorker] pour la synchronisation réseau.
     */
    suspend fun createGame(draft: GameDraft): Result<GameDetail>

    /**
     * Met à jour un jeu : modifie Room immédiatement (PENDING_UPDATE),
     * puis planifie [GameSyncWorker].
     */
    suspend fun updateGame(gameId: Int, draft: GameDraft): Result<GameDetail>

    /**
     * Supprime un jeu : marque PENDING_DELETE dans Room (disparaît de l'UI),
     * puis planifie [GameSyncWorker].
     */
    suspend fun deleteGame(gameId: Int): Result<String>

    // ── Lookups (réseau avec cache simple) ──────────────────────────────────

    suspend fun getGameTypes(): Result<List<GameTypeOption>>
    suspend fun getEditors(): Result<List<EditorOption>>
    suspend fun getMechanisms(): Result<List<MechanismOption>>
    suspend fun uploadGameImage(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Result<String>
}
