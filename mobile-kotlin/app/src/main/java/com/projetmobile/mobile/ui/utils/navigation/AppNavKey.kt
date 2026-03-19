package com.projetmobile.mobile.ui.utils.navigation

import androidx.navigation3.runtime.NavKey
import com.projetmobile.mobile.data.entity.auth.VerificationResultStatus
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppNavKey : NavKey

@Serializable
data object Festivals : AppNavKey

@Serializable
data object Reservants : AppNavKey

@Serializable
data object Games : AppNavKey

@Serializable
data object Login : AppNavKey

@Serializable
data object Register : AppNavKey

@Serializable
data object Profile : AppNavKey

@Serializable
data object Admin : AppNavKey

@Serializable
data object ForgotPassword : AppNavKey

@Serializable
data class PendingVerification(val email: String?) : AppNavKey

@Serializable
data class VerificationResult(val status: VerificationResultStatus) : AppNavKey

@Serializable
data class ResetPassword(val token: String?) : AppNavKey

enum class TopLevelTab {
    Festivals,
    Reservants,
    Games,
    Login,
    Register,
    Profile,
    Admin,
}
