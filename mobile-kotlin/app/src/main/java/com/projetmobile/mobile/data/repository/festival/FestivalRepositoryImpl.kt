package com.projetmobile.mobile.data.repository.festival

import com.projetmobile.mobile.data.mapper.festival.toFestivalSummary
import com.projetmobile.mobile.data.remote.festival.FestivalApiService
import com.projetmobile.mobile.data.entity.festival.FestivalSummary
import com.projetmobile.mobile.data.repository.runRepositoryCall

class FestivalRepositoryImpl(
    private val festivalApiService: FestivalApiService,
) : FestivalRepository {
    override suspend fun getFestivals(): Result<List<FestivalSummary>> {
        return runRepositoryCall(defaultMessage = "Impossible de récupérer les festivals.") {
            festivalApiService.getFestivals().map { festival -> festival.toFestivalSummary() }
        }
    }
}
