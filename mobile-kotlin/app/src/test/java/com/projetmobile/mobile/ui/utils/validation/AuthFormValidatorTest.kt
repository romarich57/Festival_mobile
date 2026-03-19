package com.projetmobile.mobile.ui.utils.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthFormValidatorTest {

    @Test
    fun validateLogin_returnsErrorsWhenIdentifierAndPasswordAreMissing() {
        val result = AuthFormValidator.validateLogin(identifier = "", password = "")

        assertTrue(result.identifierError != null)
        assertTrue(result.passwordError != null)
        assertTrue(result.isInvalid)
    }

    @Test
    fun validateLogin_acceptsFilledCredentials() {
        val result = AuthFormValidator.validateLogin(
            identifier = "festival@example.com",
            password = "Secret123!",
        )

        assertEquals(null, result.identifierError)
        assertEquals(null, result.passwordError)
        assertTrue(result.isValid)
    }

    @Test
    fun validateRegister_requiresMandatoryFieldsAndEmailFormat() {
        val result = AuthFormValidator.validateRegister(
            username = "",
            firstName = "",
            lastName = "",
            email = "invalid-email",
            password = "123",
            phone = "",
        )

        assertTrue(result.usernameError != null)
        assertTrue(result.firstNameError != null)
        assertTrue(result.lastNameError != null)
        assertTrue(result.emailError != null)
        assertTrue(result.passwordError != null)
        assertTrue(result.isInvalid)
    }

    @Test
    fun validateResendVerification_requiresAnEmailAddress() {
        val result = AuthFormValidator.validateResendVerification("festivalUser")

        assertTrue(result.emailError != null)
        assertTrue(result.isInvalid)
    }

    @Test
    fun validateForgotPassword_requiresValidEmail() {
        val result = AuthFormValidator.validateForgotPassword("not-an-email")

        assertTrue(result.emailError != null)
        assertTrue(result.isInvalid)
    }

    @Test
    fun validateResetPassword_requiresMinimumLengthAndMatchingConfirmation() {
        val result = AuthFormValidator.validateResetPassword(
            password = "1234567",
            confirmation = "12345678",
        )

        assertTrue(result.passwordError != null)
        assertTrue(result.confirmationError != null)
        assertTrue(result.isInvalid)
    }

    @Test
    fun validateResetPassword_acceptsValidPasswordAndConfirmation() {
        val result = AuthFormValidator.validateResetPassword(
            password = "NewPassword123!",
            confirmation = "NewPassword123!",
        )

        assertEquals(null, result.passwordError)
        assertEquals(null, result.confirmationError)
        assertTrue(result.isValid)
    }

    @Test
    fun validateProfileUpdate_reusesRegisterRulesWithoutPassword() {
        val invalid = AuthFormValidator.validateProfileUpdate(
            username = "",
            firstName = "",
            lastName = "",
            email = "invalid-email",
            phone = "123",
        )
        val valid = AuthFormValidator.validateProfileUpdate(
            username = "romain",
            firstName = "Romain",
            lastName = "Richard",
            email = "romain@example.com",
            phone = "0600000000",
        )

        assertTrue(invalid.isInvalid)
        assertTrue(invalid.usernameError != null)
        assertTrue(invalid.emailError != null)
        assertTrue(invalid.phoneError != null)
        assertTrue(valid.isValid)
        assertEquals(null, valid.emailError)
    }
}
