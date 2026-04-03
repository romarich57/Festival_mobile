package com.projetmobile.mobile.data.remote.games

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface GamesApiService {
    @GET("games")
    suspend fun getGames(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("title") title: String? = null,
        @Query("type") type: String? = null,
        @Query("editor_id") editorId: Int? = null,
        @Query("min_age") minAge: Int? = null,
        @Query("sort") sort: String,
    ): GamesPageResponseDto

    @GET("games/types")
    suspend fun getGameTypes(): List<String>

    @GET("games/{id}")
    suspend fun getGame(@Path("id") gameId: Int): GameDto

    @POST("games")
    suspend fun createGame(@Body request: GameUpsertRequestDto): GameDto

    @PUT("games/{id}")
    suspend fun updateGame(
        @Path("id") gameId: Int,
        @Body request: GameUpsertRequestDto,
    ): GameDto

    @DELETE("games/{id}")
    suspend fun deleteGame(@Path("id") gameId: Int): DeleteGameResponseDto

    @GET("editors")
    suspend fun getEditors(): List<EditorDto>

    @GET("mechanisms")
    suspend fun getMechanisms(): List<MechanismDto>

    @Multipart
    @POST("upload/game-image")
    suspend fun uploadGameImage(@Part image: MultipartBody.Part): UploadGameImageResponseDto
}
