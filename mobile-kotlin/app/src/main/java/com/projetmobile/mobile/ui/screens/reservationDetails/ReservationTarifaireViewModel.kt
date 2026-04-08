/**
 * Rôle : ViewModel gérant la tarification d'une réservation et le calcul des montants dus.
 *
 * Précondition : La réservation parente doit exister et ses montants de facturation récupérés via les APIs.
 *
 * Postcondition : Permet au composant ReservationTarifaireTab d'afficher sa vérité d'état sans erreur.
 */
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
import com.projetmobile.mobile.data.repository.toRepositoryException
import com.projetmobile.mobile.data.repository.festival.FestivalRepository
import com.projetmobile.mobile.data.repository.reservation.ReservationRepository
import kotlinx.coroutines.launch

/**
 * Rôle : Porte l'état et la logique du module les détails de réservation.
 */
class ReservationTarifaireViewModel(
    private val reservationRepository: ReservationRepository,
    private val festivalRepository: FestivalRepository,
) : ViewModel() {

    var uiState: ReservationTarifaireUiState by mutableStateOf(ReservationTarifaireUiState.Loading)
        private set

    /**
     * Rôle : Charge réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun loadReservation(reservationId: Int) {
        viewModelScope.launch {
            uiState = ReservationTarifaireUiState.Loading
            try {
                uiState = fetchState(reservationId)
            } catch (throwable: Throwable) {
                uiState = ReservationTarifaireUiState.Error(
                    throwable.toRepositoryException("Impossible de charger la tarification.")
                        .localizedMessage
                        ?: "Impossible de charger la tarification.",
                )
            }
        }
    }

    /**
     * Rôle : Gère la modification du champ zone tables.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onZoneTablesChanged(zoneId: Int, value: String) {
        val current = uiState as? ReservationTarifaireUiState.Success ?: return
        val sanitized = sanitizeIntInput(value)
        uiState = current.copy(
            zones = current.zones.map { zone ->
                if (zone.id == zoneId) zone.copy(reservedTables = sanitized) else zone
            }
        )
    }

    /**
     * Rôle : Gère la modification du champ nb prises.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onNbPrisesChanged(value: String) {
        val current = uiState as? ReservationTarifaireUiState.Success ?: return
        uiState = current.copy(nbPrises = sanitizeIntInput(value))
    }

    /**
     * Rôle : Gère la modification du champ table discount.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onTableDiscountChanged(value: String) {
        val current = uiState as? ReservationTarifaireUiState.Success ?: return
        uiState = current.copy(tableDiscountOffered = sanitizeDecimalInput(value))
    }

    /**
     * Rôle : Gère la modification du champ direct discount.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onDirectDiscountChanged(value: String) {
        val current = uiState as? ReservationTarifaireUiState.Success ?: return
        uiState = current.copy(directDiscount = sanitizeDecimalInput(value))
    }

    /**
     * Rôle : Gère la modification du champ note.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onNoteChanged(value: String) {
        val current = uiState as? ReservationTarifaireUiState.Success ?: return
        uiState = current.copy(note = value)
    }

    /**
     * Rôle : Réinitialise message.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun clearMessage() {
        val current = uiState as? ReservationTarifaireUiState.Success ?: return
        uiState = current.copy(userMessage = null)
    }

    /**
     * Rôle : Enregistre réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
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
            } catch (throwable: Throwable) {
                val latest = uiState as? ReservationTarifaireUiState.Success
                if (latest != null) {
                    uiState = latest.copy(
                        isSaving = false,
                        userMessage = throwable.toRepositoryException("Erreur de sauvegarde.")
                            .localizedMessage
                            ?: "Erreur de sauvegarde",
                    )
                } else {
                    uiState = ReservationTarifaireUiState.Error(
                        throwable.toRepositoryException("Impossible de charger la tarification.")
                            .localizedMessage
                            ?: "Impossible de charger la tarification.",
                    )
                }
            }
        }
    }

    /**
     * Rôle : Exécute l'action fetch état du module les détails de réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
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

    /**
     * Rôle : Exécute l'action sanitize int input du module les détails de réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun sanitizeIntInput(value: String): String {
        return value.filter { it.isDigit() }
    }

    /**
     * Rôle : Exécute l'action sanitize decimal input du module les détails de réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun sanitizeDecimalInput(value: String): String {
        return value.filter { it.isDigit() || it == '.' || it == ',' }
    }

    /**
     * Rôle : Exécute l'action parse int input du module les détails de réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun parseIntInput(value: String): Int {
        return value.toIntOrNull() ?: 0
    }

    /**
     * Rôle : Exécute l'action parse double input du module les détails de réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun parseDoubleInput(value: String): Double {
        val normalized = value.replace(',', '.')
        return normalized.toDoubleOrNull() ?: 0.0
    }

    /**
     * Rôle : Exécute l'action number to input du module les détails de réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun numberToInput(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }

    /**
     * Rôle : Exécute l'action compute résumé du module les détails de réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
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

    /**
     * Rôle : Décrit le composant réservation résumé du module les détails de réservation.
     */
    data class ReservationSummary(
        val startPrice: Double,
        val finalPrice: Double,
        val nbPrises: Int,
        val tableDiscountOffered: Double,
        val directDiscount: Double,
        val zonePayloads: List<ReservationZoneUpdateDto>,
    )

    /**
     * Rôle : Expose un singleton de support pour le module les détails de réservation.
     */
    companion object {
        /**
         * Rôle : Exécute l'action factory du module les détails de réservation.
         *
         * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
         *
         * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
         */
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
