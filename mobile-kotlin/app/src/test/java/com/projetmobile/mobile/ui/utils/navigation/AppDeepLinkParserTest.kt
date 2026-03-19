package com.projetmobile.mobile.ui.utils.navigation

import com.projetmobile.mobile.data.entity.auth.VerificationResultStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppDeepLinkParserTest {

    @Test
    fun parseAppDeepLink_mapsVerificationStatuses() {
        assertEquals(
            VerificationResult(VerificationResultStatus.Success),
            parseAppDeepLinkString("festivalapp://auth/verification?status=success"),
        )
        assertEquals(
            VerificationResult(VerificationResultStatus.Expired),
            parseAppDeepLinkString("festivalapp://auth/verification?status=expired"),
        )
        assertEquals(
            VerificationResult(VerificationResultStatus.Invalid),
            parseAppDeepLinkString("festivalapp://auth/verification?status=invalid"),
        )
    }

    @Test
    fun parseAppDeepLink_mapsUnknownVerificationStatusToError() {
        assertEquals(
            VerificationResult(VerificationResultStatus.Error),
            parseAppDeepLinkString("festivalapp://auth/verification?status=unexpected"),
        )
    }

    @Test
    fun parseAppDeepLink_mapsResetPasswordTokenIncludingMissingToken() {
        assertEquals(
            ResetPassword("secure-token"),
            parseAppDeepLinkString("festivalapp://auth/reset-password?token=secure-token"),
        )
        assertEquals(
            ResetPassword(null),
            parseAppDeepLinkString("festivalapp://auth/reset-password"),
        )
    }

    @Test
    fun parseAppDeepLink_returnsNullForUnsupportedUris() {
        assertNull(parseAppDeepLinkString(null))
        assertNull(parseAppDeepLinkString("festivalapp://auth/unknown"))
        assertNull(parseAppDeepLinkString("https://example.com/auth/verification?status=success"))
    }
}
