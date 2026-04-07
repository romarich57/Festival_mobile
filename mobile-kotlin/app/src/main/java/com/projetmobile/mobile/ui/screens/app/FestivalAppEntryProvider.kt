package com.projetmobile.mobile.ui.screens.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import com.projetmobile.mobile.data.repository.admin.AdminRepository
import com.projetmobile.mobile.data.repository.auth.AuthRepository
import com.projetmobile.mobile.data.repository.festival.FestivalRepository
import com.projetmobile.mobile.data.repository.games.GamesRepository
import com.projetmobile.mobile.data.repository.profile.ProfileRepository
import com.projetmobile.mobile.data.repository.reservation.ReservationRepository
import com.projetmobile.mobile.data.repository.reservants.ReservantsRepository
import com.projetmobile.mobile.data.repository.workflow.WorkflowRepository
import com.projetmobile.mobile.data.repository.zonePlan.ZonePlanRepository
import com.projetmobile.mobile.data.entity.festival.canDeleteFestivals
import com.projetmobile.mobile.data.entity.festival.canManageFestivals
import com.projetmobile.mobile.ui.screens.admin.catalog.AdminCatalogRoute
import com.projetmobile.mobile.ui.screens.admin.detail.AdminUserDetailRoute
import com.projetmobile.mobile.ui.screens.admin.form.AdminUserFormMode
import com.projetmobile.mobile.ui.screens.admin.form.AdminUserFormRoute
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
import com.projetmobile.mobile.ui.screens.festivalForm.FestivalFormScreen
import com.projetmobile.mobile.ui.screens.festivalForm.FestivalFormViewModel
import com.projetmobile.mobile.ui.screens.games.GameDetailRoute
import com.projetmobile.mobile.ui.screens.games.GameFormMode
import com.projetmobile.mobile.ui.screens.games.GameFormRoute
import com.projetmobile.mobile.ui.screens.games.GamesCatalogRoute
import com.projetmobile.mobile.ui.screens.profile.ProfileScreen
import com.projetmobile.mobile.ui.screens.profile.ProfileViewModel
import com.projetmobile.mobile.ui.screens.profile.profileViewModelFactory
import com.projetmobile.mobile.ui.screens.reservation.ReservationDashboardScreen
import com.projetmobile.mobile.ui.screens.reservation.ReservationDashboardViewModel
import com.projetmobile.mobile.ui.screens.reservants.ReservantDetailRoute
import com.projetmobile.mobile.ui.screens.reservants.ReservantFormMode
import com.projetmobile.mobile.ui.screens.reservants.ReservantFormRoute
import com.projetmobile.mobile.ui.screens.reservants.ReservantsCatalogRoute
import com.projetmobile.mobile.ui.screens.reservationDetails.ReservationDetailsScreen
import com.projetmobile.mobile.ui.screens.reservationDetails.ReservationTarifaireViewModel
import com.projetmobile.mobile.ui.screens.reservationDetails.WorkflowViewModel
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.ZonePlanViewModel
import com.projetmobile.mobile.ui.screens.reservationform.ReservationFormScreen
import com.projetmobile.mobile.ui.screens.reservationform.ReservationFormViewModel
import com.projetmobile.mobile.ui.utils.navigation.Admin
import com.projetmobile.mobile.ui.utils.navigation.AdminUserCreate
import com.projetmobile.mobile.ui.utils.navigation.AdminUserDetail
import com.projetmobile.mobile.ui.utils.navigation.AdminUserEdit
import com.projetmobile.mobile.ui.utils.navigation.AppNavKey
import com.projetmobile.mobile.ui.utils.navigation.FestivalForm
import com.projetmobile.mobile.ui.utils.navigation.Festivals
import com.projetmobile.mobile.ui.utils.navigation.ForgotPassword
import com.projetmobile.mobile.ui.utils.navigation.GameCreate
import com.projetmobile.mobile.ui.utils.navigation.GameDetails
import com.projetmobile.mobile.ui.utils.navigation.GameEdit
import com.projetmobile.mobile.ui.utils.navigation.Login
import com.projetmobile.mobile.ui.utils.navigation.PendingVerification
import com.projetmobile.mobile.ui.utils.navigation.Profile
import com.projetmobile.mobile.ui.utils.navigation.ReservantCreate
import com.projetmobile.mobile.ui.utils.navigation.ReservantDetails
import com.projetmobile.mobile.ui.utils.navigation.ReservantEdit
import com.projetmobile.mobile.ui.utils.navigation.ReservantGameCreate
import com.projetmobile.mobile.ui.utils.navigation.ReservationDashboard
import com.projetmobile.mobile.ui.utils.navigation.ReservationDetails
import com.projetmobile.mobile.ui.utils.navigation.ReservationForm
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
    reservantsRepository: ReservantsRepository,
    adminRepository: AdminRepository,
    reservationRepository: ReservationRepository,
    workflowRepository: WorkflowRepository,
    zonePlanRepository: ZonePlanRepository,
    sessionUiState: AppSessionUiState,
    sessionViewModel: AppSessionViewModel,
    festivalRefreshSignal: Int,
    festivalFlashMessage: String?,
    gamesRefreshSignal: Int,
    gamesFlashMessage: String?,
    reservantsRefreshSignal: Int,
    reservantsFlashMessage: String?,
    adminRefreshSignal: Int,
    adminFlashMessage: String?,
    onOpenRoot: (TopLevelTab) -> Unit,
    onOpenSecondary: (TopLevelTab, AppNavKey) -> Unit,
    onSelectTopLevelTab: (TopLevelTab) -> Unit,
    onConsumeFestivalFlashMessage: () -> Unit,
    onConsumeGamesFlashMessage: () -> Unit,
    onConsumeReservantsFlashMessage: () -> Unit,
    onConsumeAdminFlashMessage: () -> Unit,
    onFestivalSaved: (String) -> Unit,
    onGamesSaved: (String) -> Unit,
    onReservantSaved: (Int, String) -> Unit,
    onLinkedGameCreated: (Int, String) -> Unit,
    onAdminUserSaved: (String) -> Unit,
    festivalsBackStack: NavBackStack<AppNavKey>,
): (AppNavKey) -> NavEntry<AppNavKey> {
    return { key ->
        when (key) {
            Festivals -> NavEntry(key) {
                val festivalViewModel: FestivalViewModel = viewModel(
                    factory = FestivalViewModel.factory(festivalRepository),
                )
                val currentUserRole = sessionUiState.currentUser?.role
                val canManageFestivalCatalog = canManageFestivals(currentUserRole)
                val canDeleteFestivalCatalog = canDeleteFestivals(currentUserRole)
                LaunchedEffect(festivalRefreshSignal) {
                    if (festivalRefreshSignal == 0) {
                        return@LaunchedEffect
                    }
                    festivalViewModel.consumeExternalRefresh(festivalFlashMessage)
                    onConsumeFestivalFlashMessage()
                }
                FestivalScreen(
                    viewModel = festivalViewModel,
                    canAdd = canManageFestivalCatalog,
                    canDelete = canDeleteFestivalCatalog,
                    onFestivalClick = { festivalId ->
                        if (sessionUiState.currentUser == null) {
                            onOpenRoot(TopLevelTab.Login)
                        } else {
                            festivalsBackStack.add(ReservationDashboard(festivalId))
                        }
                    },
                    onAddClick = {
                        festivalsBackStack.add(FestivalForm)
                    },
                    onDeleteSuccess = onFestivalSaved,
                )
            }

            FestivalForm -> NavEntry(key) {
                val festivalFormViewModel: FestivalFormViewModel = viewModel(
                    factory = FestivalFormViewModel.factory(festivalRepository),
                )
                FestivalFormScreen(
                    viewModel = festivalFormViewModel,
                    onBack = { festivalsBackStack.removeLastOrNull() },
                    onSaved = onFestivalSaved,
                )
            }

            is ReservationDashboard -> NavEntry(key) {
                val reservationDashboardViewModel: ReservationDashboardViewModel = viewModel(
                    factory = ReservationDashboardViewModel.factory(reservationRepository)
                )
                val dashboardUiState by reservationDashboardViewModel.uiState.collectAsStateWithLifecycle()
                val filteredReservations by reservationDashboardViewModel.filteredReservations.collectAsStateWithLifecycle()
                val searchQuery by reservationDashboardViewModel.searchQuery.collectAsStateWithLifecycle()

                ReservationDashboardScreen(
                    festivalId = key.festivalId,
                    uiState = dashboardUiState,
                    filteredReservations = filteredReservations,
                    searchQuery = searchQuery,
                    onSearchQueryChanged = { reservationDashboardViewModel.searchQuery.value = it },
                    onLoadReservations = reservationDashboardViewModel::loadReservations,
                    onDeleteReservation = reservationDashboardViewModel::deleteReservation,
                    onNavigateToDetails = { reservationId -> festivalsBackStack.add(ReservationDetails(reservationId))},
                    onNavigateToCreate = {
                        festivalsBackStack.add(ReservationForm(key.festivalId))
                    }
                )
            }

            is ReservationDetails -> NavEntry(key) {
                val workflowViewModel: WorkflowViewModel = viewModel(
                    factory = WorkflowViewModel.factory(workflowRepository)
                )
                val reservationTarifaireViewModel: ReservationTarifaireViewModel = viewModel(
                    factory = ReservationTarifaireViewModel.factory(reservationRepository, festivalRepository)
                )
                val zonePlanViewModel: ZonePlanViewModel = viewModel(
                    factory = ZonePlanViewModel.factory(zonePlanRepository, reservationRepository)
                )
                ReservationDetailsScreen(
                    reservationId = key.reservationId,
                    workflowViewModel = workflowViewModel,
                    tarifaireViewModel = reservationTarifaireViewModel,
                    zonePlanViewModel = zonePlanViewModel,
                    onBackClick = { festivalsBackStack.removeLastOrNull() }
                )
            }

            is ReservationForm -> NavEntry(key) {
                val reservationFormViewModel: ReservationFormViewModel = viewModel(
                    factory = ReservationFormViewModel.factory(
                        reservationRepository = reservationRepository,
                        reservantsRepository = reservantsRepository,
                    )
                )
                val formUiState by reservationFormViewModel.uiState.collectAsStateWithLifecycle()

                ReservationFormScreen(
                    uiState = formUiState,
                    onUseExistingReservantChanged = reservationFormViewModel::onUseExistingReservantChanged,
                    onSelectedReservantChanged = reservationFormViewModel::onSelectedReservantChanged,
                    onNomChanged = reservationFormViewModel::onNomChanged,
                    onEmailChanged = reservationFormViewModel::onEmailChanged,
                    onTypeChanged = reservationFormViewModel::onTypeChanged,
                    onSubmit = { reservationFormViewModel.createReservation(key.festivalId) },
                    onNavigateBack = {
                        festivalsBackStack.removeLastOrNull()
                    }
                )
            }

            com.projetmobile.mobile.ui.utils.navigation.Reservants -> NavEntry(key) {
                ReservantsCatalogRoute(
                    loadReservants = reservantsRepository::refreshReservants,
                    observeReservants = reservantsRepository.observeReservants(),
                    loadDeleteSummary = reservantsRepository::getDeleteSummary,
                    deleteReservant = reservantsRepository::deleteReservant,
                    currentUserRole = sessionUiState.currentUser?.role,
                    refreshSignal = reservantsRefreshSignal,
                    flashMessage = reservantsFlashMessage,
                    onConsumeFlashMessage = onConsumeReservantsFlashMessage,
                    onCreateReservant = {
                        onOpenSecondary(TopLevelTab.Reservants, ReservantCreate)
                    },
                    onOpenReservantDetails = { reservantId ->
                        onOpenSecondary(TopLevelTab.Reservants, ReservantDetails(reservantId))
                    },
                    onEditReservant = { reservantId ->
                        onOpenSecondary(TopLevelTab.Reservants, ReservantEdit(reservantId))
                    },
                )
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
                    mode = GameFormMode.Create(),
                    onBackToList = { onOpenRoot(TopLevelTab.Games) },
                    onGameSaved = onGamesSaved,
                )
            }

            ReservantCreate -> NavEntry(key) {
                ReservantFormRoute(
                    mode = ReservantFormMode.Create,
                    loadEditors = reservantsRepository::getEditors,
                    loadReservant = reservantsRepository::getReservant,
                    createReservant = reservantsRepository::createReservant,
                    updateReservant = reservantsRepository::updateReservant,
                    currentUserRole = sessionUiState.currentUser?.role,
                    onBackToList = { onOpenRoot(TopLevelTab.Reservants) },
                    onReservantSaved = onReservantSaved,
                )
            }

            is ReservantDetails -> NavEntry(key) {
                ReservantDetailRoute(
                    reservantId = key.reservantId,
                    loadReservant = reservantsRepository::getReservant,
                    loadContacts = reservantsRepository::getContacts,
                    addContact = reservantsRepository::addContact,
                    loadGames = { editorId ->
                        gamesRepository.refreshGames(
                            filters = com.projetmobile.mobile.data.entity.games.GameFilters(editorId = editorId),
                            page = 1,
                            limit = 50,
                        ).map { page -> page.items }
                    },
                    currentUserRole = sessionUiState.currentUser?.role,
                    refreshSignal = reservantsRefreshSignal,
                    flashMessage = reservantsFlashMessage,
                    onConsumeFlashMessage = onConsumeReservantsFlashMessage,
                    onEditReservant = { reservantId ->
                        onOpenSecondary(TopLevelTab.Reservants, ReservantEdit(reservantId))
                    },
                    onOpenGameDetails = { gameId ->
                        onOpenSecondary(TopLevelTab.Games, GameDetails(gameId))
                    },
                    onCreateLinkedGame = { reservantId, editorId ->
                        onOpenSecondary(
                            TopLevelTab.Reservants,
                            ReservantGameCreate(reservantId, editorId),
                        )
                    },
                )
            }

            is ReservantEdit -> NavEntry(key) {
                ReservantFormRoute(
                    mode = ReservantFormMode.Edit(key.reservantId),
                    loadEditors = reservantsRepository::getEditors,
                    loadReservant = reservantsRepository::getReservant,
                    createReservant = reservantsRepository::createReservant,
                    updateReservant = reservantsRepository::updateReservant,
                    currentUserRole = sessionUiState.currentUser?.role,
                    onBackToList = {
                        onOpenSecondary(TopLevelTab.Reservants, ReservantDetails(key.reservantId))
                    },
                    onReservantSaved = onReservantSaved,
                )
            }

            is ReservantGameCreate -> NavEntry(key) {
                GameFormRoute(
                    gamesRepository = gamesRepository,
                    mode = GameFormMode.Create(
                        prefilledEditorId = key.editorId,
                        lockEditorSelection = true,
                    ),
                    onBackToList = {
                        onOpenSecondary(TopLevelTab.Reservants, ReservantDetails(key.reservantId))
                    },
                    onGameSaved = { message ->
                        onLinkedGameCreated(key.reservantId, message)
                    },
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

            Admin -> NavEntry(key) {
                AdminCatalogRoute(
                    adminRepository = adminRepository,
                    adminRefreshSignal = adminRefreshSignal,
                    adminFlashMessage = adminFlashMessage,
                    onConsumeAdminFlashMessage = onConsumeAdminFlashMessage,
                    onCreateUser = { onOpenSecondary(TopLevelTab.Admin, AdminUserCreate) },
                    onOpenUserDetail = { userId ->
                        onOpenSecondary(TopLevelTab.Admin, AdminUserDetail(userId))
                    },
                )
            }

            is AdminUserDetail -> NavEntry(key) {
                AdminUserDetailRoute(
                    adminRepository = adminRepository,
                    userId = key.userId,
                    onEditUser = { userId ->
                        onOpenSecondary(TopLevelTab.Admin, AdminUserEdit(userId))
                    },
                )
            }

            AdminUserCreate -> NavEntry(key) {
                AdminUserFormRoute(
                    adminRepository = adminRepository,
                    mode = AdminUserFormMode.Create,
                    onUserSaved = onAdminUserSaved,
                )
            }

            is AdminUserEdit -> NavEntry(key) {
                AdminUserFormRoute(
                    adminRepository = adminRepository,
                    mode = AdminUserFormMode.Edit(key.userId),
                    onUserSaved = onAdminUserSaved,
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
                    onBackToLogin = { onOpenRoot(TopLevelTab.Login) },
                )
            }
        }
    }
}
