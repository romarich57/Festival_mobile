package com.projetmobile.mobile.ui.screens.admin.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.entity.admin.AdminUserCreateInput
import com.projetmobile.mobile.data.entity.admin.AdminUserUpdateInput
import com.projetmobile.mobile.data.repository.admin.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Rôle : Gère la création et modification des profils d'utilisateurs par l'administrateur.
 *
 * Précondition : Un `adminRepository` doit être initialisé par le constructeur global et gère les droits.
 *
 * Postcondition : Vérifie la saisie des formulaires et applique les appels de mutation correspondants.
 */
internal class AdminUserFormViewModel(
    private val adminRepository: AdminRepository,
    private val mode: AdminUserFormMode,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUserFormUiState(mode = mode))
    val uiState: StateFlow<AdminUserFormUiState> = _uiState.asStateFlow()

    init {
        if (mode is AdminUserFormMode.Edit) loadUser(mode.userId)
    }

    private fun loadUser(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            adminRepository.getUserById(userId)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            initialUser = user,
                            form = formStateFrom(user),
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Impossible de charger l'utilisateur.",
                        )
                    }
                }
        }
    }

    fun onLoginChanged(value: String) = updateForm { copy(login = value, loginError = null) }
    fun onPasswordChanged(value: String) = updateForm { copy(password = value, passwordError = null) }
    fun onFirstNameChanged(value: String) = updateForm { copy(firstName = value, firstNameError = null) }
    fun onLastNameChanged(value: String) = updateForm { copy(lastName = value, lastNameError = null) }
    fun onEmailChanged(value: String) = updateForm { copy(email = value, emailError = null) }
    fun onPhoneChanged(value: String) = updateForm { copy(phone = value) }
    fun onRoleSelected(role: String) = updateForm { copy(role = role) }
    fun onEmailVerifiedChanged(value: Boolean) = updateForm { copy(emailVerified = value) }

    fun submit() {
        val form = _uiState.value.form
        if (!validate(form)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (mode) {
                is AdminUserFormMode.Create -> createUser(form)
                is AdminUserFormMode.Edit -> updateUser(mode.userId, form)
            }
        }
    }

    private suspend fun createUser(form: AdminUserFormState) {
        adminRepository.createUser(
            AdminUserCreateInput(
                login = form.login.trim(),
                password = form.password,
                firstName = form.firstName.trim(),
                lastName = form.lastName.trim(),
                email = form.email.trim(),
                phone = form.phone.trim().takeIf { it.isNotBlank() },
                role = form.role,
            ),
        )
            .onSuccess {
                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = error.localizedMessage ?: "Impossible de créer l'utilisateur.")
                }
            }
    }

    private suspend fun updateUser(userId: Int, form: AdminUserFormState) {
        adminRepository.updateUser(
            id = userId,
            input = AdminUserUpdateInput(
                login = form.login.trim(),
                firstName = form.firstName.trim(),
                lastName = form.lastName.trim(),
                email = form.email.trim(),
                phone = form.phone.trim().takeIf { it.isNotBlank() },
                role = form.role,
                emailVerified = form.emailVerified,
            ),
        )
            .onSuccess {
                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = error.localizedMessage ?: "Impossible de mettre à jour l'utilisateur.")
                }
            }
    }

    private fun validate(form: AdminUserFormState): Boolean {
        var valid = true
        var updated = form

        if (form.login.isBlank()) {
            updated = updated.copy(loginError = "Identifiant requis")
            valid = false
        }
        if (mode is AdminUserFormMode.Create && form.password.length < 8) {
            updated = updated.copy(passwordError = "Mot de passe minimum 8 caractères")
            valid = false
        }
        if (form.firstName.isBlank()) {
            updated = updated.copy(firstNameError = "Prénom requis")
            valid = false
        }
        if (form.lastName.isBlank()) {
            updated = updated.copy(lastNameError = "Nom requis")
            valid = false
        }
        val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        if (!emailRegex.matches(form.email.trim())) {
            updated = updated.copy(emailError = "Email invalide")
            valid = false
        }

        if (!valid) _uiState.update { it.copy(form = updated) }
        return valid
    }

    private fun updateForm(transform: AdminUserFormState.() -> AdminUserFormState) {
        _uiState.update { it.copy(form = it.form.transform()) }
    }

    companion object {
        fun factory(
            adminRepository: AdminRepository,
            mode: AdminUserFormMode,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { AdminUserFormViewModel(adminRepository, mode) }
        }
    }
}
