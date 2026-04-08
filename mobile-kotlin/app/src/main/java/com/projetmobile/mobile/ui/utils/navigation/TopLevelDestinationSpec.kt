/**
 * Rôle : Décrit explicitement les destinations de premier niveau (celles présentes dans la barre de navigation basse).
 * Lie chaque onglet à son icône, son titre et sa route canonique (AppNavKey).
 * Précondition : Les rôles de l'utilisateur doivent être connus pour évaluer les onglets de menu affichables.
 * Postcondition : Fournit la liste des onglets de bas de page filtrée selon l'état d'authentification de l'utilisateur.
 */
package com.projetmobile.mobile.ui.utils.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector

data class TopLevelDestinationSpec(
    val tab: TopLevelTab,
    val label: String,
    val icon: ImageVector,
    val rootKey: AppNavKey,
    val topBarTitle: String,
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

private fun isAdminRole(userRole: String?): Boolean {
    return normalizeUserRole(userRole) == "admin"
}

private fun normalizeUserRole(userRole: String?): String? {
    return userRole
        ?.trim()
        ?.lowercase()
        ?.takeIf { it.isNotBlank() }
}
