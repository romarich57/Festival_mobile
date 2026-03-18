package com.projetmobile.mobile.ui.screens.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReservationFormScreen(
    festivalId: Int,
    viewModel: ReservationViewModel,
    onNavigateBack: () -> Unit // L'action pour revenir au tableau de bord
) {
    // Les champs de saisie
    var nom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("editeur") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text("Nouvelle Réservation", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nom,
            onValueChange = { nom = it },
            label = { Text("Nom du réservant") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = type,
            onValueChange = { type = it },
            label = { Text("Type (ex: editeur, boutique...)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Boutons en bas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onNavigateBack) {
                Text("Annuler")
            }

            Button(onClick = {
                // 1. On lance la création dans le ViewModel
                viewModel.createReservation(festivalId, nom, email, type)
                // 2. On revient en arrière automatiquement
                onNavigateBack()
            }) {
                Text("Créer la réservation")
            }
        }
    }
}