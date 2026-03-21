package com.projetmobile.mobile.data.repository.games

import com.projetmobile.mobile.data.entity.games.GameDraft
import com.projetmobile.mobile.data.entity.games.GameFilters
import com.projetmobile.mobile.data.mapper.games.toEditorOption
import com.projetmobile.mobile.data.mapper.games.toGameDetail
import com.projetmobile.mobile.data.mapper.games.toGameListPage
import com.projetmobile.mobile.data.mapper.games.toGameTypeOption
import com.projetmobile.mobile.data.mapper.games.toMechanismOption
import com.projetmobile.mobile.data.remote.games.GamesApiService
import com.projetmobile.mobile.data.remote.games.toRequestDto
import com.projetmobile.mobile.data.repository.runRepositoryCall
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class GamesRepositoryImpl(
    private val gamesApiService: GamesApiService,
) : GamesRepository {

    override suspend fun getGames(
        filters: GameFilters,
        page: Int,
        limit: Int,
    ) = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les jeux.",
    ) {
        gamesApiService.getGames(
            page = page,
            limit = limit,
            title = filters.title.trim().takeIf { it.isNotEmpty() },
            type = filters.type?.trim()?.takeIf { it.isNotEmpty() },
            editorId = filters.editorId,
            minAge = filters.minAge,
            sort = filters.sort.apiValue,
        ).toGameListPage()
    }

    override suspend fun getGame(gameId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de récupérer le jeu.",
    ) {
        gamesApiService.getGame(gameId).toGameDetail()
    }

    override suspend fun getGameTypes() = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les types de jeux.",
    ) {
        gamesApiService.getGameTypes()
            .map { it.toGameTypeOption() }
    }

    override suspend fun getEditors() = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les éditeurs.",
    ) {
        gamesApiService.getEditors()
            .map { it.toEditorOption() }
    }

    override suspend fun getMechanisms() = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les mécanismes.",
    ) {
        gamesApiService.getMechanisms()
            .map { it.toMechanismOption() }
    }

    override suspend fun createGame(draft: GameDraft) = runRepositoryCall(
        defaultMessage = "Impossible de créer le jeu.",
    ) {
        gamesApiService.createGame(draft.toRequestDto()).toGameDetail()
    }

    override suspend fun updateGame(
        gameId: Int,
        draft: GameDraft,
    ) = runRepositoryCall(
        defaultMessage = "Impossible de mettre à jour le jeu.",
    ) {
        gamesApiService.updateGame(
            gameId = gameId,
            request = draft.toRequestDto(),
        ).toGameDetail()
    }

    override suspend fun deleteGame(gameId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de supprimer le jeu.",
    ) {
        gamesApiService.deleteGame(gameId).message
    }

    override suspend fun uploadGameImage(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ) = runRepositoryCall(
        defaultMessage = "Impossible d'envoyer l'image du jeu.",
    ) {
        val requestBody = bytes.toRequestBody(mimeType.toMediaType())
        val imagePart = MultipartBody.Part.createFormData(
            name = "image",
            filename = fileName,
            body = requestBody,
        )
        gamesApiService.uploadGameImage(imagePart).url
    }
}
