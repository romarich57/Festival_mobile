package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.EditorOption
import com.projetmobile.mobile.data.entity.games.GameTypeOption
import com.projetmobile.mobile.data.entity.games.MechanismOption

sealed interface GameFormMode {
    data class Create(
        val prefilledEditorId: Int? = null,
        val lockEditorSelection: Boolean = false,
    ) : GameFormMode

    data class Edit(val gameId: Int) : GameFormMode
}

enum class GameImageSourceMode {
    Url,
    File,
}

data class LocalGameImageSelection(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val previewUriString: String,
)

data class GameImageSelectionPayload(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val previewUriString: String,
)

data class GameFormFields(
    val title: String = "",
    val titleError: String? = null,
    val type: String = "",
    val typeError: String? = null,
    val editorId: Int? = null,
    val editorError: String? = null,
    val minAgeInput: String = "",
    val minAgeError: String? = null,
    val authors: String = "",
    val authorsError: String? = null,
    val minPlayersInput: String = "",
    val minPlayersError: String? = null,
    val maxPlayersInput: String = "",
    val maxPlayersError: String? = null,
    val durationMinutesInput: String = "",
    val durationMinutesError: String? = null,
    val prototype: Boolean = false,
    val theme: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val rulesVideoUrl: String = "",
    val selectedMechanismIds: Set<Int> = emptySet(),
)

/**
 * Rôle : Représente l'état du formulaire de création/modification de jeu.
 *
 * Précondition : Centralise les options disponibles (types, éditeurs, mécanismes) et les valeurs saisies.
 *
 * Postcondition : Garantit que l'interface reflète l'état validé ou en cours sous forme de données immuables.
 */
data class GameFormUiState(
    val mode: GameFormMode,
    val fields: GameFormFields = GameFormFields(),
    val availableTypes: List<GameTypeOption> = emptyList(),
    val availableEditors: List<EditorOption> = emptyList(),
    val availableMechanisms: List<MechanismOption> = emptyList(),
    val imageSourceMode: GameImageSourceMode = GameImageSourceMode.Url,
    val localImageSelection: LocalGameImageSelection? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val completedMessage: String? = null,
    val lookupErrorMessage: String? = null,
    val errorMessage: String? = null,
) {
    val isEditMode: Boolean
        get() = mode is GameFormMode.Edit

    val isEditorSelectionLocked: Boolean
        get() = (mode as? GameFormMode.Create)?.lockEditorSelection == true
}
