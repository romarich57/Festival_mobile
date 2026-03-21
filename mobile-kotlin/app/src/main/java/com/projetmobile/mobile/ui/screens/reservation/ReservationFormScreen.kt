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

    val typeOptions = listOf("editeur", "boutique", "prestataire", "animateur", "association")
    var expanded by remember { mutableStateOf(false) }

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

        @OptIn(ExperimentalMaterial3Api::class) // Nécessaire pour le composant ExposedDropdownMenuBox
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = type,
                onValueChange = {},
                readOnly = true,
                label = { Text("Type de réservant") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                typeOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption.replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            type = selectionOption
                            expanded = false
                        }
                    )
                }
            }
        }
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