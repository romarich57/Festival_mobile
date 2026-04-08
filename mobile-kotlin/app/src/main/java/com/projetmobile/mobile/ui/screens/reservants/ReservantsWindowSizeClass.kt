/**
 * Rôle : Détermine la classe de largeur utilisée par les écrans de réservants pour adapter la mise en page.
 * Précondition : La largeur disponible doit être connue sous forme de `Dp`.
 * Postcondition : Retourne une classe compacte, moyenne ou étendue selon les seuils de l'application.
 */
package com.projetmobile.mobile.ui.screens.reservants

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Rôle : Décrit le composant réservants fenêtre taille classe du module les réservants.
 */
internal enum class ReservantsWindowSizeClass {
    Compact,
    Medium,
    Expanded,
}

/**
 * Rôle : Catégorise la largeur d'écran d'un écran de réservants en classe responsive.
 * Précondition : `width` doit représenter la largeur réelle disponible dans l'UI Compose.
 * Postcondition : Retourne la classe de taille adaptée aux seuils 600dp et 840dp.
 */
internal fun reservantsWindowSizeClass(width: Dp): ReservantsWindowSizeClass {
    return when {
        width >= 840.dp -> ReservantsWindowSizeClass.Expanded
        width >= 600.dp -> ReservantsWindowSizeClass.Medium
        else -> ReservantsWindowSizeClass.Compact
    }
}
