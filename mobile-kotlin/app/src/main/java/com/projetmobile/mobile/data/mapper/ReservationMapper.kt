package com.projetmobile.mobile.data.mapper

import com.projetmobile.mobile.data.remote.reservation.ReservationDashboardRowDto
import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity

/**
 * Rôle : Convertit un objet Data Transfer Object (DTO) de tableau de bord de réservation en son entité métier correspondante.
 * 
 * Précondition : Le DTO `ReservationDashboardRowDto` doit posséder au moins un identifiant, un type de réservant, et un statut de workflow valides.
 * Postcondition : Retourne un objet `ReservationDashboardRowEntity` exploitable par l'UI, avec une chaîne vide si le nom du réservant est nul.
 */
fun ReservationDashboardRowDto.toEntity(): ReservationDashboardRowEntity {
    return ReservationDashboardRowEntity(
        id = this.id,
        // Si le backend renvoie null, on met une chaîne vide pour éviter les crashs
        reservantName = this.reservantName ?: "",
        reservantType = this.reservantType,
        workflowState = this.workflowState
    )
}