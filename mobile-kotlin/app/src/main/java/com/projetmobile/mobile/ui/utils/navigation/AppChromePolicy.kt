package com.projetmobile.mobile.ui.utils.navigation

import com.projetmobile.mobile.data.entity.auth.VerificationResultStatus

data class AppChromeState(
    val title: String,
    val showBack: Boolean,
    val showBottomBar: Boolean,
    val selectedTab: TopLevelTab,
)

fun ownerTab(key: AppNavKey): TopLevelTab {
    return when (key) {
        Festivals -> TopLevelTab.Festivals
        Reservants,
        ReservantCreate,
        is ReservantDetails,
        is ReservantEdit,
        is ReservantGameCreate -> TopLevelTab.Reservants
        Games, GameCreate, is GameDetails, is GameEdit -> TopLevelTab.Games
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
        Reservants -> "Réservants"
        ReservantCreate -> "Nouveau réservant"
        is ReservantDetails -> "Détail du réservant"
        is ReservantEdit -> "Modifier un réservant"
        is ReservantGameCreate -> "Nouveau jeu"
        Games -> "Jeux"
        GameCreate -> "Nouveau jeu"
        is GameDetails -> "Détails du jeu"
        is GameEdit -> "Modifier un jeu"
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

private fun verificationTitleFor(status: VerificationResultStatus): String {
    return when (status) {
        VerificationResultStatus.Success -> "Email confirmé"
        VerificationResultStatus.Expired -> "Lien expiré"
        VerificationResultStatus.Invalid -> "Lien invalide"
        VerificationResultStatus.Error -> "Erreur de vérification"
    }
}
