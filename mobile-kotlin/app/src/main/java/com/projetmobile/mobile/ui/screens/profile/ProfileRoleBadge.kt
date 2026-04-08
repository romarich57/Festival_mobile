/**
 * Rôle : Composant affichant spécifiquement le badge de rôle global de l'utilisateur.
 *
 * Précondition : N/A.
 *
 * Postcondition : Affiche les couleurs ou le texte correspondant au compte.
 */
package com.projetmobile.mobile.ui.screens.profile

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
/**
 * Rôle : Exécute l'action profil role badge du module le profil.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
internal fun ProfileRoleBadge(role: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color(0xFFE8EEF9),
    ) {
        Text(
            text = role,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = Color(0xFF24406F),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
