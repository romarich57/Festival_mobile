package com.projetmobile.mobile.ui.utils.navigation

import com.projetmobile.mobile.data.entity.auth.VerificationResultStatus
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class AppNavKeySerializationTest {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = false
    }

    @Test
    fun appNavKeys_roundTripThroughSerialization() {
        val keys = listOf<AppNavKey>(
            Festivals,
            Login,
            Register,
            Profile,
            ForgotPassword,
            PendingVerification("user@example.com"),
            PendingVerification(null),
            VerificationResult(VerificationResultStatus.Success),
            VerificationResult(VerificationResultStatus.Expired),
            VerificationResult(VerificationResultStatus.Invalid),
            VerificationResult(VerificationResultStatus.Error),
            ResetPassword("reset-token"),
            ResetPassword(null),
        )

        keys.forEach { key ->
            val encoded = json.encodeToString<AppNavKey>(key)
            val decoded = json.decodeFromString<AppNavKey>(encoded)
            assertEquals(key, decoded)
        }
    }
}
