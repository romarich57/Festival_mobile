/**
 * Rôle : Porte l'état et la logique du module le profil pour l'écran Compose associé.
 */

package com.projetmobile.mobile.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.repository.RepositoryException
import com.projetmobile.mobile.data.repository.RepositoryFailureKind
import com.projetmobile.mobile.data.repository.isOfflineFriendlyFailure
import com.projetmobile.mobile.data.repository.profile.ProfileRepository
import com.projetmobile.mobile.ui.utils.validation.AuthFormValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Rôle : Gère l'édition du profil (Pseudo, Mail, Avatars, Suppression de compte) et garde la session locale à jour temporairement.
 *
 * Précondition : Construit en injectant l'état utilisateur courant depuis le flux de configuration.
 *
 * Postcondition : Affiche les chargements partiels [uiState] pendant les mutators HTTP et rafraichit la liste des tokens ou des données applicatives de manière synchrone.
 */
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

    /**
     * Rôle : Rafraîchit profil.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
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
                        // On préserve les champs en cours d'édition pour ne pas écraser une saisie non encore validée.
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
                        // Un message hors-ligne n'est affiché que si un profil existe déjà en cache local.
                        val repositoryException = error as? RepositoryException
                        val shouldShowOfflineInfo = state.profile != null &&
                            repositoryException?.kind?.isOfflineFriendlyFailure() == true
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            infoMessage = if (shouldShowOfflineInfo) {
                                error.localizedMessage ?: "Mode hors-ligne: profil local affiché."
                            } else {
                                state.infoMessage
                            },
                            errorMessage = if (shouldShowOfflineInfo) {
                                null
                            } else {
                                error.localizedMessage ?: "Impossible de récupérer le profil."
                            },
                        )
                    }
                }
        }
    }

    /**
     * Rôle : Démarre editing.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
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

    /**
     * Rôle : Démarre editing champ.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun startEditingField(field: ProfileEditableField) {
        _uiState.update { state ->
            state.copy(
                editingFields = state.editingFields + field,
                infoMessage = null,
                errorMessage = null,
            ).recalculated()
        }
    }

    /**
     * Rôle : Annule editing.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
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

    /**
     * Rôle : Gère la modification du champ identifiant.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onLoginChanged(value: String) = updateForm { copy(login = value, loginError = null) }

    /**
     * Rôle : Gère la modification du champ first name.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onFirstNameChanged(value: String) = updateForm { copy(firstName = value, firstNameError = null) }

    /**
     * Rôle : Gère la modification du champ last name.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onLastNameChanged(value: String) = updateForm { copy(lastName = value, lastNameError = null) }

    /**
     * Rôle : Gère la modification du champ email.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onEmailChanged(value: String) = updateForm { copy(email = value, emailError = null) }

    /**
     * Rôle : Gère la modification du champ téléphone.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onPhoneChanged(value: String) = updateForm { copy(phone = value, phoneError = null) }

    /**
     * Rôle : Gère la sélection de avatar.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onAvatarSelected(selection: AvatarSelectionPayload) {
        val validationMessage = validateAvatarSelection(selection)
        if (validationMessage != null) {
            // Une sélection invalide est rejetée avant toute mise à jour de l'état visuel.
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

    /**
     * Rôle : Supprime avatar.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun removeAvatar() {
        _uiState.update { state ->
            state.copy(
                avatarState = nextAvatarStateAfterRemoval(state.profile, state.avatarState),
                errorMessage = null,
                infoMessage = null,
            ).recalculated()
        }
    }

    /**
     * Rôle : Exécute l'action send mot de passe réinitialisation link du module le profil.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun sendPasswordResetLink() {
        val email = _uiState.value.profile?.email?.trim().orEmpty()
        if (email.isBlank()) {
            // Sans email de profil, on s'arrête immédiatement pour éviter un appel réseau inutile.
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

    /**
     * Rôle : Enregistre profil.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
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
            // Les erreurs de validation sont publiées localement avant toute tentative de persistance.
            _uiState.update { state -> state.withValidationErrors(validation) }
            return
        }
        if (!currentState.hasPendingChanges) {
            // Aucun changement réel ne doit déclencher une requête d'écriture.
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
                    // L'avatar local est d'abord téléversé pour obtenir une URL réutilisable dans la mise à jour.
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
                // Si les données agrégées deviennent incohérentes, on abandonne la sauvegarde sans persister d'état partiel.
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

    /**
     * Rôle : Consomme en attente session utilisateur mise à jour.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun consumePendingSessionUserUpdate() {
        _uiState.update { state ->
            state.copy(pendingSessionUserUpdate = null)
        }
    }

    /**
     * Rôle : Ferme information message.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun dismissInfoMessage() {
        _uiState.update { state ->
            state.copy(infoMessage = null)
        }
    }

    /**
     * Rôle : Exécute l'action mise à jour formulaire du module le profil.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
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
