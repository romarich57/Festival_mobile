package com.projetmobile.mobile.ui.screens.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import com.projetmobile.mobile.data.repository.auth.AuthRepository
import com.projetmobile.mobile.data.repository.festival.FestivalRepository
import com.projetmobile.mobile.data.repository.games.GamesRepository
import com.projetmobile.mobile.data.repository.profile.ProfileRepository
import com.projetmobile.mobile.ui.components.ImplementationPlaceholder
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
import com.projetmobile.mobile.ui.screens.games.GameDetailRoute
import com.projetmobile.mobile.ui.screens.games.GameFormMode
import com.projetmobile.mobile.ui.screens.games.GameFormRoute
import com.projetmobile.mobile.ui.screens.games.GamesCatalogRoute
import com.projetmobile.mobile.ui.screens.profile.ProfileScreen
import com.projetmobile.mobile.ui.screens.profile.ProfileViewModel
import com.projetmobile.mobile.ui.screens.profile.profileViewModelFactory
import com.projetmobile.mobile.ui.utils.navigation.AppNavKey
import com.projetmobile.mobile.ui.utils.navigation.Festivals
import com.projetmobile.mobile.ui.utils.navigation.ForgotPassword
import com.projetmobile.mobile.ui.utils.navigation.GameCreate
import com.projetmobile.mobile.ui.utils.navigation.GameDetails
import com.projetmobile.mobile.ui.utils.navigation.GameEdit
import com.projetmobile.mobile.ui.utils.navigation.Login
import com.projetmobile.mobile.ui.utils.navigation.PendingVerification
import com.projetmobile.mobile.ui.utils.navigation.Profile
import com.projetmobile.mobile.ui.utils.navigation.ResetPassword
import com.projetmobile.mobile.ui.utils.navigation.TopLevelTab
import com.projetmobile.mobile.ui.utils.navigation.VerificationResult
import com.projetmobile.mobile.ui.utils.session.AppSessionUiState
import com.projetmobile.mobile.ui.utils.session.AppSessionViewModel

