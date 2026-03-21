package com.projetmobile.mobile.data.entity.reservants

data class ReservantEditorOption(
    val id: Int,
    val name: String,
    val email: String?,
    val website: String?,
    val description: String?,
    val logoUrl: String?,
    val isExhibitor: Boolean,
    val isDistributor: Boolean,
)
