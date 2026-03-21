package com.projetmobile.mobile.data.repository.games

import com.projetmobile.mobile.data.entity.games.EditorOption
import com.projetmobile.mobile.data.entity.games.GameDetail
import com.projetmobile.mobile.data.entity.games.GameDraft
import com.projetmobile.mobile.data.entity.games.GameFilters
import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.games.GameTypeOption
import com.projetmobile.mobile.data.entity.games.MechanismOption
import com.projetmobile.mobile.data.entity.games.PagedResult

interface GamesRepository {
    suspend fun getGames(
        filters: GameFilters,
        page: Int,
        limit: Int,
    ): Result<PagedResult<GameListItem>>

    suspend fun getGame(gameId: Int): Result<GameDetail>

    suspend fun getGameTypes(): Result<List<GameTypeOption>>

    suspend fun getEditors(): Result<List<EditorOption>>

    suspend fun getMechanisms(): Result<List<MechanismOption>>

    suspend fun createGame(draft: GameDraft): Result<GameDetail>

    suspend fun updateGame(
        gameId: Int,
        draft: GameDraft,
    ): Result<GameDetail>

    suspend fun deleteGame(gameId: Int): Result<String>

    suspend fun uploadGameImage(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Result<String>
}
