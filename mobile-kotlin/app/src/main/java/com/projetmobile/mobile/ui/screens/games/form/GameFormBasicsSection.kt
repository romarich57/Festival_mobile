package com.projetmobile.mobile.ui.screens.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.games.EditorOption
import com.projetmobile.mobile.data.entity.games.GameTypeOption
import com.projetmobile.mobile.ui.components.FestivalTextField

@Composable
internal fun GameFormBasicsSection(
    fields: GameFormFields,
    availableTypes: List<GameTypeOption>,
    availableEditors: List<EditorOption>,
    editorSelectionLocked: Boolean,
    imageSourceMode: GameImageSourceMode,
    localImageSelection: LocalGameImageSelection?,
    actions: GameFormActions,
) {
    GameImagePreviewRow(
        imageUrl = fields.imageUrl,
        imageSourceMode = imageSourceMode,
        localImageSelection = localImageSelection,
    )

    FestivalTextField(
        value = fields.title,
        onValueChange = actions.onTitleChanged,
        label = "Titre *",
        isError = fields.titleError != null,
        supportingText = fields.titleError,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game-title-field"),
    )

    FestivalTextField(
        value = fields.type,
        onValueChange = actions.onTypeChanged,
        label = "Type *",
        isError = fields.typeError != null,
        supportingText = fields.typeError,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game-type-field"),
    )

    if (availableTypes.isNotEmpty()) {
        GameTypeSuggestionsRow(
            types = availableTypes,
            selectedType = fields.type,
            onSelectSuggestedType = actions.onSelectSuggestedType,
        )
    }

    GamesDropdownSelector(
        label = "Éditeur *",
        selectedLabel = availableEditors.firstOrNull { it.id == fields.editorId }?.name
            ?: "Choisir un éditeur",
        options = availableEditors.map { it.name to it.id },
        onValueSelected = actions.onEditorSelected,
        enabled = !editorSelectionLocked,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game-editor-field"),
    )
    if (fields.editorError != null) {
        Text(
            text = fields.editorError,
            color = Color(0xFFB42318),
            style = MaterialTheme.typography.bodySmall,
        )
    }

    GameNumberFieldRow(
        minAgeInput = fields.minAgeInput,
        minAgeError = fields.minAgeError,
        onMinAgeChanged = actions.onMinAgeChanged,
        minPlayersInput = fields.minPlayersInput,
        minPlayersError = fields.minPlayersError,
        onMinPlayersChanged = actions.onMinPlayersChanged,
        maxPlayersInput = fields.maxPlayersInput,
        maxPlayersError = fields.maxPlayersError,
        onMaxPlayersChanged = actions.onMaxPlayersChanged,
    )

    FestivalTextField(
        value = fields.authors,
        onValueChange = actions.onAuthorsChanged,
        label = "Auteurs *",
        isError = fields.authorsError != null,
        supportingText = fields.authorsError,
        modifier = Modifier.fillMaxWidth(),
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(
            checked = fields.prototype,
            onCheckedChange = actions.onPrototypeChanged,
        )
        Text(
            text = "Prototype",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF5D6981),
        )
    }

    FestivalTextField(
        value = fields.durationMinutesInput,
        onValueChange = actions.onDurationMinutesChanged,
        label = "Durée (minutes)",
        isError = fields.durationMinutesError != null,
        supportingText = fields.durationMinutesError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )

    FestivalTextField(
        value = fields.theme,
        onValueChange = actions.onThemeChanged,
        label = "Thème",
        modifier = Modifier.fillMaxWidth(),
    )

    FestivalTextField(
        value = fields.description,
        onValueChange = actions.onDescriptionChanged,
        label = "Description",
        singleLine = false,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
    )
}
