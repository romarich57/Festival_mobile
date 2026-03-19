package com.projetmobile.mobile.ui.utils.navigation

import com.projetmobile.mobile.data.entity.auth.VerificationResultStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppChromeResolverTest {

    @Test
    fun chromeFor_publicTopLevelLogin_hasNoBackAndSelectsLogin() {
        val chrome = chromeFor(
            activeKey = Login,
            activeBackStack = listOf(Login),
            isAuthenticated = false,
            userRole = null,
        )

        assertEquals("Connexion", chrome.title)
        assertFalse(chrome.showBack)
        assertTrue(chrome.showBottomBar)
        assertEquals(TopLevelTab.Login, chrome.selectedTab)
    }

    @Test
    fun specFor_reservants_usesSingularNavbarLabel() {
        assertEquals("Réservant", specFor(TopLevelTab.Reservants).label)
    }

    @Test
    fun visibleTabs_publicSession_showsPublicTabsOnly() {
        assertEquals(
            listOf(TopLevelTab.Festivals, TopLevelTab.Login, TopLevelTab.Register),
            visibleTabs(isAuthenticated = false, userRole = null),
        )
    }

    @Test
    fun visibleTabs_adminSession_showsAdminTabs() {
        assertEquals(
            listOf(
                TopLevelTab.Festivals,
                TopLevelTab.Reservants,
                TopLevelTab.Games,
                TopLevelTab.Profile,
                TopLevelTab.Admin,
            ),
            visibleTabs(isAuthenticated = true, userRole = "admin"),
        )
    }

    @Test
    fun visibleTabs_nonAdminRoles_shareSameBusinessTabs() {
        val expectedTabs = listOf(
            TopLevelTab.Festivals,
            TopLevelTab.Reservants,
            TopLevelTab.Games,
            TopLevelTab.Profile,
        )

        listOf("organizer", "super-organizer", "benevole", null).forEach { role ->
            assertEquals(
                expectedTabs,
                visibleTabs(isAuthenticated = true, userRole = role),
            )
        }
    }

    @Test
    fun chromeFor_pendingVerification_selectsRegisterAndShowsBack() {
        val chrome = chromeFor(
            activeKey = PendingVerification("user@example.com"),
            activeBackStack = listOf(Register, PendingVerification("user@example.com")),
            isAuthenticated = false,
            userRole = null,
        )

        assertEquals("Vérifiez votre email", chrome.title)
        assertTrue(chrome.showBack)
        assertTrue(chrome.showBottomBar)
        assertEquals(TopLevelTab.Register, chrome.selectedTab)
    }

    @Test
    fun chromeFor_verificationError_usesDynamicTitleAndLoginTab() {
        val chrome = chromeFor(
            activeKey = VerificationResult(VerificationResultStatus.Error),
            activeBackStack = listOf(Login, VerificationResult(VerificationResultStatus.Error)),
            isAuthenticated = false,
            userRole = null,
        )

        assertEquals("Erreur de vérification", chrome.title)
        assertTrue(chrome.showBack)
        assertEquals(TopLevelTab.Login, chrome.selectedTab)
    }

    @Test
    fun chromeFor_authenticatedProfile_hasNoBackAndSelectsProfile() {
        val chrome = chromeFor(
            activeKey = Profile,
            activeBackStack = listOf(Profile),
            isAuthenticated = true,
            userRole = "organizer",
        )

        assertEquals("Profil", chrome.title)
        assertFalse(chrome.showBack)
        assertTrue(chrome.showBottomBar)
        assertEquals(TopLevelTab.Profile, chrome.selectedTab)
    }

    @Test
    fun chromeFor_invisibleAdminTabForNonAdmin_fallsBackToFestivalsSelection() {
        val chrome = chromeFor(
            activeKey = Admin,
            activeBackStack = listOf(Admin),
            isAuthenticated = true,
            userRole = "organizer",
        )

        assertEquals("Admin", chrome.title)
        assertFalse(chrome.showBack)
        assertTrue(chrome.showBottomBar)
        assertEquals(TopLevelTab.Festivals, chrome.selectedTab)
    }
}
