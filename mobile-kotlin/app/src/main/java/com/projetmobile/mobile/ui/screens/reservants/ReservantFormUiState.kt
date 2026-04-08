/**
 * Rôle : Décrit l'état UI immuable du module les réservants.
 */

package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantEditorOption

/**
 * Rôle : Définit le contrat du module les réservants.
 */
sealed interface ReservantFormMode {
    /**
     * Rôle : Expose un singleton de support pour le module les réservants.
     */
    data object Create : ReservantFormMode

    /**
     * Rôle : Décrit le composant édition du module les réservants.
     */
    data class Edit(val reservantId: Int) : ReservantFormMode
}

/**
 * Rôle : Décrit le composant réservant formulaire champs du module les réservants.
 */
internal data class ReservantFormFields(
    val name: String = "",
    val nameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val type: String? = null,
    val typeError: String? = null,
    val linkedEditorId: Int? = null,
    val linkedEditorError: String? = null,
    val phoneNumber: String = "",
    val phoneNumberError: String? = null,
    val address: String = "",
    val siret: String = "",
    val notes: String = "",
) {
    /**
     * Rôle : Retourne l'état enrichi de champ erreurs.
     *
     * Précondition : Les données du module doivent être disponibles pour initialiser ou exposer l'état.
     *
     * Postcondition : L'objet retourné décrit un état cohérent et immuable.
     */
    fun withFieldErrors(errors: ReservantFormFieldErrors): ReservantFormFields {
        return copy(
            nameError = errors.nameError,
            emailError = errors.emailError,
            typeError = errors.typeError,
            linkedEditorError = errors.linkedEditorError,
            phoneNumberError = errors.phoneNumberError,
        )
    }
}

/**
 * Rôle : Décrit le composant réservant formulaire snapshot du module les réservants.
 */
internal data class ReservantFormSnapshot(
    val type: String? = null,
    val linkedEditorId: Int? = null,
    val address: String? = null,
    val siret: String? = null,
)

/**
 * Rôle : L'état complet du formulaire de saisie pour un éditeur ou un individu.
 *
 * Précondition : Centralisé pour contenir les champs avec leurs erreurs respectives.
 *
 * Postcondition : Garantit qu'un modèle de données correspond toujours à l'UI sans altération directe.
 */
internal data class ReservantFormUiState(
    val mode: ReservantFormMode,
    val fields: ReservantFormFields = ReservantFormFields(),
    val snapshot: ReservantFormSnapshot = ReservantFormSnapshot(),
    val availableEditors: List<ReservantEditorOption> = emptyList(),
    val canManageReservants: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val lookupErrorMessage: String? = null,
    val errorMessage: String? = null,
    val completedMessage: String? = null,
    val completedReservantId: Int? = null,
) {
    val isEditMode: Boolean
        get() = mode is ReservantFormMode.Edit

    val shouldShowEditorSelector: Boolean
        get() = fields.type == ReservantTypeChoice.Editor.value
}
