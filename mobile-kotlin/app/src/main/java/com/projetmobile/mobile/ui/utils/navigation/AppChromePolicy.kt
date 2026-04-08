/**
 * Rôle : Gère la politique d'affichage du "Chrome" de l'application (Barres de navigation, Titres, Bouton Retour).
 * Ce fichier déduit l'état de l'UI globale en fonction de la destination courante et de l'état d'authentification.
 * Précondition : Appelé à chaque changement de navigation dans le composant racine `FestivalApp`.
 * Postcondition : Fournit un objet `AppChromeState` décrivant exactement comment configurer le composant Scaffold.
 */
package com.projetmobile.mobile.ui.utils.navigation

import com.projetmobile.mobile.data.entity.auth.VerificationResultStatus

/**
 * Rôle : Décrit l'état immuable du module navigation.
 */
data class AppChromeState(
    val title: String,
    val showBack: Boolean,
    val showBottomBar: Boolean,
    val selectedTab: TopLevelTab,
)

/**
 * Rôle : Exécute l'action owner onglet du module navigation.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
fun ownerTab(key: AppNavKey): TopLevelTab {
    return when (key) {
        Festivals,
        is ReservationDashboard,
        is ReservationForm,
        is ReservationDetails,
        FestivalForm -> TopLevelTab.Festivals
        Reservants,
        ReservantCreate,
        is ReservantDetails,
        is ReservantEdit,
        is ReservantGameCreate -> TopLevelTab.Reservants
        Games, GameCreate, is GameDetails, is GameEdit -> TopLevelTab.Games
        Login, ForgotPassword, is VerificationResult, is ResetPassword -> TopLevelTab.Login
        Register, is PendingVerification -> TopLevelTab.Register
        Profile -> TopLevelTab.Profile
        Admin,
        is AdminUserDetail,
        AdminUserCreate,
        is AdminUserEdit -> TopLevelTab.Admin
    }
}

/**
 * Rôle : Exécute l'action chrome for du module navigation.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
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
        showBottomBar = activeKey !is FestivalForm,
        selectedTab = ownerTab(activeKey).let { owner ->
            if (owner in tabsToShow) owner else tabsToShow.first()
        },
    )
}

/**
 * Rôle : Exécute l'action top bar title for du module navigation.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
fun topBarTitleFor(key: AppNavKey): String {
    return when (key) {
        Festivals -> "Festivals"
        is ReservationDashboard -> "Réservations"
        is ReservationForm -> "Nouvelle Réservation"
        is ReservationDetails -> "Détails de la Réservation"
        FestivalForm -> "Nouveau festival"
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
        is AdminUserDetail -> "Fiche utilisateur"
        AdminUserCreate -> "Créer un utilisateur"
        is AdminUserEdit -> "Modifier un utilisateur"
        ForgotPassword -> "Mot de passe oublié"
        is PendingVerification -> "Vérifiez votre email"
        is VerificationResult -> verificationTitleFor(key.status)
        is ResetPassword -> "Réinitialiser votre mot de passe"
    }
}

/**
 * Rôle : Exécute l'action verification title for du module navigation.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
private fun verificationTitleFor(status: VerificationResultStatus): String {
    return when (status) {
        VerificationResultStatus.Success -> "Email confirmé"
        VerificationResultStatus.Expired -> "Lien expiré"
        VerificationResultStatus.Invalid -> "Lien invalide"
        VerificationResultStatus.Error -> "Erreur de vérification"
    }
}
