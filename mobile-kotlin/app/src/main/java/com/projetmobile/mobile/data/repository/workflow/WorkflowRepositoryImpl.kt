package com.projetmobile.mobile.data.repository.workflow

import com.projetmobile.mobile.data.remote.reservation.ReservationApiService
import com.projetmobile.mobile.data.remote.reservation.WorkflowDto
import com.projetmobile.mobile.data.remote.reservation.WorkflowUpdatePayload

/**
 * Rôle : Implémente concrètement le [WorkflowRepository] sans passer par la base SQLite (Room).
 * Par choix d'architecture, le suivi des relances "Workflow" s'opère toujours on-the-fly (Internet requis).
 * 
 * Précondition : [ReservationApiService] opérationnel (c'est lui qui héberge les Endpoints Workflow).
 * Postcondition : Renvoie les Dto directement au ViewModel de l'UI.
 */
class WorkflowRepositoryImpl(
    private val api: ReservationApiService) : WorkflowRepository {

    override suspend fun updateWorkflow(reservationId: Int, data: WorkflowUpdatePayload): WorkflowDto {
        return api.updateWorkflow(reservationId, data)
    }

    override suspend fun getWorkflow(reservationId: Int): WorkflowDto {
        return api.getWorkflowByReservationId(reservationId)
    }

    override suspend fun addContactDate(id: Int) {
        api.addContactDate(id)
    }
}