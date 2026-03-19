package com.projetmobile.mobile.data.mapper.festival

import com.projetmobile.mobile.data.entity.festival.FestivalSummary
import com.projetmobile.mobile.data.remote.festival.FestivalDto

fun FestivalDto.toFestivalSummary(): FestivalSummary = FestivalSummary(
    id = id,
    name = name,
    startDate = startDate,
    endDate = endDate,
    stockTablesStandard = stockTablesStandard,
    stockTablesGrande = stockTablesGrande,
    stockTablesMairie = stockTablesMairie,
    stockChaises = stockChaises,
    prixPrises = prixPrises,
)
