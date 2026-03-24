package com.projetmobile.mobile.data.mapper.festival

import android.os.Build
import androidx.annotation.RequiresApi
import com.projetmobile.mobile.data.entity.festival.FestivalSummary
import com.projetmobile.mobile.data.remote.festival.FestivalDto

import com.projetmobile.mobile.ui.utils.formatDate

/**
 * Convertit un FestivalDto (couche réseau) en FestivalSummary (couche métier/UI).
 * Les dates ISO 8601 sont formatées en dd/MM/yyyy dès le mapping.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun FestivalDto.toFestivalSummary(): FestivalSummary = FestivalSummary(
    id = id ?: -1,
    name = name,
    startDate = formatDate(startDate),   // format dd/mm//yyyy
    endDate = formatDate(endDate),
    stockTablesStandard = stockTablesStandard,
    stockTablesGrande = stockTablesGrande,
    stockTablesMairie = stockTablesMairie,
    stockChaises = stockChaises,
    prixPrises = prixPrises,
)
