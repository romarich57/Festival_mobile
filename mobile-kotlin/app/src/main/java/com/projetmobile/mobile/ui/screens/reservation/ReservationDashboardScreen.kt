package com.projetmobile.mobile.ui.screens.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.ReservationCard

/**
 * Rôle : Affiche le tableau de bord des réservations (Dashboard) pour un festival donné.
 *
 * Précondition : Un `festivalId` valide est nécessaire en tant que racine de l'affichage pour la liste des stands alloués.
 *
 * Postcondition : Affiche les KPI des états des réservations (en cours, facturé) ainsi qu'une FlatList filtrable des détails.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Liste des Réservations",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nouvelle réservation"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->

        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .padding(bottom = 1.dp)) {

            HorizontalDivider(
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 8.dp)
                    .padding(start = 10.dp, end = 20.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Nom du réservant:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 10.dp, bottom = 2.dp)
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                label = { Text("Rechercher un réservant...") },
                modifier = Modifier.fillMaxWidth().padding(10.dp)
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
}
