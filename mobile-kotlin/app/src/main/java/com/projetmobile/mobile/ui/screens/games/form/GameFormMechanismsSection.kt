/**
 * Rôle : Section dédiée à l'édition ou la consultation des mécanismes de jeu.
 *
 * Précondition : Charger la liste des mécanismes disponibles dans les Lookups.
 *
 * Postcondition : Retourne la sélection de tags associés.
 */
package com.projetmobile.mobile.ui.screens.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.games.MechanismOption

@Composable
/**
 * Rôle : Exécute l'action jeu formulaire mechanisms section du module les jeux formulaire.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
internal fun GameFormMechanismsSection(
    mechanisms: List<MechanismOption>,
    selectedIds: Set<Int>,
    onToggleMechanism: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Mécanismes",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF1B2740),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            mechanisms.forEach { mechanism ->
                FilterChip(
                    selected = mechanism.id in selectedIds,
                    onClick = { onToggleMechanism(mechanism.id) },
                    label = { Text(mechanism.name) },
                )
            }
        }
    }
}
