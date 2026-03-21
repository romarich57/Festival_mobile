package com.projetmobile.mobile.data.entity.games

data class GameFilters(
    val title: String = "",
    val type: String? = null,
    val editorId: Int? = null,
    val minAge: Int? = null,
    val sort: GameSort = GameSort.TitleAsc,
)
