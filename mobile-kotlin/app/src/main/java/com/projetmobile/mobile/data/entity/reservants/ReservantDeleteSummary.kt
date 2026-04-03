package com.projetmobile.mobile.data.entity.reservants

data class ReservantDeleteSummary(
    val reservantId: Int,
    val contacts: List<ReservantDeleteContactSummary>,
    val workflows: List<ReservantDeleteWorkflowSummary>,
    val reservations: List<ReservantDeleteReservationSummary>,
)

data class ReservantDeleteContactSummary(
    val id: Int,
    val name: String,
    val email: String?,
)

data class ReservantDeleteWorkflowSummary(
    val id: Int,
    val festivalId: Int?,
    val state: String?,
    val festivalName: String?,
)

data class ReservantDeleteReservationSummary(
    val id: Int,
    val festivalId: Int?,
    val paymentStatus: String?,
    val festivalName: String?,
    val relation: String?,
)
