package com.projetmobile.mobile.ui.screens.reservationDetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.remote.reservation.ReservationUpdatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.ReservationZoneUpdateDto
import com.projetmobile.mobile.data.repository.festival.FestivalRepository
import com.projetmobile.mobile.data.repository.reservation.ReservationRepository
import kotlinx.coroutines.launch

class ReservationTarifaireViewModel(
    private val reservationRepository: ReservationRepository,
    private val festivalRepository: FestivalRepository,
) : ViewModel() {

    var uiState: ReservationTarifaireUiState by mutableStateOf(ReservationTarifaireUiState.Loading)
        private set

    fun loadReservation(reservationId: Int) {
        viewModelScope.launch {
            uiState = ReservationTarifaireUiState.Loading
            try {
                uiState = fetchState(reservationId)
            } catch (e: Exception) {
                uiState = ReservationTarifaireUiState.Error("Erreur reseau : ${e.message}")
            }
        }
    }

    fun onZoneTablesChanged(zoneId: Int, value: String) {
        val current = uiState as? ReservationTarifaireUiState.Success ?: return
        val sanitized = sanitizeIntInput(value)
        uiState = current.copy(
            zones = current.zones.map { zone ->
                if (zone.id == zoneId) zone.copy(reservedTables = sanitized) else zone
            }
        )
    }

    fun onNbPrisesChanged(value: String) {
        val current = uiState as? ReservationTarifaireUiState.Success ?: return
        uiState = current.copy(nbPrises = sanitizeIntInput(value))
    }

    fun onTableDiscountChanged(value: String) {
        val current = uiState as? ReservationTarifaireUiState.Success ?: return
        uiState = current.copy(tableDiscountOffered = sanitizeDecimalInput(value))
    }

    fun onDirectDiscountChanged(value: String) {
        val current = uiState as? ReservationTarifaireUiState.Success ?: return
        uiState = current.copy(directDiscount = sanitizeDecimalInput(value))
    }

    fun onNoteChanged(value: String) {
        val current = uiState as? ReservationTarifaireUiState.Success ?: return
        uiState = current.copy(note = value)
    }

    fun clearMessage() {
        val current = uiState as? ReservationTarifaireUiState.Success ?: return
        uiState = current.copy(userMessage = null)
    }

    fun saveReservation(reservationId: Int) {
        val current = uiState as? ReservationTarifaireUiState.Success ?: return
        viewModelScope.launch {
            uiState = current.copy(isSaving = true, userMessage = null)
            try {
                val summary = computeSummary(current)
                val payload = ReservationUpdatePayloadDto(
                    startPrice = summary.startPrice,
                    nbPrises = summary.nbPrises,
                    finalPrice = summary.finalPrice,
                    tableDiscountOffered = summary.tableDiscountOffered,
                    directDiscount = summary.directDiscount,
                    note = current.note.takeIf { it.isNotBlank() },
                    zonesTarifaires = summary.zonePayloads,
                )
                reservationRepository.updateReservation(reservationId, payload)
                uiState = fetchState(reservationId).copy(userMessage = "Enregistre avec succes")
            } catch (e: Exception) {
                val latest = uiState as? ReservationTarifaireUiState.Success
                if (latest != null) {
                    uiState = latest.copy(isSaving = false, userMessage = "Erreur de sauvegarde")
                } else {
                    uiState = ReservationTarifaireUiState.Error("Erreur reseau : ${e.message}")
                }
            }
        }
    }

    private suspend fun fetchState(reservationId: Int): ReservationTarifaireUiState.Success {
        val reservationDetails = reservationRepository.getReservationDetails(reservationId)
        val zones = reservationRepository.getZonesTarifaires(reservationDetails.festivalId)
        val festivalSummary = festivalRepository.getFestival(reservationDetails.festivalId).getOrNull()
        val prixPrises = festivalSummary?.prixPrises ?: 0.0

        val reservedByZone = reservationDetails.zonesTarifaires.associateBy { it.zoneTarifaireId }
        val zoneForms = zones.map { zone ->
            val reserved = reservedByZone[zone.id]?.nbTablesReservees ?: 0
            ZoneTarifaireFormState(
                id = zone.id,
                name = zone.name,
                pricePerTable = zone.pricePerTable,
                m2Price = zone.m2Price,
                totalTables = zone.nbTables,
                availableTables = zone.nbTablesAvailable,
                reservedTables = reserved.toString(),
                reservedTablesInitial = reserved,
            )
        }

        return ReservationTarifaireUiState.Success(
            festivalId = reservationDetails.festivalId,
            zones = zoneForms,
            prixPrises = prixPrises,
            nbPrises = reservationDetails.nbPrises.toString(),
            tableDiscountOffered = numberToInput(reservationDetails.tableDiscountOffered),
            directDiscount = numberToInput(reservationDetails.directDiscount),
            note = reservationDetails.note.orEmpty(),
        )
    }

    private fun sanitizeIntInput(value: String): String {
        return value.filter { it.isDigit() }
    }

    private fun sanitizeDecimalInput(value: String): String {
        return value.filter { it.isDigit() || it == '.' || it == ',' }
    }

    private fun parseIntInput(value: String): Int {
        return value.toIntOrNull() ?: 0
    }

    private fun parseDoubleInput(value: String): Double {
        val normalized = value.replace(',', '.')
        return normalized.toDoubleOrNull() ?: 0.0
    }

    private fun numberToInput(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }

    private fun computeSummary(state: ReservationTarifaireUiState.Success): ReservationSummary {
        val zonePayloads = state.zones
            .mapNotNull { zone ->
                val count = parseIntInput(zone.reservedTables)
                if (count <= 0) return@mapNotNull null
                ReservationZoneUpdateDto(
                    zoneTarifaireId = zone.id,
                    nbTablesReservees = count,
                    nbChaisesReservees = 0,
                )
            }

        val tablesPrice = state.zones.sumOf { zone ->
            val count = parseIntInput(zone.reservedTables)
            count * zone.pricePerTable
        }
        val nbPrises = parseIntInput(state.nbPrises)
        val prisesPrice = nbPrises * state.prixPrises
        val startPrice = tablesPrice + prisesPrice

        val tableDiscountOffered = parseDoubleInput(state.tableDiscountOffered)
        val directDiscount = parseDoubleInput(state.directDiscount)
        val totalTables = state.zones.sumOf { parseIntInput(it.reservedTables) }
        val averageTablePrice = if (totalTables > 0) tablesPrice / totalTables else 0.0
        val tableDiscountValue = tableDiscountOffered * averageTablePrice
        val finalPrice = (startPrice - tableDiscountValue - directDiscount).coerceAtLeast(0.0)

        return ReservationSummary(
            startPrice = startPrice,
            finalPrice = finalPrice,
            nbPrises = nbPrises,
            tableDiscountOffered = tableDiscountOffered,
            directDiscount = directDiscount,
            zonePayloads = zonePayloads,
        )
    }

    data class ReservationSummary(
        val startPrice: Double,
        val finalPrice: Double,
        val nbPrises: Int,
        val tableDiscountOffered: Double,
        val directDiscount: Double,
        val zonePayloads: List<ReservationZoneUpdateDto>,
    )

    companion object {
        fun factory(
            reservationRepository: ReservationRepository,
            festivalRepository: FestivalRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ReservationTarifaireViewModel(reservationRepository, festivalRepository)
            }
        }
    }
}
