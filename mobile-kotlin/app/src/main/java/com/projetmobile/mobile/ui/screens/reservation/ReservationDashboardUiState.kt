package com.projetmobile.mobile.ui.screens.reservation

import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity

/**
 * Rôle : Décrit l'état en lecture seule du tableau de bord des réservations.
 *
 * Précondition : Met à jour la liste des réservations en fonction des critères actifs (search, type, sort).
 *
 * Postcondition : Utilisé par Screen pour savoir l'état de chargement et le contenu de la table sans manipuler de données nulles.
 */
data class ReservationDashboardUiState(
    val isLoading: Boolean = false,
    val reservations: List<ReservationDashboardRowEntity> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val typeFilter: String = "all",
    val sortKey: String = "name-asc",
)
