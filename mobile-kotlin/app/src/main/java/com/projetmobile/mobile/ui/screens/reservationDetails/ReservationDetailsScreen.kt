package com.projetmobile.mobile.ui.screens.reservationDetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.projetmobile.mobile.ui.components.workflow.WorkflowContent
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.ZonePlanTab
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.ZonePlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailsScreen(
    reservationId: Int,
    workflowViewModel: WorkflowViewModel,
    tarifaireViewModel: ReservationTarifaireViewModel,
    zonePlanViewModel: ZonePlanViewModel,
    onBackClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Workflow", "Zones Tarifaires", "Plan")


    Column(modifier = Modifier.fillMaxSize()) {
        // Barre d'onglets (Material 3)
        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        val successState = tarifaireViewModel.uiState as? ReservationTarifaireUiState.Success
        val fId = successState?.festivalId

        // Contenu dynamique selon l'onglet
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTabIndex) {
                0 -> WorkflowTab(reservationId, workflowViewModel)
                1 -> ZonesTarifairesTab(reservationId, tarifaireViewModel)
                2 -> ZonePlanTab(reservationId, zonePlanViewModel)
            }
        }
    }

}

@Composable
fun WorkflowTab(reservationId: Int, viewModel: WorkflowViewModel) {
    // On déclenche le chargement au premier affichage de l'onglet
    LaunchedEffect(reservationId) {
        viewModel.loadWorkflow(reservationId)
    }

    when (val state = viewModel.uiState) {
        is WorkflowUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is WorkflowUiState.Error -> CenterText(state.message)
        is WorkflowUiState.Success -> {
            WorkflowContent(
                workflow = state.workflow,
                isSaving = state.isSaving,
                onSave = { payload -> viewModel.updateWorkflow(state.workflow.id, payload) }
            )
        }
    }
}

@Composable
fun CenterText(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}