internal fun festivalAppEntryProvider(
    authRepository: AuthRepository,
    festivalRepository: FestivalRepository,
    gamesRepository: GamesRepository,
    profileRepository: ProfileRepository,
    sessionUiState: AppSessionUiState,
    sessionViewModel: AppSessionViewModel,
    gamesRefreshSignal: Int,
    gamesFlashMessage: String?,
    onOpenRoot: (TopLevelTab) -> Unit,
    onOpenSecondary: (TopLevelTab, AppNavKey) -> Unit,
    onSelectTopLevelTab: (TopLevelTab) -> Unit,
    onConsumeGamesFlashMessage: () -> Unit,
    onGamesSaved: (String) -> Unit,
): (AppNavKey) -> NavEntry<AppNavKey> {
    return { key ->
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

            com.projetmobile.mobile.ui.utils.navigation.Reservants -> NavEntry(key) {
                ImplementationPlaceholder()
            }

            com.projetmobile.mobile.ui.utils.navigation.Games -> NavEntry(key) {
                GamesCatalogRoute(
                    gamesRepository = gamesRepository,
                    currentUserRole = sessionUiState.currentUser?.role,
                    gamesRefreshSignal = gamesRefreshSignal,
                    gamesFlashMessage = gamesFlashMessage,
                    onConsumeGamesFlashMessage = onConsumeGamesFlashMessage,
                    onCreateGame = { onOpenSecondary(TopLevelTab.Games, GameCreate) },
                    onOpenGameDetails = { gameId ->
                        onOpenSecondary(TopLevelTab.Games, GameDetails(gameId))
                    },
                    onEditGame = { gameId ->
                        onOpenSecondary(TopLevelTab.Games, GameEdit(gameId))
                    },
                )
            }

            GameCreate -> NavEntry(key) {
                GameFormRoute(
                    gamesRepository = gamesRepository,
                    mode = GameFormMode.Create,
                    onBackToList = { onOpenRoot(TopLevelTab.Games) },
                    onGameSaved = onGamesSaved,
                )
            }

            is GameDetails -> NavEntry(key) {
                GameDetailRoute(
                    gamesRepository = gamesRepository,
                    gameId = key.gameId,
                    currentUserRole = sessionUiState.currentUser?.role,
                    onEditGame = { gameId ->
                        onOpenSecondary(TopLevelTab.Games, GameEdit(gameId))
                    },
                )
            }

            is GameEdit -> NavEntry(key) {
                GameFormRoute(
                    gamesRepository = gamesRepository,
                    mode = GameFormMode.Edit(key.gameId),
                    onBackToList = { onOpenRoot(TopLevelTab.Games) },
                    onGameSaved = onGamesSaved,
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
                    onOpenRoot(TopLevelTab.Login)
                    onSelectTopLevelTab(TopLevelTab.Festivals)
                }

                LoginScreen(
                    uiState = loginUiState,
                    sessionUser = sessionUiState.currentUser,
                    onIdentifierChanged = loginViewModel::onIdentifierChanged,
                    onPasswordChanged = loginViewModel::onPasswordChanged,
                    onSubmit = loginViewModel::submitLogin,
                    onNavigateRegister = { onOpenRoot(TopLevelTab.Register) },
                    onResendVerification = loginViewModel::resendVerification,
                    onNavigateForgotPassword = { onOpenSecondary(TopLevelTab.Login, ForgotPassword) },
                    onNavigateFestivals = { onSelectTopLevelTab(TopLevelTab.Festivals) },
                )
            }

            com.projetmobile.mobile.ui.utils.navigation.Register -> NavEntry(key) {
                val registerViewModel: RegisterViewModel = viewModel(
                    factory = RegisterViewModel.factory(authRepository),
                )
                val registerUiState by registerViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(registerUiState.pendingVerificationEmail) {
                    val pendingEmail = registerUiState.pendingVerificationEmail ?: return@LaunchedEffect
                    registerViewModel.consumePendingVerificationEmail()
                    onOpenSecondary(
                        TopLevelTab.Register,
                        PendingVerification(pendingEmail),
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
                    onNavigateLogin = { onOpenRoot(TopLevelTab.Login) },
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
                    onBackToLogin = { onOpenRoot(TopLevelTab.Login) },
                )
            }

            is VerificationResult -> NavEntry(key) {
                VerificationResultScreen(
                    status = key.status,
                    onNavigateLogin = { onOpenRoot(TopLevelTab.Login) },
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
                    onBackToLogin = { onOpenRoot(TopLevelTab.Login) },
                )
            }

            Profile -> NavEntry(key) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = profileViewModelFactory(
                        profileRepository = profileRepository,
                        initialUser = sessionUiState.currentUser,
                    ),
                )
                val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(
                    profileUiState.pendingSessionUserUpdate?.id,
                    profileUiState.pendingSessionUserUpdate?.email,
                ) {
                    val updatedUser = profileUiState.pendingSessionUserUpdate ?: return@LaunchedEffect
                    sessionViewModel.onUserProfileUpdated(updatedUser)
                    profileViewModel.consumePendingSessionUserUpdate()
                }

                ProfileScreen(
                    uiState = profileUiState,
                    isLoggingOut = sessionUiState.isLoggingOut,
                    onStartEditingField = profileViewModel::startEditingField,
                    onCancelEditing = profileViewModel::cancelEditing,
                    onLoginChanged = profileViewModel::onLoginChanged,
                    onFirstNameChanged = profileViewModel::onFirstNameChanged,
                    onLastNameChanged = profileViewModel::onLastNameChanged,
                    onEmailChanged = profileViewModel::onEmailChanged,
                    onPhoneChanged = profileViewModel::onPhoneChanged,
                    onSaveProfile = profileViewModel::saveProfile,
                    onDismissInfoMessage = profileViewModel::dismissInfoMessage,
                    onSendPasswordReset = profileViewModel::sendPasswordResetLink,
                    onLogout = sessionViewModel::logout,
                )
            }

            com.projetmobile.mobile.ui.utils.navigation.Admin -> NavEntry(key) {
                ImplementationPlaceholder()
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
                    onBackToLogin = { onOpenRoot(TopLevelTab.Login) },
                )
            }
        }
    }
}
