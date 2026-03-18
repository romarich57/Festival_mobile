package com.projetmobile.mobile.ui.utils.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.PersonAddAlt1
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
data object Login : AppNavKey

@Serializable
data object Register : AppNavKey

@Serializable
data object Profile : AppNavKey

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
    Login,
    Register,
    Profile,
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
)

private val specByTab = topLevelDestinationSpecs.associateBy(TopLevelDestinationSpec::tab)

fun specFor(tab: TopLevelTab): TopLevelDestinationSpec = checkNotNull(specByTab[tab]) {
    "No top-level destination spec found for $tab"
}

fun visibleTabs(isAuthenticated: Boolean): List<TopLevelTab> {
    return if (isAuthenticated) {
        listOf(TopLevelTab.Festivals, TopLevelTab.Profile)
    } else {
        listOf(TopLevelTab.Festivals, TopLevelTab.Login, TopLevelTab.Register)
    }
}

fun ownerTab(key: AppNavKey): TopLevelTab {
    return when (key) {
        Festivals -> TopLevelTab.Festivals
        Login, ForgotPassword, is VerificationResult, is ResetPassword -> TopLevelTab.Login
        Register, is PendingVerification -> TopLevelTab.Register
        Profile -> TopLevelTab.Profile
    }
}

fun chromeFor(
    activeKey: AppNavKey,
    activeBackStack: List<AppNavKey>,
    isAuthenticated: Boolean,
): AppChromeState {
    return AppChromeState(
        title = topBarTitleFor(activeKey),
        showBack = activeBackStack.size > 1,
        showBottomBar = true,
        selectedTab = ownerTab(activeKey).let { owner ->
            if (owner in visibleTabs(isAuthenticated)) owner else visibleTabs(isAuthenticated).first()
        },
    )
}

fun topBarTitleFor(key: AppNavKey): String {
    return when (key) {
        Festivals -> "Festivals"
        Login -> "Connexion"
        Register -> "Inscription"
        Profile -> "Profil"
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
