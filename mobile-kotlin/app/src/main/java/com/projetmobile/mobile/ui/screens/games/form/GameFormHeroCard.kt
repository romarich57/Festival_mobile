/**
 * Rôle : Encart principal en haut d'édition contenant le titre, le nom localisé et le summary.
 *
 * Précondition : Contexte de FormUI State.
 *
 * Postcondition : Encadre visuellement les données maîtresses et identifiantes du jeu.
 */
package com.projetmobile.mobile.ui.screens.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthCard

@Composable
/**
 * Rôle : Exécute l'action jeu formulaire hero carte du module les jeux formulaire.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
internal fun GameFormHeroCard(
    mode: GameFormMode,
    gameTitle: String,
    onBackToList: () -> Unit,
) {
    val isEditMode = mode is GameFormMode.Edit
    val heroContainerColor = if (isEditMode) {
        MaterialTheme.colorScheme.surface
    } else {
        Color(0xFFEAF0FB)
    }

    AuthCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = heroContainerColor,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            val useRowLayout = maxWidth >= 520.dp
            val resolvedTitle = when {
                gameTitle.isNotBlank() -> gameTitle
                isEditMode -> "Nouveau jeu"
                else -> ""
            }

            if (useRowLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (resolvedTitle.isNotBlank()) {
                        Text(
                            text = resolvedTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (isEditMode) Color(0xFF18233A) else Color(0xFF24406F),
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Row(modifier = Modifier.weight(1f)) {}
                    }
                    OutlinedButton(
                        onClick = onBackToList,
                        modifier = Modifier.testTag("game-back-to-list-button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                        )
                        Text(
                            text = "Retour",
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (resolvedTitle.isNotBlank()) {
                        Text(
                            text = resolvedTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (isEditMode) Color(0xFF18233A) else Color(0xFF24406F),
                        )
                    }
                    OutlinedButton(
                        onClick = onBackToList,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("game-back-to-list-button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                        )
                        Text(
                            text = "Retour",
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
