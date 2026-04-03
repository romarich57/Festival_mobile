package com.projetmobile.mobile.data.entity.games

import kotlinx.serialization.Serializable

@Serializable
data class MechanismOption(
    val id: Int,
    val name: String,
    val description: String?,
)
