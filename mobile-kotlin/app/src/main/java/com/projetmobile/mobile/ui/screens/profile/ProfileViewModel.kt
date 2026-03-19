package com.projetmobile.mobile.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.repository.profile.ProfileRepository
import com.projetmobile.mobile.ui.utils.validation.AuthFormValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    initialUser: AuthUser?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProfileUiState(
            profile = initialUser,
            form = profileFormStateFor(initialUser),
            isLoading = initialUser == null,
            isRefreshing = initialUser != null,
        ).recalculated(),
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = state.profile == null,
                    isRefreshing = state.profile != null,
                    errorMessage = null,
                )
            }

            profileRepository.getProfile()
                .onSuccess { user ->
                    _uiState.update { state ->
                        val nextForm = if (state.isEditing) state.form else profileFormStateFor(user)
                        state.copy(
                            profile = user,
                            form = nextForm,
                            isLoading = false,
                            isRefreshing = false,
                            pendingSessionUserUpdate = user,
                        ).recalculated()
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = error.localizedMessage
                                ?: "Impossible de récupérer le profil.",
                        )
                    }
                }
        }
    }

    fun startEditing() {
        _uiState.update { state ->
            state.copy(
                editingFields = ProfileEditableField.entries.toSet(),
                form = profileFormStateFor(state.profile),
                avatarState = ProfileAvatarState.Unchanged,
                infoMessage = null,
                errorMessage = null,
            ).recalculated()
        }
    }

    fun startEditingField(field: ProfileEditableField) {
        _uiState.update { state ->
            state.copy(
                editingFields = state.editingFields + field,
                infoMessage = null,
                errorMessage = null,
            ).recalculated()
        }
    }

    fun cancelEditing() {
        _uiState.update { state ->
            state.copy(
                editingFields = emptySet(),
                form = profileFormStateFor(state.profile),
                avatarState = ProfileAvatarState.Unchanged,
                infoMessage = null,
                errorMessage = null,
            ).recalculated()
        }
    }

    fun onLoginChanged(value: String) = updateForm { copy(login = value, loginError = null) }

    fun onFirstNameChanged(value: String) = updateForm { copy(firstName = value, firstNameError = null) }

    fun onLastNameChanged(value: String) = updateForm { copy(lastName = value, lastNameError = null) }

    fun onEmailChanged(value: String) = updateForm { copy(email = value, emailError = null) }

    fun onPhoneChanged(value: String) = updateForm { copy(phone = value, phoneError = null) }

    fun onAvatarSelected(selection: AvatarSelectionPayload) {
        val validationMessage = validateAvatarSelection(selection)
        if (validationMessage != null) {
            _uiState.update { state ->
                state.copy(
                    errorMessage = validationMessage,
                    infoMessage = null,
                )
            }
            return
        }

        _uiState.update { state ->
            state.copy(
                avatarState = selection.toAvatarState(),
                errorMessage = null,
                infoMessage = null,
            ).recalculated()
        }
    }

    fun removeAvatar() {
        _uiState.update { state ->
            state.copy(
                avatarState = nextAvatarStateAfterRemoval(state.profile, state.avatarState),
                errorMessage = null,
                infoMessage = null,
            ).recalculated()
        }
    }

    fun sendPasswordResetLink() {
        val email = _uiState.value.profile?.email?.trim().orEmpty()
        if (email.isBlank()) {
            _uiState.update { state ->
                state.copy(errorMessage = "Aucun email disponible pour envoyer le lien.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isSendingPasswordReset = true,
                    errorMessage = null,
                    infoMessage = null,
                )
            }

            profileRepository.requestPasswordReset(email)
                .onSuccess { message ->
                    _uiState.update { state ->
                        state.copy(
                            isSendingPasswordReset = false,
                            infoMessage = message,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isSendingPasswordReset = false,
                            errorMessage = error.localizedMessage
                                ?: "Impossible d'envoyer le lien de réinitialisation.",
                        )
                    }
                }
        }
    }

    fun saveProfile() {
        val currentState = _uiState.value
        val currentProfile = currentState.profile ?: return
        val validation = AuthFormValidator.validateProfileUpdate(
            username = currentState.form.login,
            firstName = currentState.form.firstName,
            lastName = currentState.form.lastName,
            email = currentState.form.email,
            phone = currentState.form.phone,
        )
        if (validation.isInvalid) {
            _uiState.update { state -> state.withValidationErrors(validation) }
            return
        }
        if (!currentState.hasPendingChanges) {
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isSaving = true,
                    errorMessage = null,
                    infoMessage = null,
                )
            }

            val uploadedAvatarUrl = when (val avatarState = _uiState.value.avatarState) {
                is ProfileAvatarState.LocalSelection -> {
                    profileRepository.uploadAvatar(
                        fileName = avatarState.fileName,
                        mimeType = avatarState.mimeType,
                        bytes = avatarState.bytes,
                    ).getOrElse { error ->
                        _uiState.update { state ->
                            state.copy(
                                isSaving = false,
                                errorMessage = error.localizedMessage ?: "Impossible d'envoyer l'avatar.",
                            )
                        }
                        return@launch
                    }.url
                }

                else -> null
            }

            val updateInput = buildProfileUpdateInput(
                savedProfile = currentProfile,
                form = _uiState.value.form,
                avatarState = _uiState.value.avatarState,
                uploadedAvatarUrl = uploadedAvatarUrl,
            )
            if (updateInput == null) {
                _uiState.update { state ->
                    state.copy(isSaving = false).recalculated()
                }
                return@launch
            }

            profileRepository.updateProfile(updateInput)
                .onSuccess { result ->
                    _uiState.update { state ->
                        state.copy(
                            profile = result.user,
                            form = profileFormStateFor(result.user),
                            avatarState = ProfileAvatarState.Unchanged,
                            editingFields = emptySet(),
                            isSaving = false,
                            infoMessage = result.message,
                            errorMessage = null,
                            pendingSessionUserUpdate = result.user,
                        ).recalculated()
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(isSaving = false)
                            .withBackendProfileError(
                                error.localizedMessage
                                    ?: "Impossible de mettre à jour le profil.",
                            )
                    }
                }
        }
    }

    fun consumePendingSessionUserUpdate() {
        _uiState.update { state ->
            state.copy(pendingSessionUserUpdate = null)
        }
    }

    fun dismissInfoMessage() {
        _uiState.update { state ->
            state.copy(infoMessage = null)
        }
    }

    private fun updateForm(transform: ProfileFormState.() -> ProfileFormState) {
        _uiState.update { state ->
            state.copy(
                form = state.form.transform(),
                errorMessage = null,
                infoMessage = null,
            ).recalculated()
        }
    }

}
