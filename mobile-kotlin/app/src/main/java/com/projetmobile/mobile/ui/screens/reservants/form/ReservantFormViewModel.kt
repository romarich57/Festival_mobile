package com.projetmobile.mobile.ui.screens.reservants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.projetmobile.mobile.data.entity.reservants.ReservantDetail
import com.projetmobile.mobile.data.entity.reservants.ReservantDraft
import com.projetmobile.mobile.data.entity.reservants.canManageReservants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ReservantFormViewModel(
    private val loadEditors: ReservantEditorsLoader,
    private val loadReservant: ReservantLoader,
    private val createReservant: ReservantSave,
    private val updateReservant: ReservantUpdate,
    private val mode: ReservantFormMode,
    currentUserRole: String?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ReservantFormUiState(
            mode = mode,
            canManageReservants = canManageReservants(currentUserRole),
        ),
    )
    val uiState: StateFlow<ReservantFormUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun onNameChanged(value: String) = updateFields { copy(name = value, nameError = null) }

    fun onEmailChanged(value: String) = updateFields { copy(email = value, emailError = null) }

    fun onTypeSelected(value: String?) = updateFields {
        copy(
            type = value,
            typeError = null,
            linkedEditorId = if (value == "editeur") linkedEditorId else null,
            linkedEditorError = null,
        )
    }

    fun onLinkedEditorSelected(value: Int?) = updateFields {
        copy(linkedEditorId = value, linkedEditorError = null)
    }

    fun onPhoneNumberChanged(value: String) = updateFields {
        copy(phoneNumber = value, phoneNumberError = null)
    }

    fun onAddressChanged(value: String) = updateFields { copy(address = value) }

    fun onSiretChanged(value: String) = updateFields { copy(siret = value) }

    fun onNotesChanged(value: String) = updateFields { copy(notes = value) }

    fun saveReservant() {
        val validationErrors = validateReservantForm(
            fields = _uiState.value.fields,
            isEditMode = _uiState.value.isEditMode,
        )
        if (validationErrors.hasAny()) {
            _uiState.update { state ->
                state.copy(fields = state.fields.withFieldErrors(validationErrors))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isSaving = true,
                    errorMessage = null,
                    fields = state.fields.withFieldErrors(
                        validateReservantForm(state.fields, state.isEditMode),
                    ),
                )
            }
            val draft = _uiState.value.fields.toDraft()
            val result = when (val currentMode = mode) {
                ReservantFormMode.Create -> createReservant(draft)
                is ReservantFormMode.Edit -> updateReservant(currentMode.reservantId, draft)
            }

            result.onSuccess { reservant ->
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        completedMessage = if (state.isEditMode) {
                            "Réservant mis à jour."
                        } else {
                            "Réservant créé."
                        },
                        completedReservantId = reservant.id,
                    )
                }
            }.onFailure { error ->
                val presentation = mapReservantFormSaveError(
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

    fun consumeCompletion() {
        _uiState.update { state ->
            state.copy(
                completedMessage = null,
                completedReservantId = null,
            )
        }
    }

    fun dismissErrorMessage() {
        _uiState.update { state -> state.copy(errorMessage = null) }
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

            loadEditors()
                .onSuccess { editors ->
                    _uiState.update { state -> state.copy(availableEditors = editors) }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(lookupErrorMessage = "Impossible de charger les éditeurs.")
                    }
                }

            val editMode = mode as? ReservantFormMode.Edit
            if (editMode != null) {
                loadReservant(editMode.reservantId)
                    .onSuccess { reservant ->
                        _uiState.update { state ->
                            state.copy(
                                fields = reservant.toFields(),
                                snapshot = reservant.toSnapshot(),
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update { state ->
                            state.copy(errorMessage = mapReservantFormLoadError(error))
                        }
                    }
            }

            _uiState.update { state -> state.copy(isLoading = false) }
        }
    }

    private fun updateFields(transform: ReservantFormFields.() -> ReservantFormFields) {
        _uiState.update { state ->
            state.copy(
                fields = state.fields.transform(),
                errorMessage = null,
            )
        }
    }
}

private fun ReservantFormFields.toDraft(): ReservantDraft {
    return ReservantDraft(
        name = name.trim(),
        email = email.trim(),
        type = type.orEmpty(),
        editorId = linkedEditorId,
        phoneNumber = phoneNumber.trim().ifBlank { null },
        address = address.trim().ifBlank { null },
        siret = siret.trim().ifBlank { null },
        notes = notes.trim().ifBlank { null },
    )
}

private fun ReservantDetail.toFields(): ReservantFormFields {
    return ReservantFormFields(
        name = name,
        email = email,
        type = type,
        linkedEditorId = editorId,
        phoneNumber = phoneNumber.orEmpty(),
        address = address.orEmpty(),
        siret = siret.orEmpty(),
        notes = notes.orEmpty(),
    )
}

private fun ReservantDetail.toSnapshot(): ReservantFormSnapshot {
    return ReservantFormSnapshot(
        type = type,
        linkedEditorId = editorId,
        address = address,
        siret = siret,
    )
}

internal fun reservantFormViewModelFactory(
    mode: ReservantFormMode,
    loadEditors: ReservantEditorsLoader,
    loadReservant: ReservantLoader,
    createReservant: ReservantSave,
    updateReservant: ReservantUpdate,
    currentUserRole: String?,
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReservantFormViewModel(
                mode = mode,
                loadEditors = loadEditors,
                loadReservant = loadReservant,
                createReservant = createReservant,
                updateReservant = updateReservant,
                currentUserRole = currentUserRole,
            ) as T
        }
    }
}
