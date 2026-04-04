package com.projetmobile.mobile.data.repository.zonePlan

import com.projetmobile.mobile.data.remote.zoneplan.CreateZonePlanDto
import com.projetmobile.mobile.data.remote.zoneplan.GameAllocationUpdateDto
import com.projetmobile.mobile.data.remote.zoneplan.SimpleAllocationPayloadDto
import com.projetmobile.mobile.data.remote.zoneplan.ZonePlanApiService
import com.projetmobile.mobile.data.remote.zoneplan.ZonePlanContextDto

class ZonePlanRepositoryImpl(
    private val api: ZonePlanApiService,
) : ZonePlanRepository {

    override suspend fun createZonePlan(
        festivalId: Int,
        name: String,
        idZoneTarifaire: Int,
        nbTables: Int,
    ) {
        api.createZonePlan(
            CreateZonePlanDto(
                name = name,
                festivalId = festivalId,
                idZoneTarifaire = idZoneTarifaire,
                nbTables = nbTables,
            )
        )
    }

    override suspend fun getZonePlanContext(reservationId: Int, festivalId: Int): ZonePlanContextDto {
        return api.getZonePlanContext(reservationId, festivalId)
    }

    override suspend fun upsertSimpleAllocation(
        reservationId: Int,
        zonePlanId: Int,
        payload: SimpleAllocationPayloadDto,
    ) {
        api.upsertSimpleAllocation(reservationId, zonePlanId, payload)
    }

    override suspend fun deleteSimpleAllocation(reservationId: Int, zonePlanId: Int) {
        api.deleteSimpleAllocation(reservationId, zonePlanId)
    }

    override suspend fun updateGameAllocation(allocationId: Int, payload: GameAllocationUpdateDto) {
        api.updateGameAllocation(allocationId, payload)
    }
}