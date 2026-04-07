package com.projetmobile.mobile.ui.screens.festival

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone
import com.projetmobile.mobile.ui.components.festival.FestivalList

@Composable
fun FestivalScreen(
    viewModel: FestivalViewModel,
    modifier: Modifier = Modifier,
    canAdd: Boolean = false,
    canDelete: Boolean = false,
    onFestivalClick: (id: Int) -> Unit = {},
    onAddClick: () -> Unit = {},
    onDeleteSuccess: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentFestivalId by viewModel.currentFestivalId.collectAsStateWithLifecycle()
    val pendingDeleteId by viewModel.pendingDeleteFestivalId.collectAsStateWithLifecycle()

    // Dialogue de confirmation
    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Supprimer le festival ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete(onDeleteSuccess) }) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Annuler")
                }
            }
        )
    }

    Column(modifier = modifier) {
        if (uiState.infoMessage != null) {
            AuthFeedbackBanner(
                message = uiState.infoMessage!!,
                tone = AuthFeedbackTone.Success,
                modifier = Modifier.padding(16.dp),
            )
            LaunchedEffect(uiState.infoMessage) {
                kotlinx.coroutines.delay(3500)
                viewModel.consumeInfoMessage()
            }
        }

        // Affichage de l'erreur sous forme de bannière si elle existe
        if (uiState.errorMessage != null && uiState.festivals.isNotEmpty()) {
            AuthFeedbackBanner(
                message = uiState.errorMessage!!,
                tone = AuthFeedbackTone.Error,
                modifier = Modifier.padding(16.dp)
            )
            // Optionnel : masquer l'erreur après quelques secondes ou via un clic
            LaunchedEffect(uiState.errorMessage) {
                kotlinx.coroutines.delay(5000)
                viewModel.consumeError()
            }
        }

        FestivalList(
            festivals = uiState.festivals,
            currentFestivalId = currentFestivalId,
            isLoading = uiState.isLoading,
            errorMessage = if (uiState.festivals.isEmpty()) uiState.errorMessage else null,
            canDelete = canDelete,
            canAdd = canAdd,
            onSelect = { id ->
                if (id != null) {
                    viewModel.selectFestival(id)
                    onFestivalClick(id)
                } else {
                    viewModel.clearSelection()
                }
            },
            onDeleteRequest = { id -> viewModel.requestDeleteFestival(id) },
            onAddClick = onAddClick,
            onRetry = { viewModel.loadFestivals() },
        )
    }
}
