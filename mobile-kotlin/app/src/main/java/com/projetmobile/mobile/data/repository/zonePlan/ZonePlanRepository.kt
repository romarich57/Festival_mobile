package com.projetmobile.mobile.data.repository.zonePlan

import com.projetmobile.mobile.data.remote.zoneplan.GameAllocationUpdateDto
import com.projetmobile.mobile.data.remote.zoneplan.SimpleAllocationPayloadDto
import com.projetmobile.mobile.data.remote.zoneplan.ZonePlanContextDto

interface ZonePlanRepository {

    suspend fun getZonePlanContext(reservationId: Int, festivalId: Int?): ZonePlanContextDto

    suspend fun upsertSimpleAllocation(
        reservationId: Int,
        zonePlanId: Int,
        payload: SimpleAllocationPayloadDto,
    )

    suspend fun deleteSimpleAllocation(reservationId: Int, zonePlanId: Int)

    suspend fun updateGameAllocation(allocationId: Int, payload: GameAllocationUpdateDto)
}