package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.placement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.StockState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementFormCard(
    form: PlacementFormState,
    unplacedGames: List<GameAllocationState>,
    stock: StockState,
    isSaving: Boolean,
    onWithGameChanged: (Boolean) -> Unit,
    onGameSelected: (Int) -> Unit,
    onPlacePerCopyChanged: (String) -> Unit,
    onNbCopiesChanged: (String) -> Unit,
    onTableTypeChanged: (String) -> Unit,
    onChairsChanged: (String) -> Unit,
    onNbTablesChanged: (String) -> Unit,
    onUseM2Changed: (Boolean) -> Unit,
    onM2ValueChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nouveau placement", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            // Toggle: with game or without
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Avec jeu", modifier = Modifier.weight(1f))
                Switch(checked = form.withGame, onCheckedChange = onWithGameChanged)
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (form.withGame) {
                // Game selector
                GameSelectorDropdown(
                    games = unplacedGames,
                    selectedId = form.selectedGameAllocationId,
                    onSelected = onGameSelected,
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Place per copy
                OutlinedTextField(
                    value = form.placePerCopy,
                    onValueChange = onPlacePerCopyChanged,
                    label = { Text("Place par exemplaire (tables)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Nb copies
                OutlinedTextField(
                    value = form.nbCopies,
                    onValueChange = onNbCopiesChanged,
                    label = { Text("Nombre d'exemplaires") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                // Show computed total tables
                val placePerCopy = form.placePerCopy.replace(',', '.').toDoubleOrNull() ?: 0.0
                val nbCopies = form.nbCopies.replace(',', '.').toDoubleOrNull() ?: 0.0
                val totalTables = placePerCopy * nbCopies
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total tables occupées : ${"%.1f".format(totalTables)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
            } else {
                // Without game: tables or m2
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = !form.useM2,
                        onClick = { onUseM2Changed(false) },
                        label = { Text("Tables") },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = form.useM2,
                        onClick = { onUseM2Changed(true) },
                        label = { Text("m²") },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (form.useM2) {
                    OutlinedTextField(
                        value = form.m2Value,
                        onValueChange = onM2ValueChanged,
                        label = { Text("Surface (m²)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = {
                            val tables = form.nbTables.toIntOrNull() ?: 0
                            Text("= $tables table(s) (1 table = 4.5 m²)")
                        },
                    )
                } else {
                    OutlinedTextField(
                        value = form.nbTables,
                        onValueChange = onNbTablesChanged,
                        label = { Text("Nombre de tables") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Table type dropdown
            TableTypeDropdown(
                selected = form.tableType,
                stock = stock,
                onSelected = onTableTypeChanged,
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Chairs
            OutlinedTextField(
                value = form.nbChaises,
                onValueChange = onChairsChanged,
                label = { Text("Nombre de chaises") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("Disponibles : ${stock.chaisesAvailable}") },
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    enabled = !isSaving,
                ) {
                    Text("Annuler")
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(18.dp)
                                .height(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSelectorDropdown(
    games: List<GameAllocationState>,
    selectedId: Int?,
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedGame = games.find { it.allocationId == selectedId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selectedGame?.gameTitle ?: "Sélectionner un jeu",
            onValueChange = {},
            readOnly = true,
            label = { Text("Jeu") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            if (games.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Aucun jeu disponible") },
                    onClick = { expanded = false },
                    enabled = false,
                )
            } else {
                games.forEach { game ->
                    DropdownMenuItem(
                        text = { Text(game.gameTitle) },
                        onClick = {
                            onSelected(game.allocationId)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableTypeDropdown(
    selected: String,
    stock: StockState,
    onSelected: (String) -> Unit,
) {
    val tableTypes = listOf("aucun", "standard", "grande", "mairie")
    val labels = mapOf(
        "aucun" to "Aucun",
        "standard" to "Standard (dispo: ${stock.availableForType("standard")})",
        "grande" to "Grande (dispo: ${stock.availableForType("grande")})",
        "mairie" to "Mairie (dispo: ${stock.availableForType("mairie")})",
    )
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = labels[selected] ?: selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Type de table") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            tableTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(labels[type] ?: type) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    },
                )
            }
        }
    }
}
