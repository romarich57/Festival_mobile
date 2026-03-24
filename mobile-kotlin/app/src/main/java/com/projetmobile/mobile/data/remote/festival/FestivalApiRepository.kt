package com.projetmobile.mobile.data.remote.festival

class FestivalApiRepository(private val apiService: FestivalApiService) {
    // Role : Récupérer tous les festivals depuis l'API.
    suspend fun getFestivals(): Result<List<FestivalDto>> = runCatching {
        apiService.getFestivals()
    }
}