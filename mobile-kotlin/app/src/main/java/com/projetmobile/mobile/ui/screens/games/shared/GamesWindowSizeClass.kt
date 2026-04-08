/**
 * Rôle : Détermine la classe de largeur utilisée par les écrans de jeux pour adapter leur mise en page.
 * Précondition : La largeur disponible doit être connue sous forme de `Dp`.
 * Postcondition : Retourne une classe compacte, moyenne ou étendue selon les seuils de l'application.
 */
package com.projetmobile.mobile.ui.screens.games

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Rôle : Décrit le composant jeux fenêtre taille classe du module les jeux partagé.
 */
internal enum class GamesWindowSizeClass {
    Compact,
    Medium,
    Expanded,
}

/**
 * Rôle : Catégorise la largeur d'écran d'un écran de jeux en classe responsive.
 * Précondition : `width` doit représenter la largeur réelle disponible dans l'UI Compose.
 * Postcondition : Retourne la classe de taille adaptée aux seuils 600dp et 840dp.
 */
internal fun gamesWindowSizeClass(width: Dp): GamesWindowSizeClass {
    return when {
        width >= 840.dp -> GamesWindowSizeClass.Expanded
        width >= 600.dp -> GamesWindowSizeClass.Medium
        else -> GamesWindowSizeClass.Compact
    }
}
