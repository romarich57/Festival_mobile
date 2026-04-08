/**
 * Rôle : Onglet (Tab) permettant d'afficher la tarification liée à une réservation donnée.
 *
 * Précondition : Le contexte d'affichage doit concerner une réservation valide possédant un détail des prix applicables.
 *
 * Postcondition : Le total et le détail de chaque option payante sont présentés clairement par ce composable.
 */
package com.projetmobile.mobile.ui.screens.reservationDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
/**
 * Rôle : Exécute l'action zones tarifaires onglet du module les détails de réservation.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
fun ZonesTarifairesTab(
    reservationId: Int,
    viewModel: ReservationTarifaireViewModel,
) {
    LaunchedEffect(reservationId) {
        viewModel.loadReservation(reservationId)
    }

    when (val state = viewModel.uiState) {
        ReservationTarifaireUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        is ReservationTarifaireUiState.Error -> CenterText(state.message)

        is ReservationTarifaireUiState.Success -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text(
                        text = "Zones tarifaires",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                items(state.zones, key = { it.id }) { zone ->
                    ZoneTarifaireCard(
                        zone = zone,
                        reservedTablesPrice = zoneTablesPrice(zone),
                        availableForReservation = zone.availableTables + zone.reservedTablesInitial,
                        displayedAvailable = zone.availableTables,
                        onTablesChanged = { viewModel.onZoneTablesChanged(zone.id, it) },
                    )
                }

                item {
                    PrisesCard(
                        nbPrises = state.nbPrises,
                        prixParPrise = state.prixPrises,
                        onNbPrisesChanged = viewModel::onNbPrisesChanged,
                    )
                }

                item {
                    RemisesCard(
                        tableDiscountOffered = state.tableDiscountOffered,
                        directDiscount = state.directDiscount,
                        note = state.note,
                        onTableDiscountChanged = viewModel::onTableDiscountChanged,
                        onDirectDiscountChanged = viewModel::onDirectDiscountChanged,
                        onNoteChanged = viewModel::onNoteChanged,
                    )
                }

                item {
                    RecapCard(summary = buildSummary(state))
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (!state.userMessage.isNullOrBlank()) {
                            Text(
                                text = state.userMessage,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }

                        Button(
                            onClick = { viewModel.saveReservation(reservationId) },
                            enabled = !state.isSaving,
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .height(18.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text("Sauvegarder")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
/**
 * Rôle : Exécute l'action zone tarifaire carte du module les détails de réservation.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
private fun ZoneTarifaireCard(
    zone: ZoneTarifaireFormState,
    reservedTablesPrice: Double,
    availableForReservation: Int,
    displayedAvailable: Int,
    onTablesChanged: (String) -> Unit,
) {
    Card(modifier = Modifier
        .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = zone.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${formatCurrency(zone.pricePerTable)} / table (${formatCurrency(zone.m2Price)} / m2)",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Disponibles : $displayedAvailable / ${zone.totalTables} tables",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = zone.reservedTables,
                    onValueChange = onTablesChanged,
                    label = { Text("Tables") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(120.dp),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "= ${formatCurrency(reservedTablesPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
/**
 * Rôle : Exécute l'action prises carte du module les détails de réservation.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
private fun PrisesCard(
    nbPrises: String,
    prixParPrise: Double,
    onNbPrisesChanged: (String) -> Unit,
) {
    Card(modifier = Modifier
        .fillMaxWidth(),
        colors = CardDefaults.cardColors(
        containerColor = Color.White
    )) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Prises electriques", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nbPrises,
                    onValueChange = onNbPrisesChanged,
                    label = { Text("Nombre de prises") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(150.dp),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${formatCurrency(prixParPrise)} / prise",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
/**
 * Rôle : Exécute l'action remises carte du module les détails de réservation.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
private fun RemisesCard(
    tableDiscountOffered: String,
    directDiscount: String,
    note: String,
    onTableDiscountChanged: (String) -> Unit,
    onDirectDiscountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
) {
    Card(modifier = Modifier
        .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Remises", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = tableDiscountOffered,
                onValueChange = onTableDiscountChanged,
                label = { Text("Tables offertes") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = directDiscount,
                onValueChange = onDirectDiscountChanged,
                label = { Text("Remise directe (EUR)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChanged,
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
        }
    }
}

@Composable
/**
 * Rôle : Exécute l'action recap carte du module les détails de réservation.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
private fun RecapCard(summary: ReservationPriceSummary) {
    Card(modifier = Modifier
        .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Recapitulatif", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tables reservees : ${summary.totalTables} (${formatCurrency(summary.tablesPrice)})",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Prises : ${summary.nbPrises}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Prix de base : ${formatCurrency(summary.startPrice)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Prix final : ${formatCurrency(summary.finalPrice)}",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

/**
 * Rôle : Exécute l'action zone tables price du module les détails de réservation.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
private fun zoneTablesPrice(zone: ZoneTarifaireFormState): Double {
    val count = zone.reservedTables.toIntOrNull() ?: 0
    return count * zone.pricePerTable
}

/**
 * Rôle : Formate currency.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
private fun formatCurrency(value: Double): String {
    val formatted = String.format(Locale.FRANCE, "%.2f", value)
    return "EUR$formatted"
}

/**
 * Rôle : Construit résumé.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
private fun buildSummary(state: ReservationTarifaireUiState.Success): ReservationPriceSummary {
    val tablesPrice = state.zones.sumOf { zoneTablesPrice(it) }
    val totalTables = state.zones.sumOf { it.reservedTables.toIntOrNull() ?: 0 }
    val nbPrises = state.nbPrises.toIntOrNull() ?: 0
    val prisesPrice = nbPrises * state.prixPrises
    val startPrice = tablesPrice + prisesPrice

    val tableDiscountOffered = state.tableDiscountOffered.replace(',', '.').toDoubleOrNull() ?: 0.0
    val directDiscount = state.directDiscount.replace(',', '.').toDoubleOrNull() ?: 0.0
    val averageTablePrice = if (totalTables > 0) tablesPrice / totalTables else 0.0
    val tableDiscountValue = tableDiscountOffered * averageTablePrice
    val finalPrice = (startPrice - tableDiscountValue - directDiscount).coerceAtLeast(0.0)

    return ReservationPriceSummary(
        totalTables = totalTables,
        tablesPrice = tablesPrice,
        nbPrises = nbPrises,
        startPrice = startPrice,
        finalPrice = finalPrice,
    )
}

/**
 * Rôle : Décrit le composant réservation price résumé du module les détails de réservation.
 */
private data class ReservationPriceSummary(
    val totalTables: Int,
    val tablesPrice: Double,
    val nbPrises: Int,
    val startPrice: Double,
    val finalPrice: Double,
)
