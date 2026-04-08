/**
 * Rôle : Relie la route de détail d'un réservant à son ViewModel et à l'écran Compose.
 * Ce fichier prépare les callbacks de navigation et les rafraîchissements contextuels autour de la page détail.
 * Précondition : L'identifiant du réservant et les chargeurs nécessaires doivent être fournis par la navigation.
 * Postcondition : Le détail du réservant est affiché avec des actions prêtes à être déclenchées.
 */
package com.projetmobile.mobile.ui.screens.reservants

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Rôle : Décrit le composant réservant détail actions du module les réservants détail.
 */
internal data class ReservantDetailActions(
    val onSelectTab: (ReservantDetailTab) -> Unit,
    val onRetry: () -> Unit,
    val onDismissInfoMessage: () -> Unit,
    val onDismissErrorMessage: () -> Unit,
    val onDismissContactsErrorMessage: () -> Unit,
    val onDismissGamesErrorMessage: () -> Unit,
    val onEditReservant: (Int) -> Unit,
    val onToggleContactForm: () -> Unit,
    val onContactNameChanged: (String) -> Unit,
    val onContactEmailChanged: (String) -> Unit,
    val onContactPhoneNumberChanged: (String) -> Unit,
    val onContactJobTitleChanged: (String) -> Unit,
    val onContactPrioritySelected: (Int) -> Unit,
    val onSaveContact: () -> Unit,
    val onOpenGameDetails: (Int) -> Unit,
    val onCreateLinkedGame: (Int, Int) -> Unit,
)

/**
 * Rôle : Monte l'écran de détail d'un réservant avec ses dépendances de données et ses callbacks de navigation.
 * Précondition : `reservantId`, les chargeurs et les callbacks de sortie doivent être disponibles au moment du routage.
 * Postcondition : L'écran reçoit un état observable et un objet d'actions complet pour piloter le détail.
 */
@Composable
internal fun ReservantDetailRoute(
    reservantId: Int,
    observeReservant: ReservantObserver,
    loadReservant: ReservantLoader,
    loadContacts: ReservantContactsLoader,
    addContact: ReservantContactCreator,
    loadGames: ReservantGamesLoader,
    currentUserRole: String?,
    refreshSignal: Int,
    flashMessage: String?,
    onConsumeFlashMessage: () -> Unit,
    onEditReservant: (Int) -> Unit,
    onOpenGameDetails: (Int) -> Unit,
    onCreateLinkedGame: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReservantDetailViewModel = viewModel(
        factory = reservantDetailViewModelFactory(
            reservantId = reservantId,
            observeReservant = observeReservant,
            loadReservant = loadReservant,
            loadContacts = loadContacts,
            addContact = addContact,
            loadGames = loadGames,
            currentUserRole = currentUserRole,
        ),
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(refreshSignal, flashMessage) {
        if (refreshSignal == 0 && flashMessage == null) {
            return@LaunchedEffect
        }
        viewModel.refreshReservant()
        viewModel.showInfoMessage(flashMessage)
        if (flashMessage != null) {
            onConsumeFlashMessage()
        }
    }

    val actions = remember(viewModel, onEditReservant, onOpenGameDetails, onCreateLinkedGame) {
        ReservantDetailActions(
            onSelectTab = viewModel::selectTab,
            onRetry = viewModel::refreshReservant,
            onDismissInfoMessage = viewModel::dismissInfoMessage,
            onDismissErrorMessage = viewModel::dismissErrorMessage,
            onDismissContactsErrorMessage = viewModel::dismissContactsErrorMessage,
            onDismissGamesErrorMessage = viewModel::dismissGamesErrorMessage,
            onEditReservant = onEditReservant,
            onToggleContactForm = viewModel::toggleContactForm,
            onContactNameChanged = viewModel::onContactNameChanged,
            onContactEmailChanged = viewModel::onContactEmailChanged,
            onContactPhoneNumberChanged = viewModel::onContactPhoneNumberChanged,
            onContactJobTitleChanged = viewModel::onContactJobTitleChanged,
            onContactPrioritySelected = viewModel::onContactPrioritySelected,
            onSaveContact = viewModel::saveContact,
            onOpenGameDetails = onOpenGameDetails,
            onCreateLinkedGame = onCreateLinkedGame,
        )
    }

    ReservantDetailScreen(
        uiState = uiState.value,
        actions = actions,
        modifier = modifier,
    )
}
