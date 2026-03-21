package com.projetmobile.mobile.ui.screens.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthCard

@Composable
internal fun GameFormContentCard(
    uiState: GameFormUiState,
    actions: GameFormActions,
    showHorizontalActions: Boolean,
    onPickLocalImage: () -> Unit,
) {
    AuthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game-form-card"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            GameFormBasicsSection(
                fields = uiState.fields,
                availableTypes = uiState.availableTypes,
                availableEditors = uiState.availableEditors,
                imageSourceMode = uiState.imageSourceMode,
                localImageSelection = uiState.localImageSelection,
                actions = actions,
            )

            GameFormMediaSection(
                imageSourceMode = uiState.imageSourceMode,
                imageUrl = uiState.fields.imageUrl,
                localImageSelection = uiState.localImageSelection,
                rulesVideoUrl = uiState.fields.rulesVideoUrl,
                onImageSourceModeChanged = actions.onImageSourceModeChanged,
                onImageUrlChanged = actions.onImageUrlChanged,
                onRulesVideoUrlChanged = actions.onRulesVideoUrlChanged,
                onPickFile = onPickLocalImage,
            )

            GameFormMechanismsSection(
                mechanisms = uiState.availableMechanisms,
                selectedIds = uiState.fields.selectedMechanismIds,
                onToggleMechanism = actions.onToggleMechanism,
            )

            GameFormActionButtons(
                isEditMode = uiState.isEditMode,
                isSaving = uiState.isSaving,
                showHorizontalActions = showHorizontalActions,
                onSaveGame = actions.onSaveGame,
                onBackToList = actions.onBackToList,
            )
        }
    }
}
