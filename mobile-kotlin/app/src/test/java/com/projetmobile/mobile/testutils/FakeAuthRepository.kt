package com.projetmobile.mobile.testutils

import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.entity.auth.RegisterAccountInput
import com.projetmobile.mobile.data.repository.auth.AuthRepository

class FakeAuthRepository : AuthRepository {
    var requestPasswordResetCalls: Int = 0
    var lastRequestedResetEmail: String? = null
    var requestPasswordResetResult: Result<String> = Result.success(
        "Si un compte existe pour cet email, un lien de réinitialisation vient d’être envoyé.",
    )

    var resetPasswordCalls: Int = 0
    var lastResetToken: String? = null
    var lastResetPassword: String? = null
    var resetPasswordResult: Result<String> = Result.success(
        "Mot de passe mis à jour. Vous pouvez vous connecter.",
    )

    var logoutCalls: Int = 0
    var logoutResult: Result<String> = Result.success("Déconnexion réussie")

    override suspend fun login(identifier: String, password: String): Result<AuthUser> {
        error("Not used in this test")
    }

    override suspend fun register(input: RegisterAccountInput): Result<String> {
        error("Not used in this test")
    }

    override suspend fun resendVerification(email: String): Result<String> {
        error("Not used in this test")
    }

    override suspend fun requestPasswordReset(email: String): Result<String> {
        requestPasswordResetCalls += 1
        lastRequestedResetEmail = email
        return requestPasswordResetResult
    }

    override suspend fun resetPassword(token: String, password: String): Result<String> {
        resetPasswordCalls += 1
        lastResetToken = token
        lastResetPassword = password
        return resetPasswordResult
    }

    override suspend fun logout(): Result<String> {
        logoutCalls += 1
        return logoutResult
    }

    override suspend fun restoreSession(): Result<AuthUser?> {
        error("Not used in this test")
    }

    override suspend fun getCurrentUser(): Result<AuthUser> {
        error("Not used in this test")
    }

    override suspend fun getPendingVerificationEmail(): String? = null

    override suspend fun setPendingVerificationEmail(email: String) = Unit

    override suspend fun clearPendingVerificationEmail() = Unit

    override suspend fun getLastLoginIdentifier(): String? = null

    override suspend fun setLastLoginIdentifier(identifier: String) = Unit
}
