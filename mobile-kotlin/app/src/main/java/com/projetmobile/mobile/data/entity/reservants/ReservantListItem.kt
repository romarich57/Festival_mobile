package com.projetmobile.mobile.data.entity.reservants

data class ReservantListItem(
    val id: Int,
    val name: String,
    val email: String,
    val type: String,
    val editorId: Int?,
    val phoneNumber: String?,
    val address: String?,
    val siret: String?,
    val notes: String?,
)
