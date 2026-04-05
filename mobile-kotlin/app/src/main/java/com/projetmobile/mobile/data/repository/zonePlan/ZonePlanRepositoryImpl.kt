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

    override suspend fun createSimpleAllocation(
        reservationId: Int,
        zonePlanId: Int,
        payload: SimpleAllocationPayloadDto,
    ) {
        api.createSimpleAllocation(reservationId, zonePlanId, payload)
    }

    override suspend fun deleteSimpleAllocationById(allocationId: Int) {
        api.deleteSimpleAllocationById(allocationId)
    }

    override suspend fun updateGameAllocation(allocationId: Int, payload: GameAllocationUpdateDto) {
        api.updateGameAllocation(allocationId, payload)
    }

    override suspend fun deleteZonePlan(zonePlanId: Int) {
        api.deleteZonePlan(zonePlanId)
    }
}