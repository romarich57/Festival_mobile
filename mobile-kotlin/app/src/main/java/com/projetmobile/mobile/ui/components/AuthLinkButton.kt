/**
 * Rôle : Bouton purement textuel stylisé (lien d'authentification) utilisé pour les actions secondaires.
 * Principalement prévu pour des actions comme "Mot de passe oublié" ou basculer vers "Créer un compte".
 * Précondition : Appelé dans l'affichage d'un formulaire requérant des boutons discrets.
 * Postcondition : Un lien interactif, sans les bordures fortes du bouton principal Material.
 */
package com.projetmobile.mobile.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
/**
 * Rôle : Exécute l'action auth link button du module components.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
fun AuthLinkButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
