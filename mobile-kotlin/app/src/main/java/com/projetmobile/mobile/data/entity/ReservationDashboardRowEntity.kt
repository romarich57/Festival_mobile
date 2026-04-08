package com.projetmobile.mobile.data.entity

/**
 * Rôle : Entité de domaine représentant de manière compacte une ligne simplifiée d'une réservation (exposant) du tableau de bord.
 * 
 * Précondition : Converti depuis son DTO (`ReservationDashboardRowDto`) par la couche mapper.
 * Postcondition : Structure allégée, utilisée directement par l'UI Android.
 */
data class ReservationDashboardRowEntity(
    val id: Int,
    val reservantName: String,
    val reservantType: String,
    val workflowState: String
)
