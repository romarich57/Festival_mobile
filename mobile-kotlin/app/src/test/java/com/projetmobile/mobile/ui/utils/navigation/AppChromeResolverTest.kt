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
        )

        assertEquals("Connexion", chrome.title)
        assertFalse(chrome.showBack)
        assertTrue(chrome.showBottomBar)
        assertEquals(TopLevelTab.Login, chrome.selectedTab)
    }

    @Test
    fun chromeFor_pendingVerification_selectsRegisterAndShowsBack() {
        val chrome = chromeFor(
            activeKey = PendingVerification("user@example.com"),
            activeBackStack = listOf(Register, PendingVerification("user@example.com")),
            isAuthenticated = false,
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
        )

        assertEquals("Profil", chrome.title)
        assertFalse(chrome.showBack)
        assertTrue(chrome.showBottomBar)
        assertEquals(TopLevelTab.Profile, chrome.selectedTab)
    }
}
