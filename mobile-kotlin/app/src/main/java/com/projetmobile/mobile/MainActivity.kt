package com.projetmobile.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.projetmobile.mobile.ui.screens.app.FestivalApp
import com.projetmobile.mobile.ui.theme.FestivalMobileTheme
import com.projetmobile.mobile.ui.utils.navigation.AppNavKey
import com.projetmobile.mobile.ui.utils.navigation.parseAppDeepLink
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val appContainer by lazy {
        AppContainer(this)
    }
    private val incomingDestinations = MutableSharedFlow<AppNavKey>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FestivalMobileTheme {
                FestivalApp(
                    appContainer = appContainer,
                    incomingDestinations = incomingDestinations,
                )
            }
        }
        emitDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        emitDeepLink(intent)
    }

    private fun emitDeepLink(intent: Intent?) {
        val destination = parseAppDeepLink(intent?.data) ?: return
        lifecycleScope.launch {
            incomingDestinations.emit(destination)
        }
    }
}
