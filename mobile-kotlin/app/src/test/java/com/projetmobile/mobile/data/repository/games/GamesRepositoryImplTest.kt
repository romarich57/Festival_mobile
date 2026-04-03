package com.projetmobile.mobile.data.repository.games

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamesRepositoryImplTest {

    @Test
    fun getGames_forwardsFiltersAndServerPagination() = runTest {
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
        val repository = GamesRepositoryImpl(service)

        val result = repository.getGames(
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
    fun getGames_derivesHasNextWhenCurrentPageIsBeforeLastPage() = runTest {
        val repository = GamesRepositoryImpl(
            FakeGamesApiService(
                gamesPage = GamesPageResponseDto(
                    items = listOf(gameDto(id = 1, title = "Akropolis")),
                    pagination = GamesPaginationDto(
                        page = 1,
                        limit = 20,
                        total = 40,
                        totalPages = 2,
                        sortBy = "title",
                        sortOrder = "asc",
                    ),
                ),
            ),
        )

        val page = repository.getGames(GameFilters(), page = 1, limit = 20).getOrThrow()

        assertTrue(page.hasNext)
    }

    @Test
    fun getGames_derivesNoNextPageWhenCurrentPageMatchesLastPage() = runTest {
        val repository = GamesRepositoryImpl(
            FakeGamesApiService(
                gamesPage = GamesPageResponseDto(
                    items = listOf(gameDto(id = 1, title = "Akropolis")),
                    pagination = GamesPaginationDto(
                        page = 2,
                        limit = 20,
                        total = 40,
                        totalPages = 2,
                        sortBy = "title",
                        sortOrder = "asc",
                    ),
                ),
            ),
        )

        val page = repository.getGames(GameFilters(), page = 2, limit = 20).getOrThrow()

        assertTrue(!page.hasNext)
    }

    @Test
    fun getGames_derivesNoNextPageForEmptyResults() = runTest {
        val repository = GamesRepositoryImpl(
            FakeGamesApiService(
                gamesPage = GamesPageResponseDto(
                    items = emptyList(),
                    pagination = GamesPaginationDto(
                        page = 1,
                        limit = 20,
                        total = 0,
                        totalPages = 0,
                        sortBy = "title",
                        sortOrder = "asc",
                    ),
                ),
            ),
        )

        val page = repository.getGames(GameFilters(), page = 1, limit = 20).getOrThrow()

        assertTrue(!page.hasNext)
        assertTrue(page.items.isEmpty())
    }

    @Test
    fun getGameTypes_mapsServerValues() = runTest {
        val repository = GamesRepositoryImpl(
            FakeGamesApiService(
                gameTypes = listOf(
                    "Ambiance",
                    "Experts",
                ),
            ),
        )

        val lookups = repository.getGameTypes().getOrThrow()

        assertEquals(2, lookups.size)
        assertEquals("Ambiance", lookups.first().value)
        assertEquals("Experts", lookups.last().value)
    }

    @Test
    fun uploadGameImage_returnsUploadedUrl() = runTest {
        val repository = GamesRepositoryImpl(FakeGamesApiService())

        val result = repository.uploadGameImage(
            fileName = "akropolis.png",
            mimeType = "image/png",
            bytes = byteArrayOf(1, 2, 3),
        )

        val uploadUrl = result.getOrThrow()
        assertEquals("/uploads/games/test.png", uploadUrl)
    }
}

private class FakeGamesApiService(
    private val gamesPage: GamesPageResponseDto = GamesPageResponseDto(
        items = emptyList(),
        pagination = GamesPaginationDto(
            page = 1,
            limit = 20,
            total = 0,
            totalPages = 0,
            sortBy = "title",
            sortOrder = "asc",
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
        page: Int,
        limit: Int,
        title: String?,
        type: String?,
        editorId: Int?,
        minAge: Int?,
        sort: String,
    ): GamesPageResponseDto {
        lastPage = page
        lastLimit = limit
        lastTitle = title
        lastType = type
        lastEditorId = editorId
        lastMinAge = minAge
        lastSort = sort
        return gamesPage
    }

    override suspend fun getGameTypes(): List<String> = gameTypes

    override suspend fun getGame(gameId: Int): GameDto = gameDto(id = gameId, title = "Game $gameId")

    override suspend fun createGame(request: GameUpsertRequestDto): GameDto {
        return gameDto(id = 99, title = request.title)
    }

    override suspend fun updateGame(
        gameId: Int,
        request: GameUpsertRequestDto,
    ): GameDto {
        return gameDto(id = gameId, title = request.title)
    }

    override suspend fun deleteGame(gameId: Int): DeleteGameResponseDto {
        return DeleteGameResponseDto(message = "deleted:$gameId")
    }

    override suspend fun getEditors(): List<EditorDto> = emptyList()

    override suspend fun getMechanisms(): List<MechanismDto> = emptyList()

    override suspend fun uploadGameImage(image: MultipartBody.Part): UploadGameImageResponseDto {
        return UploadGameImageResponseDto(
            url = "/uploads/games/test.png",
            message = "ok",
        )
    }
}

private fun gameDto(
    id: Int,
    title: String,
) = GameDto(
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
    durationMinutes = 90,
    theme = null,
    description = null,
    imageUrl = null,
    rulesVideoUrl = null,
    mechanisms = emptyList(),
)
