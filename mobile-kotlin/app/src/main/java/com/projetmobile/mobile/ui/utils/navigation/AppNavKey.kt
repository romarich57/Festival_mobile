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
data object ReservantCreate : AppNavKey

@Serializable
data class ReservantDetails(val reservantId: Int) : AppNavKey

@Serializable
data class ReservantEdit(val reservantId: Int) : AppNavKey

@Serializable
data class ReservantGameCreate(
    val reservantId: Int,
    val editorId: Int,
) : AppNavKey

@Serializable
data object Games : AppNavKey

@Serializable
data object GameCreate : AppNavKey

@Serializable
data class GameDetails(val gameId: Int) : AppNavKey

@Serializable
data class GameEdit(val gameId: Int) : AppNavKey

@Serializable
data object Login : AppNavKey

@Serializable
data object Register : AppNavKey

@Serializable
data object Profile : AppNavKey

@Serializable
data object Admin : AppNavKey

@Serializable
data class AdminUserDetail(val userId: Int) : AppNavKey

@Serializable
data object AdminUserCreate : AppNavKey

@Serializable
data class AdminUserEdit(val userId: Int) : AppNavKey

@Serializable
data object ForgotPassword : AppNavKey

@Serializable
data class PendingVerification(val email: String?) : AppNavKey

@Serializable
data class VerificationResult(val status: VerificationResultStatus) : AppNavKey

@Serializable
data class ResetPassword(val token: String?) : AppNavKey

@Serializable
data class ReservationDashboard(val festivalId: Int) : AppNavKey

@Serializable
data class ReservationForm(val festivalId: Int) : AppNavKey

@Serializable
data object FestivalForm : AppNavKey

@Serializable
data class ReservationDetails(val reservationId: Int): AppNavKey

enum class TopLevelTab {
    Festivals,
    Reservants,
    Games,
    Login,
    Register,
    Profile,
    Admin,
}
