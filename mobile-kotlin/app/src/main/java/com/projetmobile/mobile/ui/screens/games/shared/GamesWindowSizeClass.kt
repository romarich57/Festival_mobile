package com.projetmobile.mobile.ui.screens.games

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal enum class GamesWindowSizeClass {
    Compact,
    Medium,
    Expanded,
}

internal fun gamesWindowSizeClass(width: Dp): GamesWindowSizeClass {
    return when {
        width >= 840.dp -> GamesWindowSizeClass.Expanded
        width >= 600.dp -> GamesWindowSizeClass.Medium
        else -> GamesWindowSizeClass.Compact
    }
}
