package com.projetmobile.mobile.ui.screens.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.PrimaryAuthButton
import com.projetmobile.mobile.ui.components.PrimaryAuthButtonTone

@Composable
internal fun GameFormActionButtons(
    isEditMode: Boolean,
    isSaving: Boolean,
    showHorizontalActions: Boolean,
    onSaveGame: () -> Unit,
    onBackToList: () -> Unit,
) {
    val saveLabel = if (isEditMode) {
        "Enregistrer les modifications"
    } else {
        "Créer le jeu"
    }

    if (showHorizontalActions) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PrimaryAuthButton(
                text = saveLabel,
                modifier = Modifier
                    .weight(1f)
                    .testTag("game-save-button"),
                enabled = !isSaving,
                tone = if (isEditMode) PrimaryAuthButtonTone.Primary else PrimaryAuthButtonTone.Accent,
                onClick = onSaveGame,
            )
            OutlinedButton(
                onClick = onBackToList,
                modifier = Modifier
                    .weight(1f)
                    .testTag("game-cancel-button"),
            ) {
                Text("Annuler")
            }
        }
    } else {
        PrimaryAuthButton(
            text = saveLabel,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("game-save-button"),
            enabled = !isSaving,
            tone = if (isEditMode) PrimaryAuthButtonTone.Primary else PrimaryAuthButtonTone.Accent,
            onClick = onSaveGame,
        )
        OutlinedButton(
            onClick = onBackToList,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("game-cancel-button"),
        ) {
            Text("Annuler")
        }
    }
}
