package com.projetmobile.mobile.data.entity.games

data class EditorOption(
    val id: Int,
    val name: String,
    val email: String?,
    val website: String?,
    val description: String?,
    val logoUrl: String?,
    val isExhibitor: Boolean,
    val isDistributor: Boolean,
)
