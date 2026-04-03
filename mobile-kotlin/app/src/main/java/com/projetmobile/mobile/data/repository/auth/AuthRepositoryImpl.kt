package com.projetmobile.mobile.data.repository.auth

import com.projetmobile.mobile.data.mapper.auth.toAuthUser
import com.projetmobile.mobile.data.remote.auth.AuthApiService
import com.projetmobile.mobile.data.remote.auth.ForgotPasswordRequestDto
import com.projetmobile.mobile.data.remote.auth.LoginRequestDto
import com.projetmobile.mobile.data.remote.auth.RegisterRequestDto
import com.projetmobile.mobile.data.remote.auth.ResetPasswordRequestDto
import com.projetmobile.mobile.data.remote.auth.ResendVerificationRequestDto
import com.projetmobile.mobile.data.database.AuthPreferenceStore
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.entity.auth.RegisterAccountInput
import com.projetmobile.mobile.data.repository.runRepositoryCall
import com.projetmobile.mobile.data.repository.toRepositoryException
import retrofit2.HttpException

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val authPreferenceStore: AuthPreferenceStore,
) : AuthRepository {

    override suspend fun login(identifier: String, password: String): Result<AuthUser> {
        return runRepositoryCall(defaultMessage = "Connexion impossible.") {
            authPreferenceStore.setLastLoginIdentifier(identifier)
            val response = authApiService.login(
                LoginRequestDto(
                    identifier = identifier.trim(),
                    password = password,
                ),
            )
            authPreferenceStore.clearPendingVerificationEmail()
            response.user.toAuthUser()
        }
    }

    override suspend fun register(input: RegisterAccountInput): Result<String> {
        return runRepositoryCall(defaultMessage = "Inscription impossible.") {
            val response = authApiService.register(
                RegisterRequestDto(
                    login = input.username.trim(),
                    firstName = input.firstName.trim(),
                    lastName = input.lastName.trim(),
                    email = input.email.trim(),
                    password = input.password,
                    phone = input.phone?.trim()?.takeIf { it.isNotEmpty() },
                ),
            )
            authPreferenceStore.setPendingVerificationEmail(input.email.trim())
            authPreferenceStore.setLastLoginIdentifier(input.email.trim())
            response.message
        }
    }

    override suspend fun resendVerification(email: String): Result<String> {
        return runRepositoryCall(defaultMessage = "Renvoi impossible.") {
            val response = authApiService.resendVerification(
                ResendVerificationRequestDto(email.trim()),
            )
            authPreferenceStore.setPendingVerificationEmail(email.trim())
            response.message
        }
    }

    override suspend fun requestPasswordReset(email: String): Result<String> {
        return runRepositoryCall(defaultMessage = "Demande de réinitialisation impossible.") {
            val normalizedEmail = email.trim()
            val response = authApiService.requestPasswordReset(
                ForgotPasswordRequestDto(normalizedEmail),
            )
            authPreferenceStore.setLastLoginIdentifier(normalizedEmail)
            response.message
        }
    }

    override suspend fun resetPassword(token: String, password: String): Result<String> {
        return runRepositoryCall(defaultMessage = "Réinitialisation impossible.") {
            val response = authApiService.resetPassword(
                ResetPasswordRequestDto(
                    token = token.trim(),
                    password = password,
                ),
            )
            response.message
        }
    }

    override suspend fun logout(): Result<String> {
        return runRepositoryCall(defaultMessage = "Déconnexion impossible.") {
            val response = authApiService.logout()
            authPreferenceStore.clearPendingVerificationEmail()
            response.message
        }
    }

    override suspend fun restoreSession(): Result<AuthUser?> {
        return try {
            val response = authApiService.getCurrentUser()
            Result.success(response.user.toAuthUser())
        } catch (exception: HttpException) {
            if (exception.code() == 401 || exception.code() == 403) {
                Result.success(null)
            } else {
                Result.failure(exception)
            }
        } catch (exception: Throwable) {
            Result.failure(exception)
        }
    }

    override suspend fun getCurrentUser(): Result<AuthUser> {
        return runRepositoryCall(defaultMessage = "Impossible de récupérer la session.") {
            authApiService.getCurrentUser().user.toAuthUser()
        }
    }

    override suspend fun getPendingVerificationEmail(): String? {
        return authPreferenceStore.getPendingVerificationEmail()
    }

    override suspend fun setPendingVerificationEmail(email: String) {
        authPreferenceStore.setPendingVerificationEmail(email)
    }

    override suspend fun clearPendingVerificationEmail() {
        authPreferenceStore.clearPendingVerificationEmail()
    }

    override suspend fun getLastLoginIdentifier(): String? {
        return authPreferenceStore.getLastLoginIdentifier()
    }

    override suspend fun setLastLoginIdentifier(identifier: String) {
        authPreferenceStore.setLastLoginIdentifier(identifier)
    }
}
