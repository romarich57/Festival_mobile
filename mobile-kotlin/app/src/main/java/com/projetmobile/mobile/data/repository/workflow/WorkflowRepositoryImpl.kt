package com.projetmobile.mobile.data.repository.workflow

import com.projetmobile.mobile.data.remote.reservation.ReservationApiService
import com.projetmobile.mobile.data.remote.reservation.WorkflowDto
import com.projetmobile.mobile.data.remote.reservation.WorkflowUpdatePayload

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