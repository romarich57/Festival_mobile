package com.projetmobile.mobile.ui.screens.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.projetmobile.mobile.ui.components.GradientScreenBackground
import com.projetmobile.mobile.ui.utils.navigation.AppChromeState
import com.projetmobile.mobile.ui.utils.navigation.AppNavKey
import com.projetmobile.mobile.ui.utils.navigation.TopLevelTab
import com.projetmobile.mobile.ui.utils.navigation.specFor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FestivalAppScaffold(
    chrome: AppChromeState,
    tabsToShow: List<TopLevelTab>,
    activeBackStack: NavBackStack<AppNavKey>,
    entryDecorators: List<NavEntryDecorator<AppNavKey>>,
    entryProvider: (AppNavKey) -> NavEntry<AppNavKey>,
    selectedTopLevelTab: TopLevelTab,
    isRestoring: Boolean,
    isAuthenticated: Boolean,
    onSelectTopLevelTab: (TopLevelTab) -> Unit,
) {
    GradientScreenBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        if (chrome.title.isNotBlank()) {
                            Text(
                                text = chrome.title,
                                modifier = Modifier.testTag("app-top-bar-title"),
                            )
                        }
                    },
                    navigationIcon = {
                        if (chrome.showBack) {
                            IconButton(
                                modifier = Modifier.testTag("app-back-button"),
                                onClick = { activeBackStack.removeLastOrNull() },
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Retour",
                                )
                            }
                        }
                    },
                )
            },
            bottomBar = {
                if (chrome.showBottomBar) {
                    NavigationBar(containerColor = Color(0xFF20293E)) {
                        tabsToShow.forEach { tab ->
                            val destination = specFor(tab)
                            NavigationBarItem(
                                modifier = Modifier.testTag("bottom-tab-${tab.name}"),
                                selected = chrome.selectedTab == tab,
                                onClick = { onSelectTopLevelTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.label,
                                    )
                                },
                                label = { Text(destination.label) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            if (isRestoring) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                AppNavDisplay(
                    backStack = activeBackStack,
                    entryDecorators = entryDecorators,
                    entryProvider = entryProvider,
                    selectedTopLevelTab = selectedTopLevelTab,
                    innerPadding = innerPadding,
                    isAuthenticated = isAuthenticated,
                    onSelectTopLevelTab = onSelectTopLevelTab,
                )
            }
        }
    }
}

@Composable
private fun AppNavDisplay(
    backStack: NavBackStack<AppNavKey>,
    entryDecorators: List<NavEntryDecorator<AppNavKey>>,
    entryProvider: (AppNavKey) -> NavEntry<AppNavKey>,
    selectedTopLevelTab: TopLevelTab,
    innerPadding: PaddingValues,
    isAuthenticated: Boolean,
    onSelectTopLevelTab: (TopLevelTab) -> Unit,
) {
    NavDisplay(
        backStack = backStack,
        onBack = {
            when {
                backStack.size > 1 -> backStack.removeLastOrNull()
                selectedTopLevelTab != TopLevelTab.Festivals -> {
                    onSelectTopLevelTab(TopLevelTab.Festivals)
                }

                isAuthenticated -> Unit
                else -> Unit
            }
        },
        entryDecorators = entryDecorators,
        entryProvider = entryProvider,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    )
}

@Composable
@Suppress("UNCHECKED_CAST")
internal fun rememberAppNavBackStack(startKey: AppNavKey): NavBackStack<AppNavKey> {
    return rememberNavBackStack(startKey) as NavBackStack<AppNavKey>
}
