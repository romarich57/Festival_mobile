package com.projetmobile.mobile.data.remote.zoneplan

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ZonePlanApiService {

    @POST("zone-plan")
    suspend fun createZonePlan(
        @Body body: CreateZonePlanDto,
    ): CreateZonePlanResponseDto

    @GET("zone-plan/reservation/{reservationId}/context/{festivalId}")
    suspend fun getZonePlanContext(
        @Path("reservationId") reservationId: Int,
        @Path("festivalId") festivalId: Int,
    ): ZonePlanContextDto

    @POST("zone-plan/reservation/{reservationId}/allocations/{zonePlanId}")
    suspend fun createSimpleAllocation(
        @Path("reservationId") reservationId: Int,
        @Path("zonePlanId") zonePlanId: Int,
        @Body payload: SimpleAllocationPayloadDto,
    ): SimpleAllocationResponseDto

    @DELETE("zone-plan/allocations/{allocationId}")
    suspend fun deleteSimpleAllocationById(
        @Path("allocationId") allocationId: Int,
    )

    @PATCH("jeux_alloues/{allocationId}")
    suspend fun updateGameAllocation(
        @Path("allocationId") allocationId: Int,
        @Body payload: GameAllocationUpdateDto,
    )

    @DELETE("zone-plan/{zonePlanId}")
    suspend fun deleteZonePlan(
        @Path("zonePlanId") zonePlanId: Int,
    )
}
