/**
 * Rôle : Squelette racine de l'interface utilisateur de l'application Jetpack Compose.
 * Gère l'état global (les différentes piles de navigation, les flags de rafraîchissement d'écran)
 * et connecte les dépendances aux écrans sous-jacents de la hiérarchie.
 * Précondition : Appelé dans le `setContent` de l'activité principale.
 * Postcondition : Construit et orchestre la navigation Compose de manière réactive.
 */
package com.projetmobile.mobile.ui.screens.app

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.ViewModelStoreNavEntryDecoratorDefaults
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.projetmobile.mobile.AppContainer
import com.projetmobile.mobile.data.repository.admin.AdminRepository
import com.projetmobile.mobile.data.repository.auth.AuthRepository
import com.projetmobile.mobile.data.repository.festival.FestivalRepository
import com.projetmobile.mobile.data.repository.games.GamesRepository
import com.projetmobile.mobile.data.repository.profile.ProfileRepository
import com.projetmobile.mobile.data.repository.reservation.ReservationRepository
import com.projetmobile.mobile.data.repository.reservants.ReservantsRepository
import com.projetmobile.mobile.data.repository.workflow.WorkflowRepository
import com.projetmobile.mobile.data.repository.zonePlan.ZonePlanRepository
import com.projetmobile.mobile.ui.utils.navigation.Admin
import com.projetmobile.mobile.ui.utils.navigation.AdminUserCreate
import com.projetmobile.mobile.ui.utils.navigation.AdminUserDetail
import com.projetmobile.mobile.ui.utils.navigation.AdminUserEdit
import com.projetmobile.mobile.ui.utils.navigation.AppNavKey
import com.projetmobile.mobile.ui.utils.navigation.FestivalForm
import com.projetmobile.mobile.ui.utils.navigation.Festivals
import com.projetmobile.mobile.ui.utils.navigation.GameCreate
import com.projetmobile.mobile.ui.utils.navigation.GameDetails
import com.projetmobile.mobile.ui.utils.navigation.GameEdit
import com.projetmobile.mobile.ui.utils.navigation.Games
import com.projetmobile.mobile.ui.utils.navigation.Login
import com.projetmobile.mobile.ui.utils.navigation.Profile
import com.projetmobile.mobile.ui.utils.navigation.Register
import com.projetmobile.mobile.ui.utils.navigation.ReservantCreate
import com.projetmobile.mobile.ui.utils.navigation.ReservantDetails
import com.projetmobile.mobile.ui.utils.navigation.ReservantEdit
import com.projetmobile.mobile.ui.utils.navigation.ReservantGameCreate
import com.projetmobile.mobile.ui.utils.navigation.Reservants
import com.projetmobile.mobile.ui.utils.navigation.ReservationDashboard
import com.projetmobile.mobile.ui.utils.navigation.ReservationDetails
import com.projetmobile.mobile.ui.utils.navigation.ReservationForm
import com.projetmobile.mobile.ui.utils.navigation.TopLevelTab
import com.projetmobile.mobile.ui.utils.navigation.chromeFor
import com.projetmobile.mobile.ui.utils.navigation.ownerTab
import com.projetmobile.mobile.ui.utils.navigation.specFor
import com.projetmobile.mobile.ui.utils.navigation.visibleTabs
import com.projetmobile.mobile.ui.utils.session.AppSessionViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Rôle : Exécute l'action festival app du module app.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
fun FestivalApp(
    appContainer: AppContainer,
    incomingDestinations: Flow<AppNavKey> = emptyFlow(),
) {
    FestivalApp(
        authRepository = appContainer.authRepository,
        festivalRepository = appContainer.festivalRepository,
        gamesRepository = appContainer.gamesRepository,
        profileRepository = appContainer.profileRepository,
        reservantsRepository = appContainer.reservantsRepository,
        adminRepository = appContainer.adminRepository,
        reservationRepository = appContainer.reservationRepository,
        workflowRepository = appContainer.workflowRepository,
        zonePlanRepository = appContainer.zonePlanRepository,
        incomingDestinations = incomingDestinations,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Rôle : Exécute l'action festival app du module app.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
fun FestivalApp(
    authRepository: AuthRepository,
    festivalRepository: FestivalRepository,
    gamesRepository: GamesRepository,
    profileRepository: ProfileRepository,
    reservantsRepository: ReservantsRepository,
    adminRepository: AdminRepository,
    reservationRepository: ReservationRepository,
    workflowRepository: WorkflowRepository,
    zonePlanRepository: ZonePlanRepository,
    incomingDestinations: Flow<AppNavKey> = emptyFlow(),
) {
    val sessionViewModel: AppSessionViewModel = viewModel(
        factory = AppSessionViewModel.factory(authRepository),
    )
    val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()

    val festivalsBackStack = rememberAppNavBackStack(Festivals)
    val reservantsBackStack = rememberAppNavBackStack(Reservants)
    val gamesBackStack = rememberAppNavBackStack(Games)
    val loginBackStack = rememberAppNavBackStack(Login)
    val registerBackStack = rememberAppNavBackStack(Register)
    val profileBackStack = rememberAppNavBackStack(Profile)
    val adminBackStack = rememberAppNavBackStack(Admin)

    var selectedTopLevelTab by rememberSaveable { mutableStateOf(TopLevelTab.Festivals) }
    var previousAuthenticationState by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var gamesRefreshSignal by rememberSaveable { mutableStateOf(0) }
    var gamesFlashMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var festivalRefreshSignal by rememberSaveable { mutableStateOf(0) }
    var festivalFlashMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var reservantsRefreshSignal by rememberSaveable { mutableStateOf(0) }
    var reservantsFlashMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var adminRefreshSignal by rememberSaveable { mutableStateOf(0) }
    var adminFlashMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val isAuthenticated = sessionUiState.currentUser != null
    val userRole = sessionUiState.currentUser?.role
    val tabsToShow = visibleTabs(
        isAuthenticated = isAuthenticated,
        userRole = userRole,
    )
    val saveableStateHolder = rememberSaveableStateHolder()
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "FestivalApp requires a ViewModelStoreOwner."
    }
    val removeViewModelStoreOnPop = ViewModelStoreNavEntryDecoratorDefaults.removeViewModelStoreOnPop()

    /**
     * Rôle : Exécute l'action back stack for du module app.
     *
     * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
     *
     * Postcondition : Le résultat reflète l'opération demandée.
     */
    fun backStackFor(tab: TopLevelTab): NavBackStack<AppNavKey> {
        return when (tab) {
            TopLevelTab.Festivals -> festivalsBackStack
            TopLevelTab.Reservants -> reservantsBackStack
            TopLevelTab.Games -> gamesBackStack
            TopLevelTab.Login -> loginBackStack
            TopLevelTab.Register -> registerBackStack
            TopLevelTab.Profile -> profileBackStack
            TopLevelTab.Admin -> adminBackStack
        }
    }

    /**
     * Rôle : Exécute l'action réinitialisation to root du module app.
     *
     * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
     *
     * Postcondition : Le résultat reflète l'opération demandée.
     */
    fun resetToRoot(tab: TopLevelTab) {
        val stack = backStackFor(tab)
        val rootKey = specFor(tab).rootKey
        stack.clear()
        stack.add(rootKey)
    }

    /**
     * Rôle : Ouvre root.
     *
     * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
     *
     * Postcondition : Le résultat reflète l'opération demandée.
     */
    fun openRoot(tab: TopLevelTab) {
        resetToRoot(tab)
        selectedTopLevelTab = tab
    }

    /**
     * Rôle : Ouvre secondary.
     *
     * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
     *
     * Postcondition : Le résultat reflète l'opération demandée.
     */
    fun openSecondary(tab: TopLevelTab, destination: AppNavKey) {
        val stack = backStackFor(tab)
        val rootKey = specFor(tab).rootKey
        // On repart toujours de la racine du module avant d'ajouter une destination secondaire.
        stack.clear()
        stack.add(rootKey)
        if (destination != rootKey) {
            // La destination racine ne doit pas être dupliquée dans la pile.
            stack.add(destination)
        }
        selectedTopLevelTab = tab
    }

    /**
     * Rôle : Exécute l'action réinitialisation private stacks du module app.
     *
     * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
     *
     * Postcondition : Le résultat reflète l'opération demandée.
     */
    fun resetPrivateStacks() {
        listOf(
            TopLevelTab.Festivals,
            TopLevelTab.Reservants,
            TopLevelTab.Games,
            TopLevelTab.Profile,
            TopLevelTab.Admin,
        ).forEach(::resetToRoot)
    }

    val entryProvider = festivalAppEntryProvider(
        authRepository = authRepository,
        festivalRepository = festivalRepository,
        gamesRepository = gamesRepository,
        profileRepository = profileRepository,
        reservantsRepository = reservantsRepository,
        adminRepository = adminRepository,
        reservationRepository = reservationRepository,
        workflowRepository = workflowRepository,
        zonePlanRepository = zonePlanRepository,
        sessionUiState = sessionUiState,
        sessionViewModel = sessionViewModel,
        festivalRefreshSignal = festivalRefreshSignal,
        festivalFlashMessage = festivalFlashMessage,
        gamesRefreshSignal = gamesRefreshSignal,
        gamesFlashMessage = gamesFlashMessage,
        reservantsRefreshSignal = reservantsRefreshSignal,
        reservantsFlashMessage = reservantsFlashMessage,
        adminRefreshSignal = adminRefreshSignal,
        adminFlashMessage = adminFlashMessage,
        onOpenRoot = ::openRoot,
        onOpenSecondary = ::openSecondary,
        onSelectTopLevelTab = { selectedTopLevelTab = it },
        onConsumeFestivalFlashMessage = { festivalFlashMessage = null },
        onConsumeGamesFlashMessage = { gamesFlashMessage = null },
        onConsumeReservantsFlashMessage = { reservantsFlashMessage = null },
        onConsumeAdminFlashMessage = { adminFlashMessage = null },
        onFestivalSaved = { message ->
            festivalFlashMessage = message
            festivalRefreshSignal += 1
        },
        onGamesSaved = { message ->
            gamesFlashMessage = message
            gamesRefreshSignal += 1
            openRoot(TopLevelTab.Games)
        },
        onReservantSaved = { reservantId, message ->
            reservantsFlashMessage = message
            reservantsRefreshSignal += 1
            openSecondary(TopLevelTab.Reservants, ReservantDetails(reservantId))
        },
        onLinkedGameCreated = { reservantId, message ->
            reservantsFlashMessage = message
            reservantsRefreshSignal += 1
            openSecondary(TopLevelTab.Reservants, ReservantDetails(reservantId))
        },
        onAdminUserSaved = { message ->
            adminFlashMessage = message
            adminRefreshSignal += 1
            openRoot(TopLevelTab.Admin)
        },
        festivalsBackStack = festivalsBackStack,
    )

    val entryDecorators: List<NavEntryDecorator<AppNavKey>> = listOf(
        rememberSaveableStateHolderNavEntryDecorator<AppNavKey>(saveableStateHolder),
        rememberViewModelStoreNavEntryDecorator<AppNavKey>(
            viewModelStoreOwner = viewModelStoreOwner,
            removeViewModelStoreOnPop = removeViewModelStoreOnPop,
        ),
    )

    val resolvedSelectedTopLevelTab = selectedTopLevelTab
        .takeIf { it in tabsToShow }
        ?: when {
            !isAuthenticated && TopLevelTab.Login in tabsToShow -> TopLevelTab.Login
            else -> tabsToShow.first()
        }
    val activeBackStack = backStackFor(resolvedSelectedTopLevelTab)
    val activeKey = activeBackStack.lastOrNull() ?: specFor(resolvedSelectedTopLevelTab).rootKey
    val chrome = chromeFor(
        activeKey = activeKey,
        activeBackStack = activeBackStack.toList(),
        isAuthenticated = isAuthenticated,
        userRole = userRole,
    )

    LaunchedEffect(isAuthenticated) {
        when {
            previousAuthenticationState == null -> {
                previousAuthenticationState = isAuthenticated
            }

            previousAuthenticationState == true && !isAuthenticated -> {
                // La déconnexion invalide les piles privées pour éviter de réafficher un écran protégé.
                resetPrivateStacks()
                resetToRoot(TopLevelTab.Login)
                selectedTopLevelTab = TopLevelTab.Login
                previousAuthenticationState = false
            }

            previousAuthenticationState == false && isAuthenticated -> {
                // Après authentification, on repart sur l'accueil public avant de restaurer la navigation courante.
                resetToRoot(TopLevelTab.Login)
                selectedTopLevelTab = TopLevelTab.Festivals
                previousAuthenticationState = true
            }

            else -> {
                previousAuthenticationState = isAuthenticated
            }
        }
    }

    LaunchedEffect(resolvedSelectedTopLevelTab) {
        if (selectedTopLevelTab != resolvedSelectedTopLevelTab) {
            selectedTopLevelTab = resolvedSelectedTopLevelTab
        }
    }

    LaunchedEffect(incomingDestinations, isAuthenticated) {
        // Les destinations externes sont normalisées selon l'état d'authentification avant d'être poussées.
        incomingDestinations.collect { destination ->
            if (isAuthenticated && ownerTab(destination) != TopLevelTab.Festivals) {
                // Un utilisateur connecté ne doit pas être redirigé vers une zone publique intermédiaire.
                selectedTopLevelTab = TopLevelTab.Festivals
                return@collect
            }

            when (ownerTab(destination)) {
                TopLevelTab.Festivals -> openRoot(TopLevelTab.Festivals)
                TopLevelTab.Reservants -> when (destination) {
                    Reservants -> openRoot(TopLevelTab.Reservants)
                    ReservantCreate -> openSecondary(TopLevelTab.Reservants, ReservantCreate)
                    is ReservantDetails -> openSecondary(TopLevelTab.Reservants, destination)
                    is ReservantEdit -> openSecondary(TopLevelTab.Reservants, destination)
                    is ReservantGameCreate -> openSecondary(TopLevelTab.Reservants, destination)
                    else -> openRoot(TopLevelTab.Reservants)
                }
                TopLevelTab.Games -> when (destination) {
                    Games -> openRoot(TopLevelTab.Games)
                    GameCreate -> openSecondary(TopLevelTab.Games, GameCreate)
                    is GameDetails -> openSecondary(TopLevelTab.Games, destination)
                    is GameEdit -> openSecondary(TopLevelTab.Games, destination)
                    else -> openRoot(TopLevelTab.Games)
                }
                TopLevelTab.Login -> openSecondary(TopLevelTab.Login, destination)
                TopLevelTab.Register -> openSecondary(TopLevelTab.Register, destination)
                TopLevelTab.Profile -> openRoot(TopLevelTab.Profile)
                TopLevelTab.Admin -> when (destination) {
                    Admin -> openRoot(TopLevelTab.Admin)
                    AdminUserCreate -> openSecondary(TopLevelTab.Admin, AdminUserCreate)
                    is AdminUserDetail -> openSecondary(TopLevelTab.Admin, destination)
                    is AdminUserEdit -> openSecondary(TopLevelTab.Admin, destination)
                    else -> openRoot(TopLevelTab.Admin)
                }
            }
        }
    }

    FestivalAppScaffold(
        chrome = chrome,
        tabsToShow = tabsToShow,
        activeBackStack = activeBackStack,
        entryDecorators = entryDecorators,
        entryProvider = entryProvider,
        selectedTopLevelTab = resolvedSelectedTopLevelTab,
        isRestoring = sessionUiState.isRestoring,
        isAuthenticated = isAuthenticated,
        onSelectTopLevelTab = { selectedTopLevelTab = it },
    )
}
