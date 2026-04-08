/**
 * Rôle : Fichier de thème de l'application Jetpack Compose.
 * Il définit les couleurs, polices et formes globales utilisées à travers l'application.
 * Précondition : Appelé à la racine de la hiérarchie Compose (dans `MainActivity`).
 * Postcondition : Les composants enfants héritent du thème `MaterialTheme` défini.
 */
package com.projetmobile.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

private val LightColors = lightColorScheme(
    primary = Color(0xFF6F96DD),
    secondary = Color(0xFF255EC8),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF18233A),
    background = Color(0xFFF1F4FD),
    onBackground = Color(0xFF18233A),
)

/**
 * Rôle : Composant racine pour appliquer le thème "Festival" à l'application.
 * Précondition : Doit envelopper l'ensemble des écrans dans le bloc `setContent`.
 * Postcondition : Applique le schéma de couleurs principal et la typographie.
 */
@Composable
fun FestivalMobileTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = MaterialTheme.typography.copy(
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            titleMedium = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF57657F)),
        ),
        content = content,
    )
}
