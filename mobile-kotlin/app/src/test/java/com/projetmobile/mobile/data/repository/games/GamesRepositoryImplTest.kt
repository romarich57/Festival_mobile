package com.projetmobile.mobile.data.repository.games

import android.content.Context
import com.projetmobile.mobile.data.dao.GameDao
import com.projetmobile.mobile.data.database.SyncPreferenceStore
import com.projetmobile.mobile.data.entity.games.GameFilters
import com.projetmobile.mobile.data.entity.games.GameSort
import com.projetmobile.mobile.data.remote.games.DeleteGameResponseDto
import com.projetmobile.mobile.data.remote.games.GameDto
import com.projetmobile.mobile.data.remote.games.EditorDto
import com.projetmobile.mobile.data.remote.games.GamesApiService
import com.projetmobile.mobile.data.remote.games.GamesPaginationDto
import com.projetmobile.mobile.data.remote.games.GamesPageResponseDto
import com.projetmobile.mobile.data.remote.games.GameUpsertRequestDto
import com.projetmobile.mobile.data.remote.games.MechanismDto
import com.projetmobile.mobile.data.remote.games.UploadGameImageResponseDto
import com.projetmobile.mobile.data.room.GameRoomEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class GamesRepositoryImplTest {

    private fun buildRepository(service: FakeGamesApiService): GamesRepositoryImpl {
        return GamesRepositoryImpl(
            gamesApiService = service,
            gameDao = FakeGameDao(),
            syncPreferenceStore = FakeSyncPreferenceStore(),
            context = mock<Context>(),
        )
    }

    @Test
    fun refreshGames_forwardsFiltersAndServerPagination() = runTest {
        val service = FakeGamesApiService(
            gamesPage = GamesPageResponseDto(
                items = listOf(
                    gameDto(id = 1, title = "Akropolis"),
                    gameDto(id = 2, title = "Ark Nova"),
                ),
                pagination = GamesPaginationDto(
                    page = 2,
                    limit = 1,
                    total = 2,
                    totalPages = 2,
                    sortBy = "title",
                    sortOrder = "desc",
                ),
            ),
        )
        val repository = buildRepository(service)

        val result = repository.refreshGames(
            filters = GameFilters(
                title = "Ark",
                type = "Experts",
                editorId = 9,
                minAge = 12,
                sort = GameSort.TitleDesc,
            ),
            page = 2,
            limit = 1,
        )

        val page = result.getOrThrow()
        assertEquals(2, service.lastPage)
        assertEquals(1, service.lastLimit)
        assertEquals("Ark", service.lastTitle)
        assertEquals("Experts", service.lastType)
        assertEquals(9, service.lastEditorId)
        assertEquals(12, service.lastMinAge)
        assertEquals("title_desc", service.lastSort)
        assertEquals(2, page.page)
        assertEquals(1, page.limit)
        assertEquals(2, page.total)
        assertTrue(!page.hasNext)
        assertEquals(2, page.items.size)
        assertEquals("Akropolis", page.items.first().title)
    }

    @Test
    fun refreshGames_derivesHasNextWhenCurrentPageIsBeforeLastPage() = runTest {
        val repository = buildRepository(
            FakeGamesApiService(
                gamesPage = GamesPageResponseDto(
                    items = listOf(gameDto(id = 1, title = "Akropolis")),
                    pagination = GamesPaginationDto(
                        page = 1, limit = 20, total = 40, totalPages = 2,
                        sortBy = "title", sortOrder = "asc",
                    ),
                ),
            ),
        )

        val page = repository.refreshGames(GameFilters(), page = 1, limit = 20).getOrThrow()

        assertTrue(page.hasNext)
    }

    @Test
    fun refreshGames_derivesNoNextPageWhenCurrentPageMatchesLastPage() = runTest {
        val repository = buildRepository(
            FakeGamesApiService(
                gamesPage = GamesPageResponseDto(
                    items = listOf(gameDto(id = 1, title = "Akropolis")),
                    pagination = GamesPaginationDto(
                        page = 2, limit = 20, total = 40, totalPages = 2,
                        sortBy = "title", sortOrder = "asc",
                    ),
                ),
            ),
        )

        val page = repository.refreshGames(GameFilters(), page = 2, limit = 20).getOrThrow()

        assertTrue(!page.hasNext)
    }

    @Test
    fun refreshGames_derivesNoNextPageForEmptyResults() = runTest {
        val repository = buildRepository(
            FakeGamesApiService(
                gamesPage = GamesPageResponseDto(
                    items = emptyList(),
                    pagination = GamesPaginationDto(
                        page = 1, limit = 20, total = 0, totalPages = 0,
                        sortBy = "title", sortOrder = "asc",
                    ),
                ),
            ),
        )

        val page = repository.refreshGames(GameFilters(), page = 1, limit = 20).getOrThrow()

        assertTrue(!page.hasNext)
        assertTrue(page.items.isEmpty())
    }

    @Test
    fun getGameTypes_mapsServerValues() = runTest {
        val repository = buildRepository(
            FakeGamesApiService(gameTypes = listOf("Ambiance", "Experts")),
        )

        val lookups = repository.getGameTypes().getOrThrow()

        assertEquals(2, lookups.size)
        assertEquals("Ambiance", lookups.first().value)
        assertEquals("Experts", lookups.last().value)
    }

    @Test
    fun uploadGameImage_returnsUploadedUrl() = runTest {
        val repository = buildRepository(FakeGamesApiService())

        val result = repository.uploadGameImage(
            fileName = "akropolis.png",
            mimeType = "image/png",
            bytes = byteArrayOf(1, 2, 3),
        )

        val uploadUrl = result.getOrThrow()
        assertEquals("/uploads/games/test.png", uploadUrl)
    }
}

