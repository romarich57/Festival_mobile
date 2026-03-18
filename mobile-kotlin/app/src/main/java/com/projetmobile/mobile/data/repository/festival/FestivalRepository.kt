package com.projetmobile.mobile.data.repository.festival

import com.projetmobile.mobile.data.entity.festival.FestivalSummary

interface FestivalRepository {
    suspend fun getFestivals(): Result<List<FestivalSummary>>
}
