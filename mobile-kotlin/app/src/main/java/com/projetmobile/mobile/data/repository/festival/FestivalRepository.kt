package com.projetmobile.mobile.data.repository.festival

import com.projetmobile.mobile.data.entity.festival.FestivalSummary
import com.projetmobile.mobile.data.remote.festival.FestivalDto

interface FestivalRepository {
    suspend fun getFestivals(): Result<List<FestivalSummary>>
    suspend fun getFestival(id: Int): Result<FestivalSummary>
    suspend fun addFestival(festival: FestivalDto): Result<FestivalDto>
    suspend fun deleteFestival(id: Int): Result<Unit>
}
