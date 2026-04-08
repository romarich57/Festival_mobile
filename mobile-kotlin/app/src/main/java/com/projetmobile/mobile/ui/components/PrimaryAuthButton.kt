/**
 * Rôle : Bouton principal standardisé pour l'ensemble des écrans d'authentification.
 * Définit la forme, les couleurs et la gestion des états (actif/inactif).
 * Précondition : Utilisable partout dans l'arborescence Compose (les vues `auth`).
 * Postcondition : Affiche un bouton avec le style Material 3 de l'application Festival.
 */
package com.projetmobile.mobile.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Rôle : Décrit le composant primary auth button tone du module components.
 */
enum class PrimaryAuthButtonTone {
    Primary,
    Accent,
}

@Composable
/**
 * Rôle : Exécute l'action primary auth button du module components.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
fun PrimaryAuthButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: PrimaryAuthButtonTone = PrimaryAuthButtonTone.Primary,
    onClick: () -> Unit,
) {
    val containerColor = when (tone) {
        PrimaryAuthButtonTone.Primary -> MaterialTheme.colorScheme.primary
        PrimaryAuthButtonTone.Accent -> MaterialTheme.colorScheme.secondary
    }
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.82f),
        ),
    ) {
        Text(text = text)
    }
}
