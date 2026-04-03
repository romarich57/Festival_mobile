package com.projetmobile.mobile.ui.screens.reservants

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

internal data class ReservantFormActions(
    val onNameChanged: (String) -> Unit,
    val onEmailChanged: (String) -> Unit,
    val onTypeSelected: (String?) -> Unit,
    val onLinkedEditorSelected: (Int?) -> Unit,
    val onPhoneNumberChanged: (String) -> Unit,
    val onAddressChanged: (String) -> Unit,
    val onSiretChanged: (String) -> Unit,
    val onNotesChanged: (String) -> Unit,
    val onSaveReservant: () -> Unit,
    val onDismissErrorMessage: () -> Unit,
    val onBackToList: () -> Unit,
)

@Composable
internal fun ReservantFormRoute(
    mode: ReservantFormMode,
    loadEditors: ReservantEditorsLoader,
    loadReservant: ReservantLoader,
    createReservant: ReservantSave,
    updateReservant: ReservantUpdate,
    currentUserRole: String?,
    onBackToList: () -> Unit,
    onReservantSaved: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReservantFormViewModel = viewModel(
        factory = reservantFormViewModelFactory(
            mode = mode,
            loadEditors = loadEditors,
            loadReservant = loadReservant,
            createReservant = createReservant,
            updateReservant = updateReservant,
            currentUserRole = currentUserRole,
        ),
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.value.completedMessage, uiState.value.completedReservantId) {
        val message = uiState.value.completedMessage ?: return@LaunchedEffect
        val reservantId = uiState.value.completedReservantId ?: return@LaunchedEffect
        viewModel.consumeCompletion()
        onReservantSaved(reservantId, message)
    }

    val actions = remember(viewModel, onBackToList) {
        ReservantFormActions(
            onNameChanged = viewModel::onNameChanged,
            onEmailChanged = viewModel::onEmailChanged,
            onTypeSelected = viewModel::onTypeSelected,
            onLinkedEditorSelected = viewModel::onLinkedEditorSelected,
            onPhoneNumberChanged = viewModel::onPhoneNumberChanged,
            onAddressChanged = viewModel::onAddressChanged,
            onSiretChanged = viewModel::onSiretChanged,
            onNotesChanged = viewModel::onNotesChanged,
            onSaveReservant = viewModel::saveReservant,
            onDismissErrorMessage = viewModel::dismissErrorMessage,
            onBackToList = onBackToList,
        )
    }

    ReservantFormScreen(
        uiState = uiState.value,
        actions = actions,
        modifier = modifier,
    )
}
