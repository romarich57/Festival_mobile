package com.projetmobile.mobile.ui.utils.validation

private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

object AuthFormValidator {
    fun validateLogin(identifier: String, password: String): LoginValidationResult {
        val identifierError = if (identifier.isBlank()) {
            "Veuillez renseigner votre email ou pseudo"
        } else {
            null
        }
        val passwordError = if (password.isBlank()) {
            "Veuillez renseigner votre mot de passe"
        } else {
            null
        }

        return LoginValidationResult(
            identifierError = identifierError,
            passwordError = passwordError,
        )
    }

    fun validateRegister(
        username: String,
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phone: String,
    ): RegisterValidationResult {
        val usernameError = if (username.isBlank()) "Le pseudo est obligatoire" else null
        val firstNameError = if (firstName.isBlank()) "Le prénom est obligatoire" else null
        val lastNameError = if (lastName.isBlank()) "Le nom est obligatoire" else null
        val emailError = when {
            email.isBlank() -> "L'email est obligatoire"
            !emailRegex.matches(email.trim()) -> "Veuillez saisir un email valide"
            else -> null
        }
        val passwordError = when {
            password.isBlank() -> "Le mot de passe est obligatoire"
            password.trim().length < 8 -> "Le mot de passe doit contenir au moins 8 caractères"
            else -> null
        }
        val phoneError = if (phone.isNotBlank() && phone.trim().length < 6) {
            "Le téléphone semble trop court"
        } else {
            null
        }

        return RegisterValidationResult(
            usernameError = usernameError,
            firstNameError = firstNameError,
            lastNameError = lastNameError,
            emailError = emailError,
            passwordError = passwordError,
            phoneError = phoneError,
        )
    }

    fun validateResendVerification(email: String): ResendVerificationValidationResult {
        val emailError = when {
            email.isBlank() -> "Veuillez saisir votre email"
            !emailRegex.matches(email.trim()) -> "Le renvoi nécessite une adresse email valide"
            else -> null
        }

        return ResendVerificationValidationResult(emailError = emailError)
    }

    fun validateForgotPassword(email: String): ForgotPasswordValidationResult {
        val emailError = when {
            email.isBlank() -> "Veuillez saisir l'email associé à votre compte"
            !emailRegex.matches(email.trim()) -> "Veuillez saisir un email valide"
            else -> null
        }

        return ForgotPasswordValidationResult(emailError = emailError)
    }

    fun validateResetPassword(
        password: String,
        confirmation: String,
    ): ResetPasswordValidationResult {
        val passwordError = when {
            password.isBlank() -> "Le nouveau mot de passe est obligatoire"
            password.trim().length < 8 -> "Le mot de passe doit contenir au moins 8 caractères"
            else -> null
        }
        val confirmationError = when {
            confirmation.isBlank() -> "La confirmation est obligatoire"
            confirmation != password -> "Les mots de passe ne correspondent pas"
            else -> null
        }

        return ResetPasswordValidationResult(
            passwordError = passwordError,
            confirmationError = confirmationError,
        )
    }

    fun validateProfileUpdate(
        username: String,
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
    ): ProfileUpdateValidationResult {
        val registerValidation = validateRegister(
            username = username,
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = "temporary-pass",
            phone = phone,
        )

        return ProfileUpdateValidationResult(
            usernameError = registerValidation.usernameError,
            firstNameError = registerValidation.firstNameError,
            lastNameError = registerValidation.lastNameError,
            emailError = registerValidation.emailError,
            phoneError = registerValidation.phoneError,
        )
    }
}

data class LoginValidationResult(
    val identifierError: String?,
    val passwordError: String?,
) {
    val isValid: Boolean = identifierError == null && passwordError == null
    val isInvalid: Boolean = !isValid
}

data class RegisterValidationResult(
    val usernameError: String?,
    val firstNameError: String?,
    val lastNameError: String?,
    val emailError: String?,
    val passwordError: String?,
    val phoneError: String?,
) {
    val isValid: Boolean = listOf(
        usernameError,
        firstNameError,
        lastNameError,
        emailError,
        passwordError,
        phoneError,
    ).all { error -> error == null }
    val isInvalid: Boolean = !isValid
}

data class ResendVerificationValidationResult(
    val emailError: String?,
) {
    val isValid: Boolean = emailError == null
    val isInvalid: Boolean = !isValid
}

data class ForgotPasswordValidationResult(
    val emailError: String?,
) {
    val isValid: Boolean = emailError == null
    val isInvalid: Boolean = !isValid
}

data class ResetPasswordValidationResult(
    val passwordError: String?,
    val confirmationError: String?,
) {
    val isValid: Boolean = passwordError == null && confirmationError == null
    val isInvalid: Boolean = !isValid
}

data class ProfileUpdateValidationResult(
    val usernameError: String?,
    val firstNameError: String?,
    val lastNameError: String?,
    val emailError: String?,
    val phoneError: String?,
) {
    val isValid: Boolean = listOf(
        usernameError,
        firstNameError,
        lastNameError,
        emailError,
        phoneError,
    ).all { error -> error == null }
    val isInvalid: Boolean = !isValid
}
