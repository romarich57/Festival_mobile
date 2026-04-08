/**
 * Rôle : Compose l'écran le formulaire de festival et orchestre l'affichage de l'état et des actions utilisateur.
 */

package com.projetmobile.mobile.ui.screens.festivalForm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone
import com.projetmobile.mobile.ui.components.FestivalDatePicker
import com.projetmobile.mobile.ui.components.FestivalTextField

/**
 * Rôle : Écran de création ou modification d'un festival.
 *
 * Précondition : Le ViewModel correspondant doit gérer l'état du formulaire et l'insertion en base de données.
 *
 * Postcondition : Affiche les champs de texte et les date pickers, et soumet les informations au réseau via le ViewModel.
 */
 *
 * Traduction de FestivalFormComponent Angular :
 *  - Chaque FormControl → FestivalTextField + callback onXxxChange()
 *  - Validators → affichés via supportingText (nameError, etc.)
 *  - submit() → viewModel.submit(onSuccess = onBack)
 *  - Zones tarifaires : reportées (TODO)
 *
 * @param viewModel  ViewModel du formulaire.
 * @param onBack     Navigation retour après succès ou annulation.
 */
@Composable
fun FestivalFormScreen(
    viewModel: FestivalFormViewModel,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        // ── Feedback erreur ───────────────────────────────────────────────────
        if (uiState.errorMessage != null) {
            AuthFeedbackBanner(
                message = uiState.errorMessage!!,
                tone = AuthFeedbackTone.Error,
            )
        }

        // ── Nom ───────────────────────────────────────────────────────────────
        FestivalTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            label = "Nom du festival *",
            isError = uiState.nameError != null,
            supportingText = uiState.nameError,
            modifier = Modifier.fillMaxWidth(),
        )

        // ── Dates ─────────────────────────────────────────────────────────────
        // Format attendu par l'API : yyyy-MM-dd (ex: 2025-08-21)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FestivalDatePicker(
                value = uiState.startDate,
                onValueChange = viewModel::onStartDateChange,
                label = "Début *",
                isError = uiState.startDateError != null,
                supportingText = uiState.startDateError,
                modifier = Modifier.weight(1f),
            )
            FestivalDatePicker(
                value = uiState.endDate,
                onValueChange = viewModel::onEndDateChange,
                label = "Fin *",
                isError = uiState.endDateError != null,
                supportingText = uiState.endDateError,
                modifier = Modifier.weight(1f),
            )
        }

        HorizontalDivider()

        // ── Stocks tables ─────────────────────────────────────────────────────
        Text(
            text = "Stocks tables",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FestivalTextField(
                value = uiState.stockTablesStandard,
                onValueChange = viewModel::onStockTablesStandardChange,
                label = "Standard",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            FestivalTextField(
                value = uiState.stockTablesGrande,
                onValueChange = viewModel::onStockTablesGrandeChange,
                label = "Grande",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            FestivalTextField(
                value = uiState.stockTablesMairie,
                onValueChange = viewModel::onStockTablesMairieChange,
                label = "Mairie",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }

        HorizontalDivider()

        // ── Chaises + prix prises ─────────────────────────────────────────────
        Text(
            text = "Autres stocks",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FestivalTextField(
                value = uiState.stockChaises,
                onValueChange = viewModel::onStockChaisesChange,
                label = "Chaises",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            FestivalTextField(
                value = uiState.prixPrises,
                onValueChange = viewModel::onPrixPrisesChange,
                label = "Prix prises (€)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
        }

        HorizontalDivider()

        // ── Zones tarifaires ─────────────────────────────────────────────────
        Text(
            text = "Zones tarifaires",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (uiState.zonesError != null) {
            Text(
                text = uiState.zonesError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        uiState.zonesTarifaires.forEachIndexed { index, zone ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FestivalTextField(
                        value = zone.name,
                        onValueChange = { viewModel.onZoneNameChange(index, it) },
                        label = "Nom de zone *",
                        isError = zone.nameError != null,
                        supportingText = zone.nameError,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        FestivalTextField(
                            value = zone.nbTables,
                            onValueChange = { viewModel.onZoneNbTablesChange(index, it) },
                            label = "Nombre de tables *",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = zone.nbTablesError != null,
                            supportingText = zone.nbTablesError,
                            modifier = Modifier.weight(1f),
                        )
                        FestivalTextField(
                            value = zone.pricePerTable,
                            onValueChange = { viewModel.onZonePricePerTableChange(index, it) },
                            label = "Prix par table (€) *",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = zone.pricePerTableError != null,
                            supportingText = zone.pricePerTableError,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (uiState.zonesTarifaires.size > 1) {
                        TextButton(
                            onClick = { viewModel.removeZone(index) },
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text("Supprimer la zone")
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = viewModel::addZone,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Ajouter une zone")
        }

        Spacer(Modifier.height(8.dp))

        // ── Boutons ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isSubmitting,
            ) {
                Text("Annuler")
            }

            Button(
                onClick = {
                    viewModel.submit { message ->
                        onSaved(message)
                        onBack()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = uiState.isValid && !uiState.isSubmitting,
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Créer")
                }
            }
        }
    }
}
