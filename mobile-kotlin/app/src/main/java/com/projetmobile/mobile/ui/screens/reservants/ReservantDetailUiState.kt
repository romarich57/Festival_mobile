/**
 * Rôle : Décrit l'état UI immuable du module les réservants.
 */

package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.reservants.ReservantContact
import com.projetmobile.mobile.data.entity.reservants.ReservantDetail

/**
 * Rôle : Décrit le composant réservant détail onglet du module les réservants.
 */
internal enum class ReservantDetailTab(val label: String) {
    Infos("Infos"),
    Contacts("Contacts"),
    Jeux("Jeux"),
}

/**
 * Rôle : Décrit le composant réservant contact formulaire champs du module les réservants.
 */
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
    /**
     * Rôle : Retourne l'état enrichi de champ erreurs.
     *
     * Précondition : Les données du module doivent être disponibles pour initialiser ou exposer l'état.
     *
     * Postcondition : L'objet retourné décrit un état cohérent et immuable.
     */
    fun withFieldErrors(errors: ReservantContactFieldErrors): ReservantContactFormFields {
        return copy(
            nameError = errors.nameError,
            emailError = errors.emailError,
            phoneNumberError = errors.phoneNumberError,
            jobTitleError = errors.jobTitleError,
        )
    }
}

/**
 * Rôle : Modèle de la vue de détail liée à un Réservant.
 *
 * Précondition : Construit continuellement par le [ReservantDetailViewModel].
 *
 * Postcondition : Stocke l'option de l'onglet actif et les données potentiellement complètes du réservant.
 */
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
