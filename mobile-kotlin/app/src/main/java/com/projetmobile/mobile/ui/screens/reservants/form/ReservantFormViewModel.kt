/**
 * Rôle : Porte l'état et la logique du module les réservants formulaire pour l'écran Compose associé.
 */

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

/**
 * Rôle : Pré-remplit, valide et sauvegarde un formulaire lié à un Réservant.
 *
 * Précondition : [mode] permet de déclencher le loader idoine (création pure ou update reposant sur [id]).
 *
 * Postcondition : Informe des champs ou exceptions rencontrés, et notifie en cas de succès final.
 */
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

    /**
     * Rôle : Gère la modification du champ name.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onNameChanged(value: String) = updateFields { copy(name = value, nameError = null) }

    /**
     * Rôle : Gère la modification du champ email.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onEmailChanged(value: String) = updateFields { copy(email = value, emailError = null) }

    /**
     * Rôle : Gère la sélection de type.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onTypeSelected(value: String?) = updateFields {
        copy(
            type = value,
            typeError = null,
            linkedEditorId = if (value == "editeur") linkedEditorId else null,
            linkedEditorError = null,
        )
    }

    /**
     * Rôle : Gère la sélection de linked éditeur.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onLinkedEditorSelected(value: Int?) = updateFields {
        copy(linkedEditorId = value, linkedEditorError = null)
    }

    /**
     * Rôle : Gère la modification du champ téléphone number.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onPhoneNumberChanged(value: String) = updateFields {
        copy(phoneNumber = value, phoneNumberError = null)
    }

    /**
     * Rôle : Gère la modification du champ address.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onAddressChanged(value: String) = updateFields { copy(address = value) }

    /**
     * Rôle : Gère la modification du champ siret.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onSiretChanged(value: String) = updateFields { copy(siret = value) }

    /**
     * Rôle : Gère la modification du champ notes.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onNotesChanged(value: String) = updateFields { copy(notes = value) }

    /**
     * Rôle : Enregistre réservant.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
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

    /**
     * Rôle : Consomme completion.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun consumeCompletion() {
        _uiState.update { state ->
            state.copy(
                completedMessage = null,
                completedReservantId = null,
            )
        }
    }

    /**
     * Rôle : Ferme erreur message.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun dismissErrorMessage() {
        _uiState.update { state -> state.copy(errorMessage = null) }
    }

    /**
     * Rôle : Charge initial data.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
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

    /**
     * Rôle : Exécute l'action mise à jour champs du module les réservants formulaire.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun updateFields(transform: ReservantFormFields.() -> ReservantFormFields) {
        _uiState.update { state ->
            state.copy(
                fields = state.fields.transform(),
                errorMessage = null,
            )
        }
    }
}

/**
 * Rôle : Convertit les champs du formulaire en brouillon de réservant prêt pour le repository.
 *
 * Précondition : Le formulaire doit déjà être validé côté UI.
 *
 * Postcondition : Retourne un objet métier nettoyé, avec les champs optionnels réduits à `null` quand ils sont vides.
 */
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

/**
 * Rôle : Projette une fiche détaillée de réservant dans l'état de formulaire éditable.
 *
 * Précondition : `ReservantDetail` doit contenir les valeurs actuellement persistées côté serveur.
 *
 * Postcondition : Retourne un état de formulaire prérempli prêt à être affiché ou modifié.
 */
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

/**
 * Rôle : Capture les champs de référence d'un réservant pour détecter les changements structurels.
 *
 * Précondition : La fiche détaillée doit être disponible pour extraire les valeurs de comparaison.
 *
 * Postcondition : Retourne un snapshot immuable utilisé pour savoir si le type, l'éditeur ou l'adresse ont changé.
 */
private fun ReservantDetail.toSnapshot(): ReservantFormSnapshot {
    return ReservantFormSnapshot(
        type = type,
        linkedEditorId = editorId,
        address = address,
        siret = siret,
    )
}

/**
 * Rôle : Exécute l'action réservant formulaire vue modèle factory du module les réservants formulaire.
 *
 * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
 *
 * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
 */
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
