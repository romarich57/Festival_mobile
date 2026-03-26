package com.projetmobile.mobile.ui.screens.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.ReservationCard

@Composable
fun ReservationDashboardScreen(
    festivalId: Int,
    uiState: ReservationDashboardUiState,
    filteredReservations: List<com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onLoadReservations: (Int) -> Unit,
    onDeleteReservation: (Int, Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetails: (Int) -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var idToDelete by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(festivalId) {
        onLoadReservations(festivalId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Dashboard des Réservations",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onNavigateToCreate) {
                Text("Nouvelle Reservation")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            label = { Text("Rechercher un réservant...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null -> Text(
                text = "Erreur: ${uiState.errorMessage}",
                color = MaterialTheme.colorScheme.error
            )
            filteredReservations.isEmpty() -> Text("Aucune réservation trouvée.")
            else -> {
                LazyColumn {
                    items(filteredReservations, key = { it.id }) { reservation ->
                        ReservationCard(
                            reservation = reservation,
                            onViewDetailsClick = { onNavigateToDetails(it) },
                            onDeleteClick = { id ->
                                idToDelete = id
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmer la suppression") },
                text = { Text("Êtes-vous sûr de vouloir supprimer cette réservation ? Cette action est irréversible.") },
                confirmButton = {
                    Button(
                        onClick = {
                            idToDelete?.let { id ->
                                onDeleteReservation(id, festivalId)
                            }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Supprimer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}