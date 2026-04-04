package com.projetmobile.mobile.data.remote.zoneplan

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

interface ZonePlanApiService {

    @GET("/api/zone-plan/reservation/{reservationId}/context/{festivalId}")
    suspend fun getZonePlanContext(
        @Path("reservationId") reservationId: Int,
        @Path("festivalId") festivalId: Int,
    ): ZonePlanContextDto

    @PUT("/api/zone-plan/reservation/{reservationId}/allocations/{zonePlanId}")
    suspend fun upsertSimpleAllocation(
        @Path("reservationId") reservationId: Int,
        @Path("zonePlanId") zonePlanId: Int,
        @Body payload: SimpleAllocationPayloadDto,
    ): SimpleAllocationResponseDto

    @DELETE("/api/zone-plan/reservation/{reservationId}/allocations/{zonePlanId}")
    suspend fun deleteSimpleAllocation(
        @Path("reservationId") reservationId: Int,
        @Path("zonePlanId") zonePlanId: Int,
    )

    @PATCH("/api/jeux_alloues/{allocationId}")
    suspend fun updateGameAllocation(
        @Path("allocationId") allocationId: Int,
        @Body payload: GameAllocationUpdateDto,
    )
}
