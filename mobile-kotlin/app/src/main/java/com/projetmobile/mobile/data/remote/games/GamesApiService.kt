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

/**
 * Rôle : Contrat réseau liant l'application Android à divers endpoints API gérant 
 * la ressource "Jeu" (games), ses types, éditeurs et mécanismes associés au sein 
 * d'un évènement.
 * 
 * Précondition : Appels instanciés via une interface Retrofit avec une baseUrl correcte.
 * Postcondition : Couverture quasi-complète des méthodes CRUD (Create-Read-Update-Delete) des jeux.
 */
interface GamesApiService {
    
    /**
     * Rôle : Interroge sous forme paginée la liste des jeux présents en BDD serveur 
     * en incluant des filtres complexes optionnels de requête (Queries).
     * 
     * Précondition : Fournir obligatoirement 'page', 'limit' et 'sort'. Les autres filtres peuvent être ignorés (null).
     * Postcondition : Renvoie un objet [GamesPageResponseDto] encapsulant la liste des jeux et les méta-données.
     */
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

    /**
     * Rôle : Récupère côté serveur les types valides d'un jeu (Ex: Famille, Expert, etc.).
     * 
     * Précondition : Connecté à Internet.
     * Postcondition : Une liste de String représentant les énumérations côté backend pour le type.
     */
    @GET("games/types")
    suspend fun getGameTypes(): List<String>

    /**
     * Rôle : Demande minutieuse des attibuts restants d'un jeu individuel (id).
     * 
     * Précondition : Identifier son id.
     * Postcondition : Profil unitaire transformé en [GameDto].
     */
    @GET("games/{id}")
    suspend fun getGame(@Path("id") gameId: Int): GameDto

    /**
     * Rôle : Ordre d'ajout (`POST`) afin de déclarer un nouveau jeu.
     * 
     * Précondition : Un [GameUpsertRequestDto] bien structuré encapsulant toutes les métadonnées.
     * Postcondition : Revoit le nouveau Jeu contenant l'ID attribué côté hôte.
     */
    @POST("games")
    suspend fun createGame(@Body request: GameUpsertRequestDto): GameDto

    /**
     * Rôle : Modifie via (`PUT`) un jeu préalablement saisi/partagé.
     * 
     * Précondition : `gameId` cible valide, body modifiant une entité compatible.
     * Postcondition : Retourne la version actualisée sauvegardée du jeu.
     */
    @PUT("games/{id}")
    suspend fun updateGame(
        @Path("id") gameId: Int,
        @Body request: GameUpsertRequestDto,
    ): GameDto

    /**
     * Rôle : Demande formellement via `DELETE` d'éliminer la trace d'un jeu ciblé par son ID.
     * 
     * Précondition : L'Id du jeu à retirer.
     * Postcondition : Message sous la forme d'un [DeleteGameResponseDto].
     */
    @DELETE("games/{id}")
    suspend fun deleteGame(@Path("id") gameId: Int): DeleteGameResponseDto

    /**
     * Rôle : Fournit un tableau de tous les éditeurs pour associer ultérieurement un jeu à un créateur.
     * 
     * Précondition : Endpoint atteignable.
     * Postcondition : Converti en liste d'objets [EditorDto].
     */
    @GET("editors")
    suspend fun getEditors(): List<EditorDto>

    /**
     * Rôle : Liste les mécanismes de jeu possibles (Placement de dé, cartes, etc.) gérés par le serveur.
     * 
     * Précondition : API à jour.
     * Postcondition : Collection format [MechanismDto].
     */
    @GET("mechanisms")
    suspend fun getMechanisms(): List<MechanismDto>

    /**
     * Rôle : Autorise le téléversement (Upload File) de type multiformat (Multipart)
     * consistant à glisser une image d'illustration du jeu.
     * 
     * Précondition : Requête annotée '@Multipart' contenant une partie [MultipartBody.Part].
     * Postcondition : L'image réside sur l'espace static serveur ; l'API retourne l'URL d'accès public via [UploadGameImageResponseDto].
     */
    @Multipart
    @POST("upload/game-image")
    suspend fun uploadGameImage(@Part image: MultipartBody.Part): UploadGameImageResponseDto
}
