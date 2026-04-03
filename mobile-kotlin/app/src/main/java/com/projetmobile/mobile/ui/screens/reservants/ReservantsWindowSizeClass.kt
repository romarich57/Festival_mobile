package com.projetmobile.mobile.ui.screens.reservants

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal enum class ReservantsWindowSizeClass {
    Compact,
    Medium,
    Expanded,
}

internal fun reservantsWindowSizeClass(width: Dp): ReservantsWindowSizeClass {
    return when {
        width >= 840.dp -> ReservantsWindowSizeClass.Expanded
        width >= 600.dp -> ReservantsWindowSizeClass.Medium
        else -> ReservantsWindowSizeClass.Compact
    }
}
