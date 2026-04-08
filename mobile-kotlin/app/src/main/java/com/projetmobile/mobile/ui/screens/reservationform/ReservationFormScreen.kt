package com.projetmobile.mobile.ui.screens.reservationform

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Rôle : Composant gérant la sous-vue de création globale de session (liaison entre l'éditeur et l'année du festival courant).
 *
 * Précondition : Un ID de festival, avec état en fonction de `Reservant` existant ou nouveau.
 *
 * Postcondition : Collecte et informe le ViewModel quant à la validation finale afin de déclencher le loader d'enregistrement sur API.
 */
@Composable
fun ReservationFormScreen(
    uiState: ReservationFormUiState,
    onUseExistingReservantChanged: (Boolean) -> Unit,
    onSelectedReservantChanged: (Int) -> Unit,
    onNomChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onTypeChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val typeOptions = listOf("editeur", "boutique", "prestataire", "animateur", "association")
    var typeExpanded by remember { mutableStateOf(false) }
    var reservantExpanded by remember { mutableStateOf(false) }

    val selectedReservant = uiState.reservantOptions.firstOrNull { it.id == uiState.selectedReservantId }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text("Nouvelle Réservation", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Utiliser un réservant existant")
            Switch(
                checked = uiState.useExistingReservant,
                onCheckedChange = onUseExistingReservantChanged,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.useExistingReservant) {
            @OptIn(ExperimentalMaterial3Api::class)
            ExposedDropdownMenuBox(
                expanded = reservantExpanded,
                onExpandedChange = {
                    if (uiState.reservantOptions.isNotEmpty()) {
                        reservantExpanded = !reservantExpanded
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = selectedReservant?.name.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Réservant") },
                    placeholder = {
                        if (uiState.reservantOptions.isEmpty()) {
                            Text("Aucun réservant disponible")
                        } else {
                            Text("Sélectionnez un réservant")
                        }
                    },
                    supportingText = { Text("Liste triée par ordre alphabétique (A-Z)") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = reservantExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )

                ExposedDropdownMenu(
                    expanded = reservantExpanded,
                    onDismissRequest = { reservantExpanded = false },
                ) {
                    uiState.reservantOptions.forEach { reservant ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(reservant.name)
                                    Text(
                                        text = reservant.email,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            },
                            onClick = {
                                onSelectedReservantChanged(reservant.id)
                                reservantExpanded = false
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedReservant != null) {
                OutlinedTextField(
                    value = selectedReservant.type.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            OutlinedTextField(
                value = uiState.nom,
                onValueChange = onNomChanged,
                label = { Text("Nom du réservant") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))

            @OptIn(ExperimentalMaterial3Api::class)
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = !typeExpanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = uiState.type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type de réservant") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )

                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false },
                ) {
                    typeOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                onTypeChanged(selectionOption)
                                typeExpanded = false
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onNavigateBack) {
                Text("Annuler")
            }

            Button(
                onClick = onSubmit,
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Créer la réservation")
                }
            }
        }
    }
}
