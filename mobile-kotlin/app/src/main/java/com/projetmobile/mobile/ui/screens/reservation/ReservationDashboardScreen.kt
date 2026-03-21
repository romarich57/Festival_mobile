package com.projetmobile.mobile.ui.screens.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.ReservationCard

@Composable
fun ReservationDashboardScreen(
    festivalId: Int, // Récupéré depuis la navigation
    viewModel: ReservationViewModel,
    onNavigateToDetails: (Int) -> Unit
) {
    // Les états collectés depuis le ViewModel
    val reservations by viewModel.filteredReservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Lancement au démarrage de l'écran (équivalent du effect() dans le constructor)
    LaunchedEffect(festivalId) {
        viewModel.loadReservations(festivalId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dashboard des Réservations", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // La barre de recherche
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            label = { Text("Rechercher un réservant...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gestion des états
        when {
            isLoading -> CircularProgressIndicator()
            error != null -> Text(text = "Erreur: $error", color = MaterialTheme.colorScheme.error)
            reservations.isEmpty() -> Text("Aucune réservation trouvée.")
            else -> {
                // L'équivalent de ton @for d'Angular
                LazyColumn {
                    items(reservations, key = { it.id }) { reservation ->
                        ReservationCard(
                            reservation = reservation,
                            onViewDetailsClick = { onNavigateToDetails(it) }
                        )
                    }
                }
            }
        }
    }
}