package com.projetmobile.mobile.data.repository.auth

import android.content.ContextWrapper
import com.projetmobile.mobile.data.database.AuthPreferenceStore
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.entity.auth.RegisterAccountInput
import com.projetmobile.mobile.data.remote.auth.AuthApiService
import com.projetmobile.mobile.data.remote.auth.CurrentUserResponseDto
import com.projetmobile.mobile.data.remote.auth.ForgotPasswordRequestDto
import com.projetmobile.mobile.data.remote.auth.LoginRequestDto
import com.projetmobile.mobile.data.remote.auth.LoginResponseDto
import com.projetmobile.mobile.data.remote.auth.MessageResponseDto
import com.projetmobile.mobile.data.remote.auth.RegisterRequestDto
import com.projetmobile.mobile.data.remote.auth.ResetPasswordRequestDto
import com.projetmobile.mobile.data.remote.auth.ResendVerificationRequestDto
import com.projetmobile.mobile.data.repository.NetworkStatusProvider
import com.projetmobile.mobile.data.repository.RepositoryNetworkStatus
import java.net.ConnectException
import java.net.UnknownHostException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {

    @After
    fun tearDown() {
        RepositoryNetworkStatus.resetForTests()
    }

    @Test
    fun login_persistsAuthenticatedUserInCache() = runTest {
        val preferences = FakeAuthPreferenceStore()
        val repository = AuthRepositoryImpl(
            authApiService = FakeAuthApiService(),
            authPreferenceStore = preferences,
        )

        val user = repository.login("admin", "password").getOrThrow()

        assertEquals("admin", user.login)
        assertEquals(user, preferences.cachedUser)
    }

    @Test
    fun restoreSession_returnsCachedUserWhenNetworkIsUnavailable() = runTest {
        RepositoryNetworkStatus.initialize(NetworkStatusProvider { false })
        val cachedUserSnapshot = sampleAuthUser()
        val preferences = FakeAuthPreferenceStore().apply {
            cachedUser = cachedUserSnapshot
        }
        val repository = AuthRepositoryImpl(
            authApiService = FakeAuthApiService(
                currentUserFailure = UnknownHostException("mobile.romdev.cloud"),
            ),
            authPreferenceStore = preferences,
        )

        val restoredUser = repository.restoreSession().getOrThrow()

        assertEquals(cachedUserSnapshot, restoredUser)
    }

    @Test
    fun login_returnsBackendUnavailableMessageWhenNetworkIsValidated() = runTest {
        RepositoryNetworkStatus.initialize(NetworkStatusProvider { true })
        val repository = AuthRepositoryImpl(
            authApiService = FakeAuthApiService(
                loginFailure = ConnectException("Connection refused"),
            ),
            authPreferenceStore = FakeAuthPreferenceStore(),
        )

        val failure = repository.login("admin", "password").exceptionOrNull()

        assertEquals(
            "Serveur inaccessible pour le moment. Réessayez plus tard.",
            failure?.message,
        )
    }

    @Test
    fun restoreSession_returnsCachedUserWhenBackendIsUnavailableButNetworkIsValidated() = runTest {
        RepositoryNetworkStatus.initialize(NetworkStatusProvider { true })
        val cachedUserSnapshot = sampleAuthUser()
        val preferences = FakeAuthPreferenceStore().apply {
            cachedUser = cachedUserSnapshot
        }
        val repository = AuthRepositoryImpl(
            authApiService = FakeAuthApiService(
                currentUserFailure = ConnectException("Connection refused"),
            ),
            authPreferenceStore = preferences,
        )

        val restoredUser = repository.restoreSession().getOrThrow()

        assertEquals(cachedUserSnapshot, restoredUser)
    }

    @Test
    fun restoreSession_clearsCachedUserWhenServerReturnsUnauthorized() = runTest {
        val preferences = FakeAuthPreferenceStore().apply {
            cachedUser = sampleAuthUser()
        }
        val repository = AuthRepositoryImpl(
            authApiService = FakeAuthApiService(
                currentUserFailure = HttpException(Response.error<Any>(401, "".toResponseBody())),
            ),
            authPreferenceStore = preferences,
        )

        val restoredUser = repository.restoreSession().getOrThrow()

        assertNull(restoredUser)
        assertNull(preferences.cachedUser)
    }
}

private class FakeAuthPreferenceStore : AuthPreferenceStore(ContextWrapper(null)) {
    var pendingVerificationEmail: String? = null
    var lastLoginIdentifier: String? = null
    var cachedUser: AuthUser? = null

    override suspend fun getPendingVerificationEmail(): String? = pendingVerificationEmail

    override suspend fun setPendingVerificationEmail(email: String) {
        pendingVerificationEmail = email
    }

    override suspend fun clearPendingVerificationEmail() {
        pendingVerificationEmail = null
    }

    override suspend fun getLastLoginIdentifier(): String? = lastLoginIdentifier

    override suspend fun setLastLoginIdentifier(identifier: String) {
        lastLoginIdentifier = identifier
    }

    override suspend fun getCachedUser(): AuthUser? = cachedUser

    override suspend fun setCachedUser(user: AuthUser) {
        cachedUser = user
    }

    override suspend fun clearCachedUser() {
        cachedUser = null
    }
}

private class FakeAuthApiService(
    private val loginFailure: Throwable? = null,
    private val currentUserFailure: Throwable? = null,
) : AuthApiService {
    override suspend fun login(request: LoginRequestDto): LoginResponseDto {
        loginFailure?.let { throw it }
        return LoginResponseDto(
            message = "ok",
            user = sampleAuthUserDto(),
        )
    }

    override suspend fun register(request: RegisterRequestDto): MessageResponseDto {
        return MessageResponseDto(message = "registered")
    }

    override suspend fun resendVerification(request: ResendVerificationRequestDto): MessageResponseDto {
        return MessageResponseDto(message = "resent")
    }

    override suspend fun requestPasswordReset(request: ForgotPasswordRequestDto): MessageResponseDto {
        return MessageResponseDto(message = "reset")
    }

    override suspend fun resetPassword(request: ResetPasswordRequestDto): MessageResponseDto {
        return MessageResponseDto(message = "updated")
    }

    override suspend fun logout(): MessageResponseDto {
        return MessageResponseDto(message = "logout")
    }

    override suspend fun getCurrentUser(): CurrentUserResponseDto {
        currentUserFailure?.let { throw it }
        return CurrentUserResponseDto(user = sampleAuthUserDto())
    }
}

private fun sampleAuthUser() = AuthUser(
    id = 7,
    login = "admin",
    role = "admin",
    firstName = "Admin",
    lastName = "System",
    email = "admin@secureapp.com",
    phone = "0601020304",
    avatarUrl = null,
    emailVerified = true,
    createdAt = "2026-04-08T10:00:00.000Z",
)

private fun sampleAuthUserDto() = com.projetmobile.mobile.data.remote.auth.AuthUserDto(
    id = 7,
    login = "admin",
    role = "admin",
    firstName = "Admin",
    lastName = "System",
    email = "admin@secureapp.com",
    phone = "0601020304",
    avatarUrl = null,
    emailVerified = true,
    createdAt = "2026-04-08T10:00:00.000Z",
)
