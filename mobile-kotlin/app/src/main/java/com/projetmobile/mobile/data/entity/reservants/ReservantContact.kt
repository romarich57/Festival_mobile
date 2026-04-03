package com.projetmobile.mobile.data.entity.reservants

data class ReservantContact(
    val id: Int,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val jobTitle: String,
    val priority: Int,
)