// ── Fakes ────────────────────────────────────────────────────────────────────

private class FakeGameDao : GameDao {
    private val store = MutableStateFlow<List<GameRoomEntity>>(emptyList())

    override fun observeAll(): Flow<List<GameRoomEntity>> = store
    override fun observeByTitle(search: String): Flow<List<GameRoomEntity>> = store
    override fun observeById(id: Int): Flow<GameRoomEntity?> = MutableStateFlow(null)
    override suspend fun getById(id: Int): GameRoomEntity? = null
    override suspend fun getPending(): List<GameRoomEntity> = emptyList()
    override suspend fun upsertAll(games: List<GameRoomEntity>) {
        store.value = games
    }
    override suspend fun upsert(game: GameRoomEntity) {
        store.value = store.value + game
    }
    override suspend fun deleteById(id: Int) {
        store.value = store.value.filter { it.id != id }
    }
    override suspend fun markForDeletion(id: Int) {}
    override suspend fun updateSyncStatus(id: Int, status: String) {}
}

private class FakeSyncPreferenceStore : SyncPreferenceStore(mock<android.content.Context>()) {
    override suspend fun getLastSyncedAt(key: String): Long? = null
    override suspend fun setLastSyncedAt(key: String, timestamp: Long) {}
    override suspend fun needsRefresh(key: String, ttlMs: Long): Boolean = true
    override suspend fun invalidate(key: String) {}
}

private class FakeGamesApiService(
    private val gamesPage: GamesPageResponseDto = GamesPageResponseDto(
        items = emptyList(),
        pagination = GamesPaginationDto(
            page = 1, limit = 20, total = 0, totalPages = 0,
            sortBy = "title", sortOrder = "asc",
        ),
    ),
    private val gameTypes: List<String> = emptyList(),
) : GamesApiService {
    var lastPage: Int? = null
    var lastLimit: Int? = null
    var lastTitle: String? = null
    var lastType: String? = null
    var lastEditorId: Int? = null
    var lastMinAge: Int? = null
    var lastSort: String? = null

    override suspend fun getGames(
        page: Int, limit: Int, title: String?, type: String?,
        editorId: Int?, minAge: Int?, sort: String,
    ): GamesPageResponseDto {
        lastPage = page; lastLimit = limit; lastTitle = title
        lastType = type; lastEditorId = editorId; lastMinAge = minAge; lastSort = sort
        return gamesPage
    }

    override suspend fun getGameTypes(): List<String> = gameTypes
    override suspend fun getGame(gameId: Int): GameDto = gameDto(id = gameId, title = "Game $gameId")
    override suspend fun createGame(request: GameUpsertRequestDto): GameDto =
        gameDto(id = 99, title = request.title)
    override suspend fun updateGame(gameId: Int, request: GameUpsertRequestDto): GameDto =
        gameDto(id = gameId, title = request.title)
    override suspend fun deleteGame(gameId: Int): DeleteGameResponseDto =
        DeleteGameResponseDto(message = "deleted:$gameId")
    override suspend fun getEditors(): List<EditorDto> = emptyList()
    override suspend fun getMechanisms(): List<MechanismDto> = emptyList()
    override suspend fun uploadGameImage(image: MultipartBody.Part): UploadGameImageResponseDto =
        UploadGameImageResponseDto(url = "/uploads/games/test.png", message = "ok")
}

private fun gameDto(id: Int, title: String) = GameDto(
    id = id, title = title, type = "Experts", editorId = 9, editorName = "Super Meeple",
    minAge = 12, authors = "Designer", minPlayers = 1, maxPlayers = 4, prototype = false,
    durationMinutes = 90, theme = null, description = null, imageUrl = null,
    rulesVideoUrl = null, mechanisms = emptyList(),
)
