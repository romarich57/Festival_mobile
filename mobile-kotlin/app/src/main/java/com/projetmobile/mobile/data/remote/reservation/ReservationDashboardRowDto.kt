package com.projetmobile.mobile.data.remote.reservation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rôle : Entité ultra-minimaliste récupérée en masse fournissant assez d'information visuelle
 * pour générer les cellules du tableau de bord listant les invités majeurs du festival.
 * 
 * Précondition : Ne pas utiliser pour des affichages approfondis.
 * Postcondition : Renseigne l'UI sur le niveau global d'avancée de relationnel (`workflowState`).
 */
@Serializable
data class ReservationDashboardRowDto(
    @SerialName("id") val id: Int,
    @SerialName("reservant_name") val reservantName: String?,
    @SerialName("reservant_type") val reservantType: String,
    @SerialName("workflow_state") val workflowState: String
)