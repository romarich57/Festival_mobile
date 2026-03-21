package com.projetmobile.mobile.ui.utils.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.projetmobile.mobile.data.entity.auth.VerificationResultStatus
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
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

@Serializable
data class ReservationDashboard(val festivalId: Int) : AppNavKey

@Serializable
data class ReservationForm(val festivalId: Int) : AppNavKey


enum class TopLevelTab {
    Festivals,
    Reservants,
    Games,
    Login,
    Register,
    Profile,
    Admin,
}

data class TopLevelDestinationSpec(
    val tab: TopLevelTab,
    val label: String,
    val icon: ImageVector,
    val rootKey: AppNavKey,
    val topBarTitle: String,
)

data class AppChromeState(
    val title: String,
    val showBack: Boolean,
    val showBottomBar: Boolean,
    val selectedTab: TopLevelTab,
)

val topLevelDestinationSpecs = listOf(
    TopLevelDestinationSpec(
        tab = TopLevelTab.Festivals,
        label = "Festivals",
        icon = Icons.Outlined.Event,
        rootKey = Festivals,
        topBarTitle = "Festivals",
    ),
    TopLevelDestinationSpec(
        tab = TopLevelTab.Reservants,
        label = "Réservant",
        icon = Icons.Outlined.Groups,
        rootKey = Reservants,
        topBarTitle = "Réservants",
    ),
    TopLevelDestinationSpec(
        tab = TopLevelTab.Games,
        label = "Jeux",
        icon = Icons.Outlined.SportsEsports,
        rootKey = Games,
        topBarTitle = "Jeux",
    ),
    TopLevelDestinationSpec(
        tab = TopLevelTab.Login,
        label = "Connexion",
        icon = Icons.Outlined.LockOpen,
        rootKey = Login,
        topBarTitle = "Connexion",
    ),
    TopLevelDestinationSpec(
        tab = TopLevelTab.Register,
        label = "Inscription",
        icon = Icons.Outlined.PersonAddAlt1,
        rootKey = Register,
        topBarTitle = "Inscription",
    ),
    TopLevelDestinationSpec(
        tab = TopLevelTab.Profile,
        label = "Profil",
        icon = Icons.Outlined.AccountCircle,
        rootKey = Profile,
        topBarTitle = "Profil",
    ),
    TopLevelDestinationSpec(
        tab = TopLevelTab.Admin,
        label = "Admin",
        icon = Icons.Outlined.AdminPanelSettings,
        rootKey = Admin,
        topBarTitle = "Admin",
    ),
)

private val specByTab = topLevelDestinationSpecs.associateBy(TopLevelDestinationSpec::tab)

fun specFor(tab: TopLevelTab): TopLevelDestinationSpec = checkNotNull(specByTab[tab]) {
    "No top-level destination spec found for $tab"
}

fun visibleTabs(isAuthenticated: Boolean, userRole: String?): List<TopLevelTab> {
    return when {
        !isAuthenticated -> listOf(TopLevelTab.Festivals, TopLevelTab.Login, TopLevelTab.Register)
        isAdminRole(userRole) -> listOf(
            TopLevelTab.Festivals,
            TopLevelTab.Reservants,
            TopLevelTab.Games,
            TopLevelTab.Profile,
            TopLevelTab.Admin,
        )

        else -> listOf(
            TopLevelTab.Festivals,
            TopLevelTab.Reservants,
            TopLevelTab.Games,
            TopLevelTab.Profile,
        )
    }
}

fun ownerTab(key: AppNavKey): TopLevelTab {
    return when (key) {
        Festivals, is ReservationDashboard, is ReservationForm -> TopLevelTab.Festivals
        Reservants -> TopLevelTab.Reservants
        Games -> TopLevelTab.Games
        Login, ForgotPassword, is VerificationResult, is ResetPassword -> TopLevelTab.Login
        Register, is PendingVerification -> TopLevelTab.Register
        Profile -> TopLevelTab.Profile
        Admin -> TopLevelTab.Admin
    }
}

fun chromeFor(
    activeKey: AppNavKey,
    activeBackStack: List<AppNavKey>,
    isAuthenticated: Boolean,
    userRole: String?,
): AppChromeState {
    val tabsToShow = visibleTabs(
        isAuthenticated = isAuthenticated,
        userRole = userRole,
    )

    return AppChromeState(
        title = topBarTitleFor(activeKey),
        showBack = activeBackStack.size > 1,
        showBottomBar = true,
        selectedTab = ownerTab(activeKey).let { owner ->
            if (owner in tabsToShow) owner else tabsToShow.first()
        },
    )
}

fun topBarTitleFor(key: AppNavKey): String {
    return when (key) {
        Festivals -> "Festivals"
        is ReservationDashboard -> "Réservations"
        is ReservationForm -> "Nouvelle Réservation"
        Reservants -> "Réservants"
        Games -> "Jeux"
        Login -> "Connexion"
        Register -> "Inscription"
        Profile -> "Profil"
        Admin -> "Admin"
        ForgotPassword -> "Mot de passe oublié"
        is PendingVerification -> "Vérifiez votre email"
        is VerificationResult -> verificationTitleFor(key.status)
        is ResetPassword -> "Réinitialiser votre mot de passe"
    }
}

fun parseAppDeepLink(uri: Uri?): AppNavKey? {
    return parseAppDeepLinkString(uri?.toString())
}

fun parseAppDeepLinkString(rawUri: String?): AppNavKey? {
    val parsedUri = rawUri
        ?.takeIf { it.isNotBlank() }
        ?.let {
            runCatching { URI.create(it) }.getOrNull()
        }
        ?: return null

    if (parsedUri.scheme != "festivalapp" || parsedUri.host != "auth") {
        return null
    }

    val queryParameters = parsedUri.queryParameters()

    return when (parsedUri.path) {
        "/verification" -> VerificationResult(
            status = when (queryParameters["status"]) {
                "success" -> VerificationResultStatus.Success
                "expired" -> VerificationResultStatus.Expired
                "invalid" -> VerificationResultStatus.Invalid
                else -> VerificationResultStatus.Error
            },
        )

        "/reset-password" -> ResetPassword(queryParameters["token"]?.takeIf { it.isNotBlank() })
        else -> null
    }
}

private fun verificationTitleFor(status: VerificationResultStatus): String {
    return when (status) {
        VerificationResultStatus.Success -> "Email confirmé"
        VerificationResultStatus.Expired -> "Lien expiré"
        VerificationResultStatus.Invalid -> "Lien invalide"
        VerificationResultStatus.Error -> "Erreur de vérification"
    }
}

private fun isAdminRole(userRole: String?): Boolean {
    return normalizeUserRole(userRole) == "admin"
}

private fun normalizeUserRole(userRole: String?): String? {
    return userRole
        ?.trim()
        ?.lowercase()
        ?.takeIf { it.isNotBlank() }
}

private fun URI.queryParameters(): Map<String, String?> {
    val rawQuery = query ?: return emptyMap()
    if (rawQuery.isBlank()) {
        return emptyMap()
    }

    return rawQuery
        .split("&")
        .filter { it.isNotBlank() }
        .associate { segment ->
            val parts = segment.split("=", limit = 2)
            val key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8)
            val value = parts
                .getOrNull(1)
                ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8) }
            key to value
        }
}
