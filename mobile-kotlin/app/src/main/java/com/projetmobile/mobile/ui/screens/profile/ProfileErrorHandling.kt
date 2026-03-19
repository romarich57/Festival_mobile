package com.projetmobile.mobile.ui.screens.profile

import java.text.Normalizer
import java.util.Locale

internal fun ProfileUiState.withBackendProfileError(rawMessage: String): ProfileUiState {
    val normalized = rawMessage.normalizedForMatching()
    return when {
        normalized.contains("login deja utilise") -> {
            copy(
                form = form.copy(loginError = "Ce pseudo est déjà utilisé."),
                editingFields = editingFields + ProfileEditableField.Login,
                errorMessage = null,
                infoMessage = null,
            ).recalculated()
        }

        normalized.contains("email deja utilise") -> {
            copy(
                form = form.copy(emailError = "Cet email est déjà utilisé."),
                editingFields = editingFields + ProfileEditableField.Email,
                errorMessage = null,
                infoMessage = null,
            ).recalculated()
        }

        else -> copy(
            errorMessage = rawMessage,
            infoMessage = null,
        ).recalculated()
    }
}

private fun String.normalizedForMatching(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .lowercase(Locale.ROOT)
}
