package com.projetmobile.mobile.data.entity.games

data class PagedResult<T>(
    val items: List<T>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val hasNext: Boolean,
)
