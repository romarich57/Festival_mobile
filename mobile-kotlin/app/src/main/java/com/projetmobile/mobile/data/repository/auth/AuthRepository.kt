package com.projetmobile.mobile.data.repository.auth

import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.entity.auth.RegisterAccountInput

interface AuthRepository {
    suspend fun login(identifier: String, password: String): Result<AuthUser>

    suspend fun register(input: RegisterAccountInput): Result<String>

    suspend fun resendVerification(email: String): Result<String>

    suspend fun requestPasswordReset(email: String): Result<String>

    suspend fun resetPassword(token: String, password: String): Result<String>

    suspend fun logout(): Result<String>

    suspend fun restoreSession(): Result<AuthUser?>

    suspend fun getCurrentUser(): Result<AuthUser>

    suspend fun getPendingVerificationEmail(): String?

    suspend fun setPendingVerificationEmail(email: String)

    suspend fun clearPendingVerificationEmail()

    suspend fun getLastLoginIdentifier(): String?

    suspend fun setLastLoginIdentifier(identifier: String)
}
