package com.projetmobile.mobile.data.remote.festival

import com.projetmobile.mobile.data.remote.festival.FestivalDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Rôle : Interface réseau Retrofit régissant la communication avec les points d'entrée
 * 'festivals' de l'API backend.
 * 
 * Précondition : Nécessite une configuration valide de Retrofit côté DI.
 * Postcondition : Offre la récupération et la manipulation distante de la ressource Festival.
 */
interface FestivalApiService {
    
    /**
     * Rôle : Récupère la liste complète des festivals gérés par l'API.
     * 
     * Précondition : Authentification suffisante selon la route serveur (via Cookie JWT).
     * Postcondition : Retourne un tableau d'objets structurés de type [FestivalDto].
     */
    @GET("festivals")
    suspend fun getFestivals(): List<FestivalDto>

    /**
     * Rôle : Obtient en détail un festival en particulier à l'aide de son ID unique.
     * 
     * Précondition : Connaître l'ID distant existant.
     * Postcondition : Renvoie le [FestivalDto] ciblé.
     */
    @GET("festivals/{id}")
    suspend fun getFestival(@Path("id") id: Int): FestivalDto

    /**
     * Rôle : Soumet un nouveau festival fraîchement créé et le sauvegarde sur le serveur central.
     * 
     * Précondition : Formulaire valide mappé dans un [FestivalDto].
     * Postcondition : Enregistre en BD et retourne le festival créé avec son nouvel ID défini via [CreateFestivalResponseDto].
     */
    @POST("festivals")
    suspend fun addFestival(@Body festival: FestivalDto): CreateFestivalResponseDto

    /**
     * Rôle : Ordonne par appel HTTP la suppression d'un festival.
     * 
     * Précondition : ID distant fourni et rôle Administrateur ou habilité.
     * Postcondition : Le festival est révoqué définitivement des données côté serveur.
     */
    @DELETE("festivals/{id}")
    suspend fun deleteFestival(@Path("id") id: Int)
}
