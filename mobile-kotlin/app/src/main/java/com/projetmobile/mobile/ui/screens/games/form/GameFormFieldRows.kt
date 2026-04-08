/**
 * Rôle : Composant gérant les lignes d'édition spécifiques de certains champs (ex: prix, stock).
 *
 * Précondition : Fournir l'état et l'action de mise à jour relative.
 *
 * Postcondition : Modification en direct du flux UiState.
 */
package com.projetmobile.mobile.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.projetmobile.mobile.data.entity.games.GameTypeOption
import com.projetmobile.mobile.ui.components.FestivalTextField

@Composable
/**
 * Rôle : Exécute l'action jeu image preview row du module les jeux formulaire.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
internal fun GameImagePreviewRow(
    imageUrl: String,
    imageSourceMode: GameImageSourceMode,
    localImageSelection: LocalGameImageSelection?,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val useRowLayout = maxWidth >= 340.dp

        val previewContent: @Composable () -> Unit = {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(
                        color = Color(0xFFEAF0FB),
                        shape = RoundedCornerShape(20.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                val previewModel = when {
                    imageSourceMode == GameImageSourceMode.File && localImageSelection != null -> {
                        localImageSelection.previewUriString
                    }

                    imageUrl.isNotBlank() -> toAbsoluteBackendUrl(imageUrl)
                    else -> null
                }
                if (previewModel != null) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = Color(0xFF5D6981),
                        modifier = Modifier.size(30.dp),
                    )
                }
            }
        }

        val labelContent: @Composable () -> Unit = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Aperçu de l'image",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                    color = Color(0xFF5D6981),
                )
                Text(
                    text = when {
                        imageSourceMode == GameImageSourceMode.File && localImageSelection != null -> {
                            localImageSelection.fileName
                        }

                        imageUrl.isNotBlank() -> "Image distante"
                        else -> "Aucune image renseignée."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        if (useRowLayout) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                previewContent()
                Box(modifier = Modifier.weight(1f)) {
                    labelContent()
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                previewContent()
                labelContent()
            }
        }
    }
}

@Composable
/**
 * Rôle : Exécute l'action jeu number champ row du module les jeux formulaire.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
internal fun GameNumberFieldRow(
    minAgeInput: String,
    minAgeError: String?,
    onMinAgeChanged: (String) -> Unit,
    minPlayersInput: String,
    minPlayersError: String?,
    onMinPlayersChanged: (String) -> Unit,
    maxPlayersInput: String,
    maxPlayersError: String?,
    onMaxPlayersChanged: (String) -> Unit,
) {
    FestivalTextField(
        value = minAgeInput,
        onValueChange = onMinAgeChanged,
        label = "Âge minimum *",
        isError = minAgeError != null,
        supportingText = minAgeError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val useRowLayout = maxWidth >= 420.dp
        if (useRowLayout) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FestivalTextField(
                    value = minPlayersInput,
                    onValueChange = onMinPlayersChanged,
                    label = "Joueurs min",
                    isError = minPlayersError != null,
                    supportingText = minPlayersError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                FestivalTextField(
                    value = maxPlayersInput,
                    onValueChange = onMaxPlayersChanged,
                    label = "Joueurs max",
                    isError = maxPlayersError != null,
                    supportingText = maxPlayersError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FestivalTextField(
                    value = minPlayersInput,
                    onValueChange = onMinPlayersChanged,
                    label = "Joueurs min",
                    isError = minPlayersError != null,
                    supportingText = minPlayersError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                FestivalTextField(
                    value = maxPlayersInput,
                    onValueChange = onMaxPlayersChanged,
                    label = "Joueurs max",
                    isError = maxPlayersError != null,
                    supportingText = maxPlayersError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
/**
 * Rôle : Exécute l'action jeu type suggestions row du module les jeux formulaire.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
internal fun GameTypeSuggestionsRow(
    types: List<GameTypeOption>,
    selectedType: String,
    onSelectSuggestedType: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Suggestions",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF5D6981),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            types.forEach { type ->
                FilterChip(
                    selected = selectedType == type.value,
                    onClick = { onSelectSuggestedType(type.value) },
                    label = { Text(type.value) },
                )
            }
        }
    }
}
