package com.projetmobile.mobile.data.entity.games

data class GameDraft(
    val title: String,
    val type: String,
    val editorId: Int?,
    val minAge: Int?,
    val authors: String,
    val minPlayers: Int?,
    val maxPlayers: Int?,
    val prototype: Boolean,
    val durationMinutes: Int?,
    val theme: String?,
    val description: String?,
    val imageUrl: String?,
    val rulesVideoUrl: String?,
    val mechanismIds: List<Int>,
)
