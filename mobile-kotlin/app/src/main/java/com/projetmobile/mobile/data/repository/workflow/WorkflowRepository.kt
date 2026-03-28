package com.projetmobile.mobile.data.repository.workflow

import com.projetmobile.mobile.data.remote.reservation.WorkflowDto
import com.projetmobile.mobile.data.remote.reservation.WorkflowUpdatePayload

interface WorkflowRepository {

    suspend fun updateWorkflow(reservationId:Int, data: WorkflowUpdatePayload): WorkflowDto

    suspend fun getWorkflow(reservationId:Int): WorkflowDto

    suspend fun addContactDate(id:Int)
}