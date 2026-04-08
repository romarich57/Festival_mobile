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

/**
 * Rôle : Assurer la liaison de données contractuelle pour le référentiel des Jeux du festival.
 * Pilote la synchronisation Offline-First et la Single Source of Truth (SSOT).
 * 
 * Précondition : Module Koin initialisé.
 * Postcondition : Opérations asynchrones (`Result`) et flux réactifs (`Flow`).
 */
interface GamesRepository {

    // ── Observation Room (SSOT) — Offline-first ──────────────────────────────

    /**
     * Rôle : Offrir un flux live des jeux depuis Room, filtré dynamiquement par titre.
     * L'UI observe ce Flow : toute modification locale ou réseau est reflétée automatiquement.
     * 
     * Précondition : Table Room en place.
     * Postcondition : Émet des résumés [GameListItem].
     */
    fun observeGames(titleSearch: String = ""): Flow<List<GameListItem>>

    /**
     * Rôle : Offrir un flux live détailé d'un jeu précis depuis Room.
     * 
     * Précondition : Fournir l'ID.
     * Postcondition : Émet null si absent, sinon le [GameDetail] synchronisé.
     */
    fun observeGame(gameId: Int): Flow<GameDetail?>

    // ── Déclenchement réseau (Network-Bound Resource) ────────────────────────

    /**
     * Rôle : Requêter les jeux paginés depuis le serveur réseau API, et 
     * les persister pour l'affichage Offline.
     * 
     * Précondition : Arguments réseaux de filtres/pagination valides.
     * Postcondition : Upsert l'état local dans la BDD et notifie via Flow ([PagedResult]).
     */
    suspend fun refreshGames(
        filters: GameFilters,
        page: Int,
        limit: Int,
    ): Result<PagedResult<GameListItem>>

    /**
     * Rôle : Requêter un unitaire de jeu depuis l'API distante et alimenter Room.
     * 
     * Précondition : Un ID correct.
     * Postcondition : Met à jour Room, déclenchera implicitement `observeGame`.
     */
    suspend fun getGame(gameId: Int): Result<GameDetail>

    // ── Écriture offline-first ───────────────────────────────────────────────

    /**
     * Rôle : Créer un nouveau jeu. Sauvegardé instantanément dans Room (ID Négatif, statut PENDING_CREATE)
     * puis envoie un ordre au [GameSyncWorker].
     * 
     * Précondition : Draft valide rempli.
     * Postcondition : L'UI dispose à la seconde de son objet créé en local.
     */
    suspend fun createGame(draft: GameDraft): Result<GameDetail>

    /**
     * Rôle : Effectuer une surcharge/édition d'un jeu (PENDING_UPDATE local + Worker).
     * 
     * Précondition : Le draft doit exister dans son ID et le payload [GameDraft].
     * Postcondition : Room est écrasé sur les champs puis le syncer est réveillé.
     */
    suspend fun updateGame(gameId: Int, draft: GameDraft): Result<GameDetail>

    /**
     * Rôle : Initier la disparition d'un jeu (PENDING_DELETE si jeu distant).
     * 
     * Précondition : L'administrateur autorise cette requête via Session.
     * Postcondition : Caché de l'UI localement et effacement planifié à distance.
     */
    suspend fun deleteGame(gameId: Int): Result<String>

    // ── Lookups (réseau avec cache simple) ──────────────────────────────────

    /**
     * Rôle : Saisir la base enum des différents genres de jeux.
     * 
     * Précondition : Endpoint accessible.
     * Postcondition : Un résultat de [GameTypeOption].
     */
    suspend fun getGameTypes(): Result<List<GameTypeOption>>
    
    /**
     * Rôle : Récupérer le mapping des entreprises éditrices.
     * 
     * Précondition : Endpoint API actif.
     * Postcondition : Base métier de sélection pour comboBox.
     */
    suspend fun getEditors(): Result<List<EditorOption>>
    
    /**
     * Rôle : Récupérer liste des attibuts de mécanismes.
     * 
     * Précondition : API active.
     * Postcondition : Dictionnaire des modes de jeu.
     */
    suspend fun getMechanisms(): Result<List<MechanismOption>>
    
    /**
     * Rôle : Pousser vers le CDN l'image de couverture du jeu de manière multipartie.
     * 
     * Précondition : Extension acceptée, Mime et Bytes de l'image.
     * Postcondition : Upload, puis le serveur API nous remet la chaîne publique URL.
     */
    suspend fun uploadGameImage(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Result<String>
}
