package com.projetmobile.mobile.ui.screens.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projetmobile.mobile.data.repository.games.GamesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Rôle : Modèle-vue orchestrant la validation et la soumission d'un jeu, ainsi que son chargement pour une édition.
 *
 * Précondition : Appelé par GameFormScreen via un factory transmettant le GameFormMode (création ou édition d'ID via repo).
 *
 * Postcondition : Charge les choix (éditeurs, types, etc.) et gère la soumission finale de l'entité.
 */
internal class GameFormViewModel(
    private val gamesRepository: GamesRepository,
    private val mode: GameFormMode,
    private val validator: GameFormValidator,
    private val draftMapper: GameFormDraftMapper,
    private val prefillMapper: GameFormPrefillMapper,
    private val lookupsLoader: GameFormLookupsLoader,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameFormUiState(mode = mode))
    val uiState: StateFlow<GameFormUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }
    fun onTitleChanged(value: String) = updateFields { copy(title = value, titleError = null) }
    fun onTypeChanged(value: String) = updateFields { copy(type = value, typeError = null) }
    fun onSelectSuggestedType(value: String) = updateFields { copy(type = value, typeError = null) }
    fun onEditorSelected(editorId: Int?) {
        if (_uiState.value.isEditorSelectionLocked) {
            return
        }
        updateFields { copy(editorId = editorId, editorError = null) }
    }
    fun onMinAgeChanged(value: String) = updateNumericField(value) { copy(minAgeInput = value, minAgeError = null) }
    fun onAuthorsChanged(value: String) = updateFields { copy(authors = value, authorsError = null) }
    fun onMinPlayersChanged(value: String) = updateNumericField(value) { copy(minPlayersInput = value, minPlayersError = null) }
    fun onMaxPlayersChanged(value: String) = updateNumericField(value) { copy(maxPlayersInput = value, maxPlayersError = null) }
    fun onDurationMinutesChanged(value: String) = updateNumericField(value) { copy(durationMinutesInput = value, durationMinutesError = null) }
    fun onPrototypeChanged(value: Boolean) = updateFields { copy(prototype = value) }
    fun onThemeChanged(value: String) = updateFields { copy(theme = value) }
    fun onDescriptionChanged(value: String) = updateFields { copy(description = value) }
    fun onImageUrlChanged(value: String) = updateFields { copy(imageUrl = value) }
    fun onRulesVideoUrlChanged(value: String) = updateFields { copy(rulesVideoUrl = value) }
    fun onToggleMechanism(mechanismId: Int) {
        updateFields {
            copy(
                selectedMechanismIds = if (mechanismId in selectedMechanismIds) {
                    selectedMechanismIds - mechanismId
                } else {
                    selectedMechanismIds + mechanismId
                },
            )
        }
    }
    fun onImageSourceModeChanged(mode: GameImageSourceMode) {
        _uiState.update { state ->
            state.copy(
                imageSourceMode = mode,
                localImageSelection = if (mode == GameImageSourceMode.File) {
                    state.localImageSelection
                } else {
                    null
                },
            )
        }
    }
    fun onLocalImageSelected(selection: GameImageSelectionPayload) {
        _uiState.update { state ->
            state.copy(
                imageSourceMode = GameImageSourceMode.File,
                localImageSelection = LocalGameImageSelection(
                    fileName = selection.fileName,
                    mimeType = selection.mimeType,
                    bytes = selection.bytes,
                    previewUriString = selection.previewUriString,
                ),
                errorMessage = null,
            )
        }
    }
    fun saveGame() {
        val validation = validator.validate(_uiState.value.fields)
        if (!validation.isValid) {
            _uiState.update { state -> state.copy(fields = validation.fields) }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isSaving = true,
                    errorMessage = null,
                    fields = state.fields.withFieldErrors(GameFormFieldErrors()),
                )
            }
            val shouldUploadLocalImage = _uiState.value.imageSourceMode == GameImageSourceMode.File &&
                _uiState.value.localImageSelection != null
            val uploadedImageUrl = if (shouldUploadLocalImage) uploadSelectedImage() else null
            if (shouldUploadLocalImage && uploadedImageUrl == null) {
                return@launch
            }
            val draft = draftMapper.toDraft(
                fields = _uiState.value.fields,
                imageSourceMode = _uiState.value.imageSourceMode,
                uploadedImageUrl = uploadedImageUrl,
            )

            val result = when (val currentMode = _uiState.value.mode) {
                is GameFormMode.Create -> gamesRepository.createGame(draft)
                is GameFormMode.Edit -> gamesRepository.updateGame(currentMode.gameId, draft)
            }

            result.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        completedMessage = if (state.isEditMode) "Jeu mis à jour." else "Jeu créé.",
                    )
                }
            }.onFailure { error ->
                val presentation = mapGameFormSaveError(
                    throwable = error,
                    isEditMode = _uiState.value.isEditMode,
                )
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        fields = state.fields.withFieldErrors(presentation.fieldErrors),
                        errorMessage = presentation.bannerMessage,
                    )
                }
            }
        }
    }
    fun consumeCompletion() = _uiState.update { state -> state.copy(completedMessage = null) }
    fun dismissErrorMessage() = _uiState.update { state -> state.copy(errorMessage = null) }
    private suspend fun uploadSelectedImage(): String? {
        val selection = checkNotNull(_uiState.value.localImageSelection)
        return gamesRepository.uploadGameImage(
            fileName = selection.fileName,
            mimeType = selection.mimeType,
            bytes = selection.bytes,
        ).getOrElse { error ->
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    errorMessage = error.localizedMessage ?: "Impossible d'envoyer l'image du jeu.",
                )
            }
            null
        }
    }
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    errorMessage = null,
                    lookupErrorMessage = null,
                )
            }
            val lookups = lookupsLoader.load()
            _uiState.update { state ->
                state.copy(
                    availableTypes = lookups.availableTypes,
                    availableEditors = lookups.availableEditors,
                    availableMechanisms = lookups.availableMechanisms,
                    lookupErrorMessage = lookups.errorMessage,
                )
            }
            val createMode = mode as? GameFormMode.Create
            if (createMode?.prefilledEditorId != null) {
                _uiState.update { state ->
                    state.copy(
                        fields = state.fields.copy(
                            editorId = createMode.prefilledEditorId,
                            editorError = null,
                        ),
                    )
                }
            }
            val editMode = mode as? GameFormMode.Edit
            if (editMode != null) {
                gamesRepository.getGame(editMode.gameId)
                    .onSuccess { game ->
                        _uiState.update { state ->
                            state.copy(
                                fields = prefillMapper.toFields(game),
                                imageSourceMode = GameImageSourceMode.Url,
                                localImageSelection = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update { state ->
                            state.copy(
                                errorMessage = mapGameFormLoadError(error),
                            )
                        }
                    }
            }
            _uiState.update { state -> state.copy(isLoading = false) }
        }
    }
    private fun updateFields(transform: GameFormFields.() -> GameFormFields) {
        _uiState.update { state ->
            state.copy(
                fields = state.fields.transform(),
                errorMessage = null,
            )
        }
    }
    private fun updateNumericField(value: String, transform: GameFormFields.() -> GameFormFields) {
        if (value.isNotEmpty() && value.any { !it.isDigit() }) {
            return
        }
        updateFields(transform)
    }
}
