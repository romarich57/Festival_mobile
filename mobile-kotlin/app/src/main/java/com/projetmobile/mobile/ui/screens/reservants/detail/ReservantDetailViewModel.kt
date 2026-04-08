package com.projetmobile.mobile.ui.screens.reservants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.projetmobile.mobile.data.entity.reservants.ReservantContactDraft
import com.projetmobile.mobile.data.entity.reservants.canManageReservants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ReservantDetailViewModel(
    private val reservantId: Int,
    private val observeReservant: ReservantObserver,
    private val loadReservant: ReservantLoader,
    private val loadContacts: ReservantContactsLoader,
    private val addContact: ReservantContactCreator,
    private val loadGames: ReservantGamesLoader,
    currentUserRole: String?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ReservantDetailUiState(
            reservantId = reservantId,
            canManageReservants = canManageReservants(currentUserRole),
        ),
    )
    val uiState: StateFlow<ReservantDetailUiState> = _uiState.asStateFlow()

    init {
        startObservingLocalReservant()
        refreshReservant()
    }

    private fun startObservingLocalReservant() {
        viewModelScope.launch {
            observeReservant(reservantId).collectLatest { localReservant ->
                if (localReservant == null) {
                    return@collectLatest
                }
                _uiState.update { state ->
                    state.copy(
                        reservant = localReservant,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun selectTab(tab: ReservantDetailTab) {
        _uiState.update { state -> state.copy(activeTab = tab) }
    }

    fun refreshReservant() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = state.reservant == null,
                    errorMessage = null,
                    contactsErrorMessage = null,
                    gamesErrorMessage = null,
                )
            }
            loadReservant(reservantId)
                .onSuccess { reservant ->
                    _uiState.update { state ->
                        state.copy(
                            reservant = reservant,
                            isLoading = false,
                        )
                    }
                    refreshContacts()
                    refreshGames(reservant.editorId)
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = mapReservantDetailError(error),
                        )
                    }
                }
        }
    }

    fun refreshContacts() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoadingContacts = true,
                    contactsErrorMessage = null,
                )
            }
            loadContacts(reservantId)
                .onSuccess { contacts ->
                    _uiState.update { state ->
                        state.copy(
                            contacts = contacts.sortedByDescending { it.priority },
                            isLoadingContacts = false,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoadingContacts = false,
                            contactsErrorMessage = mapReservantContactsLoadError(error),
                        )
                    }
                }
        }
    }

    fun refreshGames(editorId: Int? = _uiState.value.linkedEditorId) {
        if (editorId == null) {
            _uiState.update { state ->
                state.copy(
                    games = emptyList(),
                    isLoadingGames = false,
                    gamesErrorMessage = null,
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoadingGames = true,
                    gamesErrorMessage = null,
                )
            }
            loadGames(editorId)
                .onSuccess { games ->
                _uiState.update { state ->
                    state.copy(
                        games = games,
                        isLoadingGames = false,
                    )
                }
            }
                .onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        isLoadingGames = false,
                        gamesErrorMessage = mapReservantGamesLoadError(error),
                    )
                }
            }
        }
    }

    fun toggleContactForm() {
        _uiState.update { state ->
            state.copy(
                isContactFormExpanded = !state.isContactFormExpanded,
                contactForm = if (state.isContactFormExpanded) {
                    ReservantContactFormFields()
                } else {
                    state.contactForm
                },
            )
        }
    }

    fun onContactNameChanged(value: String) = updateContactForm { copy(name = value, nameError = null) }

    fun onContactEmailChanged(value: String) = updateContactForm { copy(email = value, emailError = null) }

    fun onContactPhoneNumberChanged(value: String) = updateContactForm {
        copy(phoneNumber = value, phoneNumberError = null)
    }

    fun onContactJobTitleChanged(value: String) = updateContactForm {
        copy(jobTitle = value, jobTitleError = null)
    }

    fun onContactPrioritySelected(value: Int) = updateContactForm { copy(priority = value) }

    fun saveContact() {
        val validationErrors = validateReservantContact(_uiState.value.contactForm)
        if (validationErrors.hasAny()) {
            _uiState.update { state ->
                state.copy(
                    isContactFormExpanded = true,
                    contactForm = state.contactForm.withFieldErrors(validationErrors),
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isSavingContact = true,
                    contactsErrorMessage = null,
                )
            }
            addContact(reservantId, _uiState.value.contactForm.toDraft())
                .onSuccess { contact ->
                _uiState.update { state ->
                    state.copy(
                        contacts = (listOf(contact) + state.contacts).sortedByDescending { it.priority },
                        contactForm = ReservantContactFormFields(),
                        isSavingContact = false,
                        isContactFormExpanded = false,
                        infoMessage = "Contact ajouté.",
                    )
                }
            }.onFailure { error ->
                val presentation = mapReservantContactSaveError(error)
                _uiState.update { state ->
                    state.copy(
                        contactForm = state.contactForm.withFieldErrors(presentation.fieldErrors),
                        isSavingContact = false,
                        isContactFormExpanded = true,
                        contactsErrorMessage = presentation.bannerMessage,
                    )
                }
            }
        }
    }

    fun dismissInfoMessage() {
        _uiState.update { state -> state.copy(infoMessage = null) }
    }

    fun dismissErrorMessage() {
        _uiState.update { state -> state.copy(errorMessage = null) }
    }

    fun dismissContactsErrorMessage() {
        _uiState.update { state -> state.copy(contactsErrorMessage = null) }
    }

    fun dismissGamesErrorMessage() {
        _uiState.update { state -> state.copy(gamesErrorMessage = null) }
    }

    fun showInfoMessage(message: String?) {
        if (message.isNullOrBlank()) {
            return
        }
        _uiState.update { state -> state.copy(infoMessage = message) }
    }

    private fun updateContactForm(
        transform: ReservantContactFormFields.() -> ReservantContactFormFields,
    ) {
        _uiState.update { state ->
            state.copy(
                contactForm = state.contactForm.transform(),
                contactsErrorMessage = null,
            )
        }
    }
}

private fun ReservantContactFormFields.toDraft(): ReservantContactDraft {
    return ReservantContactDraft(
        name = name.trim(),
        email = email.trim(),
        phoneNumber = phoneNumber.trim(),
        jobTitle = jobTitle.trim(),
        priority = priority,
    )
}

internal fun reservantDetailViewModelFactory(
    reservantId: Int,
    observeReservant: ReservantObserver,
    loadReservant: ReservantLoader,
    loadContacts: ReservantContactsLoader,
    addContact: ReservantContactCreator,
    loadGames: ReservantGamesLoader,
    currentUserRole: String?,
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReservantDetailViewModel(
                reservantId = reservantId,
                observeReservant = observeReservant,
                loadReservant = loadReservant,
                loadContacts = loadContacts,
                addContact = addContact,
                loadGames = loadGames,
                currentUserRole = currentUserRole,
            ) as T
        }
    }
}
