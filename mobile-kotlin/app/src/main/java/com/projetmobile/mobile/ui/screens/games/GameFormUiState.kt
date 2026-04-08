/**
 * Rôle : Décrit l'état UI immuable du module les jeux.
 */

package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.EditorOption
import com.projetmobile.mobile.data.entity.games.GameTypeOption
import com.projetmobile.mobile.data.entity.games.MechanismOption

/**
 * Rôle : Définit le contrat du module les jeux.
 */
sealed interface GameFormMode {
    /**
     * Rôle : Décrit le composant création du module les jeux.
     */
    data class Create(
        val prefilledEditorId: Int? = null,
        val lockEditorSelection: Boolean = false,
    ) : GameFormMode

    /**
     * Rôle : Décrit le composant édition du module les jeux.
     */
    data class Edit(val gameId: Int) : GameFormMode
}

/**
 * Rôle : Décrit le composant jeu image source mode du module les jeux.
 */
enum class GameImageSourceMode {
    Url,
    File,
}

/**
 * Rôle : Décrit le composant local jeu image selection du module les jeux.
 */
data class LocalGameImageSelection(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val previewUriString: String,
)

/**
 * Rôle : Décrit le composant jeu image selection payload du module les jeux.
 */
data class GameImageSelectionPayload(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val previewUriString: String,
)

/**
 * Rôle : Décrit le composant jeu formulaire champs du module les jeux.
 */
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
