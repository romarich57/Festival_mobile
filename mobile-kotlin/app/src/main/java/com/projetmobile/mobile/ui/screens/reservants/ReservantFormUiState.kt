package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantEditorOption

sealed interface ReservantFormMode {
    data object Create : ReservantFormMode

    data class Edit(val reservantId: Int) : ReservantFormMode
}

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

internal data class ReservantFormSnapshot(
    val type: String? = null,
    val linkedEditorId: Int? = null,
    val address: String? = null,
    val siret: String? = null,
)

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
