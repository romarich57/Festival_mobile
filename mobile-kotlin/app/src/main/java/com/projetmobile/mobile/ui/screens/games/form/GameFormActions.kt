package com.projetmobile.mobile.ui.screens.games

internal data class GameFormActions(
    val onTitleChanged: (String) -> Unit,
    val onTypeChanged: (String) -> Unit,
    val onSelectSuggestedType: (String) -> Unit,
    val onEditorSelected: (Int?) -> Unit,
    val onMinAgeChanged: (String) -> Unit,
    val onAuthorsChanged: (String) -> Unit,
    val onMinPlayersChanged: (String) -> Unit,
    val onMaxPlayersChanged: (String) -> Unit,
    val onDurationMinutesChanged: (String) -> Unit,
    val onPrototypeChanged: (Boolean) -> Unit,
    val onThemeChanged: (String) -> Unit,
    val onDescriptionChanged: (String) -> Unit,
    val onImageUrlChanged: (String) -> Unit,
    val onRulesVideoUrlChanged: (String) -> Unit,
    val onToggleMechanism: (Int) -> Unit,
    val onImageSourceModeChanged: (GameImageSourceMode) -> Unit,
    val onLocalImageSelected: (GameImageSelectionPayload) -> Unit,
    val onSaveGame: () -> Unit,
    val onDismissErrorMessage: () -> Unit,
    val onBackToList: () -> Unit,
)
