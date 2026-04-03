package com.projetmobile.mobile.testutils

import com.projetmobile.mobile.data.entity.games.EditorOption
import com.projetmobile.mobile.data.entity.games.GameDetail
import com.projetmobile.mobile.data.entity.games.GameDraft
import com.projetmobile.mobile.data.entity.games.GameFilters
import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.games.GameTypeOption
import com.projetmobile.mobile.data.entity.games.MechanismOption
import com.projetmobile.mobile.data.entity.games.PagedResult
import com.projetmobile.mobile.data.repository.games.GamesRepository

class FakeGamesRepository(
    initialPage: PagedResult<GameListItem> = sampleGamesPage(),
    initialGame: GameDetail = sampleGameDetail(),
) : GamesRepository {
    var getGamesCalls: Int = 0
    var getGamesHandler: (suspend (GameFilters, Int, Int) -> Result<PagedResult<GameListItem>>)? = null
    var lastFilters: GameFilters? = null
    var lastPage: Int? = null
    var lastLimit: Int? = null
    val pageResults: MutableMap<Int, Result<PagedResult<GameListItem>>> = mutableMapOf(
        1 to Result.success(initialPage),
    )

    var getGameCalls: Int = 0
    var lastRequestedGameId: Int? = null
    var getGameResult: Result<GameDetail> = Result.success(initialGame)

    var gameTypesResult: Result<List<GameTypeOption>> = Result.success(
        listOf(
            sampleGameTypeOption("Experts"),
            sampleGameTypeOption("Ambiance"),
        ),
    )
    var editorsResult: Result<List<EditorOption>> = Result.success(
        listOf(sampleEditorOption()),
    )
    var mechanismsResult: Result<List<MechanismOption>> = Result.success(
        listOf(sampleMechanismOption()),
    )

    var createGameCalls: Int = 0
    var lastCreatedDraft: GameDraft? = null
    var createGameResult: Result<GameDetail> = Result.success(initialGame)

    var updateGameCalls: Int = 0
    var lastUpdatedGameId: Int? = null
    var lastUpdatedDraft: GameDraft? = null
    var updateGameResult: Result<GameDetail> = Result.success(initialGame)

    var deleteGameCalls: Int = 0
    var lastDeletedGameId: Int? = null
    var deleteGameResult: Result<String> = Result.success("Jeu supprimé.")

    var uploadGameImageCalls: Int = 0
    var lastUploadedFileName: String? = null
    var lastUploadedMimeType: String? = null
    var lastUploadedBytes: ByteArray? = null
    var uploadGameImageResult: Result<String> = Result.success("/uploads/games/test.png")

    override suspend fun getGames(
        filters: GameFilters,
        page: Int,
        limit: Int,
    ): Result<PagedResult<GameListItem>> {
        getGamesCalls += 1
        lastFilters = filters
        lastPage = page
        lastLimit = limit
        getGamesHandler?.let { handler ->
            return handler(filters, page, limit)
        }
        return pageResults[page]
            ?: Result.success(PagedResult(emptyList(), page = page, limit = limit, total = 0, hasNext = false))
    }

    override suspend fun getGame(gameId: Int): Result<GameDetail> {
        getGameCalls += 1
        lastRequestedGameId = gameId
        return getGameResult
    }

    override suspend fun getGameTypes(): Result<List<GameTypeOption>> = gameTypesResult

    override suspend fun getEditors(): Result<List<EditorOption>> = editorsResult

    override suspend fun getMechanisms(): Result<List<MechanismOption>> = mechanismsResult

    override suspend fun createGame(draft: GameDraft): Result<GameDetail> {
        createGameCalls += 1
        lastCreatedDraft = draft
        return createGameResult
    }

    override suspend fun updateGame(
        gameId: Int,
        draft: GameDraft,
    ): Result<GameDetail> {
        updateGameCalls += 1
        lastUpdatedGameId = gameId
        lastUpdatedDraft = draft
        return updateGameResult
    }

    override suspend fun deleteGame(gameId: Int): Result<String> {
        deleteGameCalls += 1
        lastDeletedGameId = gameId
        return deleteGameResult
    }

    override suspend fun uploadGameImage(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Result<String> {
        uploadGameImageCalls += 1
        lastUploadedFileName = fileName
        lastUploadedMimeType = mimeType
        lastUploadedBytes = bytes
        return uploadGameImageResult
    }
}

fun sampleGameTypeOption(value: String = "Experts") = GameTypeOption(value = value)

fun sampleEditorOption(
    id: Int = 9,
    name: String = "Super Meeple",
) = EditorOption(
    id = id,
    name = name,
    email = "contact@supermeeple.com",
    website = "https://example.com",
    description = "Editeur test",
    logoUrl = null,
    isExhibitor = true,
    isDistributor = false,
)

fun sampleMechanismOption(
    id: Int = 4,
    name: String = "Deck building",
) = MechanismOption(
    id = id,
    name = name,
    description = "Mécanisme test",
)

fun sampleGameListItem(
    id: Int = 1,
    title: String = "Akropolis",
    mechanisms: List<MechanismOption> = listOf(sampleMechanismOption()),
) = GameListItem(
    id = id,
    title = title,
    type = "Experts",
    editorId = 9,
    editorName = "Super Meeple",
    minAge = 12,
    authors = "Designer",
    minPlayers = 1,
    maxPlayers = 4,
    prototype = false,
    durationMinutes = 45,
    theme = "Ville",
    description = "Description test",
    imageUrl = "/uploads/games/$id.png",
    rulesVideoUrl = "https://www.youtube.com/watch?v=test$id",
    mechanisms = mechanisms,
)

fun sampleGameDetail(
    id: Int = 1,
    title: String = "Akropolis",
    mechanisms: List<MechanismOption> = listOf(sampleMechanismOption()),
) = GameDetail(
    id = id,
    title = title,
    type = "Experts",
    editorId = 9,
    editorName = "Super Meeple",
    minAge = 12,
    authors = "Designer",
    minPlayers = 1,
    maxPlayers = 4,
    prototype = false,
    durationMinutes = 45,
    theme = "Ville",
    description = "Description test",
    imageUrl = "/uploads/games/$id.png",
    rulesVideoUrl = "https://www.youtube.com/watch?v=test$id",
    mechanisms = mechanisms,
)

fun sampleGamesPage(
    items: List<GameListItem> = listOf(sampleGameListItem()),
    page: Int = 1,
    limit: Int = 20,
    total: Int = items.size,
    hasNext: Boolean = false,
) = PagedResult(
    items = items,
    page = page,
    limit = limit,
    total = total,
    hasNext = hasNext,
)
