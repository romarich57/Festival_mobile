package com.projetmobile.mobile.data.mapper.festival

import android.os.Build
import androidx.annotation.RequiresApi
import com.projetmobile.mobile.data.entity.festival.FestivalSummary
import com.projetmobile.mobile.data.remote.festival.FestivalDto

import com.projetmobile.mobile.ui.utils.formatDate

/**
 * Rôle : Convertit un `FestivalDto` (couche réseau) en `FestivalSummary` (couche métier/UI).
 * Les dates ISO 8601 sont formatées en dd/MM/yyyy dès le mapping.
 * 
 * Précondition : Le DTO doit contenir des informations de festival (dates valides, nom, etc.). L'API Android O (min SDK) est requise.
 * Postcondition : Retourne un objet de type `FestivalSummary` prêt à être affiché côté UI.
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
