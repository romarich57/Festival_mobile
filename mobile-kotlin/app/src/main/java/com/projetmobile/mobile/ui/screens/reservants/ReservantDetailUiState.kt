package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.reservants.ReservantContact
import com.projetmobile.mobile.data.entity.reservants.ReservantDetail

internal enum class ReservantDetailTab(val label: String) {
    Infos("Infos"),
    Contacts("Contacts"),
    Jeux("Jeux"),
}

internal data class ReservantContactFormFields(
    val name: String = "",
    val nameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val phoneNumber: String = "",
    val phoneNumberError: String? = null,
    val jobTitle: String = "",
    val jobTitleError: String? = null,
    val priority: Int = 0,
) {
    fun withFieldErrors(errors: ReservantContactFieldErrors): ReservantContactFormFields {
        return copy(
            nameError = errors.nameError,
            emailError = errors.emailError,
            phoneNumberError = errors.phoneNumberError,
            jobTitleError = errors.jobTitleError,
        )
    }
}

internal data class ReservantDetailUiState(
    val reservantId: Int,
    val activeTab: ReservantDetailTab = ReservantDetailTab.Infos,
    val reservant: ReservantDetail? = null,
    val contacts: List<ReservantContact> = emptyList(),
    val games: List<GameListItem> = emptyList(),
    val contactForm: ReservantContactFormFields = ReservantContactFormFields(),
    val canManageReservants: Boolean = false,
    val isLoading: Boolean = true,
    val isLoadingContacts: Boolean = false,
    val isLoadingGames: Boolean = false,
    val isSavingContact: Boolean = false,
    val isContactFormExpanded: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val contactsErrorMessage: String? = null,
    val gamesErrorMessage: String? = null,
) {
    val linkedEditorId: Int?
        get() = reservant?.editorId
}
