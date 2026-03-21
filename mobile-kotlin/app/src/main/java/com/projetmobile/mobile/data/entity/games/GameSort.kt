package com.projetmobile.mobile.data.entity.games

enum class GameSort(val apiValue: String) {
    TitleAsc("title_asc"),
    TitleDesc("title_desc"),
    MinAgeAsc("min_age_asc"),
    MinAgeDesc("min_age_desc"),
    EditorAsc("editor_asc"),
    EditorDesc("editor_desc"),
}
