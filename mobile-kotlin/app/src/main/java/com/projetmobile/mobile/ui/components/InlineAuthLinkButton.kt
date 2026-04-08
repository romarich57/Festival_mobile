/**
 * Rôle : Variante Inline du `AuthLinkButton`, retire tout espacement pour s'intégrer directement au sein de courtes phrases.
 * Précondition : Appelé dans la construction des paragraphes complexes ("Vous n'avez pas de compte ? [Créer un compte]").
 * Postcondition : Bouton textuel sans marges internes s'intégrant au corps du texte standard.
 */
package com.projetmobile.mobile.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
/**
 * Rôle : Exécute l'action inline auth link button du module components.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
fun InlineAuthLinkButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
