package com.projetmobile.mobile.ui.screens.auth.register

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class RegisterScreenLayoutModeTest {

    @Test
    fun registerLayoutModeFor_usesExpectedHeightThresholds() {
        assertEquals(RegisterLayoutMode.EmergencyScroll, registerLayoutModeFor(559.dp))
        assertEquals(RegisterLayoutMode.Compact, registerLayoutModeFor(560.dp))
        assertEquals(RegisterLayoutMode.Compact, registerLayoutModeFor(659.dp))
        assertEquals(RegisterLayoutMode.Standard, registerLayoutModeFor(660.dp))
    }
}
