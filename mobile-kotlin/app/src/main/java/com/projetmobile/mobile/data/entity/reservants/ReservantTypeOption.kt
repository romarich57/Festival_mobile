package com.projetmobile.mobile.data.entity.reservants

data class ReservantTypeOption(
    val value: String,
)

val DefaultReservantTypeOptions = listOf(
    ReservantTypeOption(value = "editeur"),
    ReservantTypeOption(value = "prestataire"),
    ReservantTypeOption(value = "boutique"),
    ReservantTypeOption(value = "animateur"),
    ReservantTypeOption(value = "association"),
)
