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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.ViewModelStoreNavEntryDecoratorDefaults
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.projetmobile.mobile.AppContainer
import com.projetmobile.mobile.data.repository.auth.AuthRepository
import com.projetmobile.mobile.data.repository.festival.FestivalRepository
import com.projetmobile.mobile.ui.components.GradientScreenBackground
import com.projetmobile.mobile.ui.screens.auth.emailverification.PendingVerificationScreen
import com.projetmobile.mobile.ui.screens.auth.emailverification.PendingVerificationViewModel
import com.projetmobile.mobile.ui.screens.auth.emailverification.VerificationResultScreen
import com.projetmobile.mobile.ui.screens.auth.forgotpassword.ForgotPasswordScreen
import com.projetmobile.mobile.ui.screens.auth.forgotpassword.ForgotPasswordViewModel
import com.projetmobile.mobile.ui.screens.auth.login.LoginScreen
import com.projetmobile.mobile.ui.screens.auth.login.LoginViewModel
import com.projetmobile.mobile.ui.screens.auth.register.RegisterScreen
import com.projetmobile.mobile.ui.screens.auth.register.RegisterViewModel
import com.projetmobile.mobile.ui.screens.auth.resetpassword.ResetPasswordScreen
import com.projetmobile.mobile.ui.screens.auth.resetpassword.ResetPasswordViewModel
import com.projetmobile.mobile.ui.screens.festival.FestivalScreen
import com.projetmobile.mobile.ui.screens.festival.FestivalViewModel
import com.projetmobile.mobile.ui.screens.profile.ProfileScreen
import com.projetmobile.mobile.ui.utils.navigation.AppNavKey
import com.projetmobile.mobile.ui.utils.navigation.Festivals
import com.projetmobile.mobile.ui.utils.navigation.ForgotPassword
import com.projetmobile.mobile.ui.utils.navigation.Login
import com.projetmobile.mobile.ui.utils.navigation.PendingVerification
import com.projetmobile.mobile.ui.utils.navigation.Profile
import com.projetmobile.mobile.ui.utils.navigation.Register
import com.projetmobile.mobile.ui.utils.navigation.ResetPassword
import com.projetmobile.mobile.ui.utils.navigation.TopLevelTab
import com.projetmobile.mobile.ui.utils.navigation.VerificationResult
import com.projetmobile.mobile.ui.utils.navigation.chromeFor
import com.projetmobile.mobile.ui.utils.navigation.ownerTab
import com.projetmobile.mobile.ui.utils.navigation.specFor
import com.projetmobile.mobile.ui.utils.navigation.visibleTabs
import com.projetmobile.mobile.ui.utils.session.AppSessionViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalApp(
    appContainer: AppContainer,
    incomingDestinations: Flow<AppNavKey> = emptyFlow(),
) {
    FestivalApp(
        authRepository = appContainer.authRepository,
        festivalRepository = appContainer.festivalRepository,
        incomingDestinations = incomingDestinations,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalApp(
    authRepository: AuthRepository,
    festivalRepository: FestivalRepository,
    incomingDestinations: Flow<AppNavKey> = emptyFlow(),
) {
    val sessionViewModel: AppSessionViewModel = viewModel(
        factory = AppSessionViewModel.factory(authRepository),
    )
    val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()

    val festivalsBackStack = rememberAppNavBackStack(Festivals)
    val loginBackStack = rememberAppNavBackStack(Login)
    val registerBackStack = rememberAppNavBackStack(Register)
    val profileBackStack = rememberAppNavBackStack(Profile)

    var selectedTopLevelTab by rememberSaveable { mutableStateOf(TopLevelTab.Festivals) }
    var previousAuthenticationState by rememberSaveable { mutableStateOf<Boolean?>(null) }

    val isAuthenticated = sessionUiState.currentUser != null
    val tabsToShow = visibleTabs(isAuthenticated)
    val saveableStateHolder = rememberSaveableStateHolder()
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "FestivalApp requires a ViewModelStoreOwner."
    }
    val removeViewModelStoreOnPop = ViewModelStoreNavEntryDecoratorDefaults.removeViewModelStoreOnPop()

    fun backStackFor(tab: TopLevelTab): NavBackStack<AppNavKey> {
        return when (tab) {
            TopLevelTab.Festivals -> festivalsBackStack
            TopLevelTab.Login -> loginBackStack
            TopLevelTab.Register -> registerBackStack
            TopLevelTab.Profile -> profileBackStack
        }
    }

    fun resetToRoot(tab: TopLevelTab) {
        val stack = backStackFor(tab)
        val rootKey = specFor(tab).rootKey
        stack.clear()
        stack.add(rootKey)
    }

    fun openRoot(tab: TopLevelTab) {
        resetToRoot(tab)
        selectedTopLevelTab = tab
    }

    fun openSecondary(tab: TopLevelTab, destination: AppNavKey) {
        val stack = backStackFor(tab)
        val rootKey = specFor(tab).rootKey
        stack.clear()
        stack.add(rootKey)
        if (destination != rootKey) {
            stack.add(destination)
        }
        selectedTopLevelTab = tab
    }

    val entryProvider: (AppNavKey) -> NavEntry<AppNavKey> = { key ->
        when (key) {
            Festivals -> NavEntry(key) {
                val festivalViewModel: FestivalViewModel = viewModel(
                    factory = FestivalViewModel.factory(festivalRepository),
                )
                val festivalUiState by festivalViewModel.uiState.collectAsStateWithLifecycle()
                FestivalScreen(
                    uiState = festivalUiState,
                    onRetry = festivalViewModel::loadFestivals,
                )
            }

            Login -> NavEntry(key) {
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModel.factory(authRepository),
                )
                val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(loginUiState.authenticatedUser?.id) {
                    val user = loginUiState.authenticatedUser ?: return@LaunchedEffect
                    sessionViewModel.onUserAuthenticated(user)
                    loginViewModel.consumeAuthenticatedUser()
                    resetToRoot(TopLevelTab.Login)
                    selectedTopLevelTab = TopLevelTab.Festivals
                }

                LoginScreen(
                    uiState = loginUiState,
                    sessionUser = sessionUiState.currentUser,
                    onIdentifierChanged = loginViewModel::onIdentifierChanged,
                    onPasswordChanged = loginViewModel::onPasswordChanged,
                    onSubmit = loginViewModel::submitLogin,
                    onNavigateRegister = { openRoot(TopLevelTab.Register) },
                    onResendVerification = loginViewModel::resendVerification,
                    onNavigateForgotPassword = { openSecondary(TopLevelTab.Login, ForgotPassword) },
                    onNavigateFestivals = { selectedTopLevelTab = TopLevelTab.Festivals },
                )
            }

            Register -> NavEntry(key) {
                val registerViewModel: RegisterViewModel = viewModel(
                    factory = RegisterViewModel.factory(authRepository),
                )
                val registerUiState by registerViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(registerUiState.pendingVerificationEmail) {
                    val pendingEmail = registerUiState.pendingVerificationEmail ?: return@LaunchedEffect
                    registerViewModel.consumePendingVerificationEmail()
                    openSecondary(
                        tab = TopLevelTab.Register,
                        destination = PendingVerification(pendingEmail),
                    )
                }

                RegisterScreen(
                    uiState = registerUiState,
                    onUsernameChanged = registerViewModel::onUsernameChanged,
                    onFirstNameChanged = registerViewModel::onFirstNameChanged,
                    onLastNameChanged = registerViewModel::onLastNameChanged,
                    onEmailChanged = registerViewModel::onEmailChanged,
                    onPasswordChanged = registerViewModel::onPasswordChanged,
                    onPhoneChanged = registerViewModel::onPhoneChanged,
                    onSubmit = registerViewModel::submitRegistration,
                    onNavigateLogin = { openRoot(TopLevelTab.Login) },
                )
            }

            is PendingVerification -> NavEntry(key) {
                val pendingViewModel: PendingVerificationViewModel = viewModel(
                    factory = PendingVerificationViewModel.factory(
                        authRepository = authRepository,
                        initialEmail = key.email,
                    ),
                )
                val pendingUiState by pendingViewModel.uiState.collectAsStateWithLifecycle()
                PendingVerificationScreen(
                    uiState = pendingUiState,
                    onResendVerification = pendingViewModel::resendVerification,
                    onBackToLogin = { openRoot(TopLevelTab.Login) },
                )
            }

            is VerificationResult -> NavEntry(key) {
                VerificationResultScreen(
                    status = key.status,
                    onNavigateLogin = { openRoot(TopLevelTab.Login) },
                )
            }

            ForgotPassword -> NavEntry(key) {
                val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel(
                    factory = ForgotPasswordViewModel.factory(authRepository),
                )
                val forgotPasswordUiState by forgotPasswordViewModel.uiState.collectAsStateWithLifecycle()

                ForgotPasswordScreen(
                    uiState = forgotPasswordUiState,
                    onEmailChanged = forgotPasswordViewModel::onEmailChanged,
                    onSubmit = forgotPasswordViewModel::submitPasswordResetRequest,
                    onBackToLogin = { openRoot(TopLevelTab.Login) },
                )
            }

            Profile -> NavEntry(key) {
                ProfileScreen(
                    currentUser = sessionUiState.currentUser,
                    isLoggingOut = sessionUiState.isLoggingOut,
                    errorMessage = sessionUiState.errorMessage,
                    onLogout = sessionViewModel::logout,
                )
            }

            is ResetPassword -> NavEntry(key) {
                val resetPasswordViewModel: ResetPasswordViewModel = viewModel(
                    factory = ResetPasswordViewModel.factory(
                        authRepository = authRepository,
                        initialToken = key.token,
                    ),
                )
                val resetPasswordUiState by resetPasswordViewModel.uiState.collectAsStateWithLifecycle()

                ResetPasswordScreen(
                    uiState = resetPasswordUiState,
                    onPasswordChanged = resetPasswordViewModel::onPasswordChanged,
                    onConfirmationChanged = resetPasswordViewModel::onConfirmationChanged,
                    onSubmit = resetPasswordViewModel::submitPasswordReset,
                    onBackToLogin = { openRoot(TopLevelTab.Login) },
                )
            }
        }
    }

    val entryDecorators: List<NavEntryDecorator<AppNavKey>> = listOf(
        rememberSaveableStateHolderNavEntryDecorator<AppNavKey>(saveableStateHolder),
        rememberViewModelStoreNavEntryDecorator<AppNavKey>(
            viewModelStoreOwner = viewModelStoreOwner,
            removeViewModelStoreOnPop = removeViewModelStoreOnPop,
        ),
    )

    if (selectedTopLevelTab !in tabsToShow) {
        selectedTopLevelTab = tabsToShow.first()
    }

    val activeBackStack = backStackFor(selectedTopLevelTab)
    val activeKey = activeBackStack.lastOrNull() ?: specFor(selectedTopLevelTab).rootKey
    val chrome = chromeFor(
        activeKey = activeKey,
        activeBackStack = activeBackStack.toList(),
        isAuthenticated = isAuthenticated,
    )

    LaunchedEffect(isAuthenticated) {
        when {
            previousAuthenticationState == null -> {
                previousAuthenticationState = isAuthenticated
            }

            previousAuthenticationState == true && !isAuthenticated -> {
                resetToRoot(TopLevelTab.Profile)
                resetToRoot(TopLevelTab.Login)
                selectedTopLevelTab = TopLevelTab.Login
                previousAuthenticationState = false
            }

            previousAuthenticationState == false && isAuthenticated -> {
                resetToRoot(TopLevelTab.Login)
                selectedTopLevelTab = TopLevelTab.Festivals
                previousAuthenticationState = true
            }

            else -> {
                previousAuthenticationState = isAuthenticated
            }
        }
    }

    LaunchedEffect(incomingDestinations, isAuthenticated) {
        incomingDestinations.collect { destination ->
            if (isAuthenticated && ownerTab(destination) != TopLevelTab.Festivals) {
                selectedTopLevelTab = TopLevelTab.Festivals
                return@collect
            }

            when (ownerTab(destination)) {
                TopLevelTab.Festivals -> openRoot(TopLevelTab.Festivals)
                TopLevelTab.Login -> openSecondary(TopLevelTab.Login, destination)
                TopLevelTab.Register -> openSecondary(TopLevelTab.Register, destination)
                TopLevelTab.Profile -> openRoot(TopLevelTab.Profile)
            }
        }
    }

    GradientScreenBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = chrome.title,
                            modifier = Modifier.testTag("app-top-bar-title"),
                        )
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
                                onClick = { selectedTopLevelTab = tab },
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
            if (sessionUiState.isRestoring) {
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
                    onSelectTopLevelTab = { selectedTopLevelTab = it },
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
private fun rememberAppNavBackStack(startKey: AppNavKey): NavBackStack<AppNavKey> {
    return rememberNavBackStack(startKey) as NavBackStack<AppNavKey>
}
