package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ZonePlanTab(
    reservationId: Int,
    viewModel: ZonePlanViewModel,
) {
    LaunchedEffect(reservationId) {
        viewModel.loadContext(reservationId)
    }

    when (val state = viewModel.uiState) {
        ZonePlanUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        is ZonePlanUiState.Error -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(state.message)
        }

        is ZonePlanUiState.Success -> ZonePlanContent(state, viewModel)
    }
}

@Composable
private fun ZonePlanContent(
    state: ZonePlanUiState.Success,
    viewModel: ZonePlanViewModel,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Title
        item {
            Text(
                text = "Zones de plan",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        // Stock summary
        item {
            StockSummaryCard(state.stock)
        }

        // Success/error message
        if (!state.userMessage.isNullOrBlank()) {
            item {
                Text(
                    text = state.userMessage,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                LaunchedEffect(state.userMessage) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearMessage()
                }
            }
        }

        // Zones list
        items(state.zones, key = { it.id }) { zone ->
            ZonePlanCard(
                zone = zone,
                games = state.games.filter { it.zonePlanId == zone.id },
                isSaving = state.isSaving,
                onAddPlacement = { viewModel.openPlacementForm(zone.id) },
                onDeleteSimple = { viewModel.deleteSimpleAllocation(zone.id) },
                onRemoveGame = { allocationId -> viewModel.removeGameFromZone(allocationId) },
            )
        }

        item {
            Button(
                onClick = { viewModel.openAddZoneForm() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ajouter une zone de plan")
            }
        }

        if (state.showAddZoneForm) {
            item {
                AddZoneFormCard(
                    form = state.addZoneForm,
                    zonesTarifaires = state.zonesTarifaires,
                    isSaving = state.isSaving,
                    onNameChanged = viewModel::onAddZoneNameChanged,
                    onZoneTarifaireSelected = viewModel::onAddZoneZoneTarifaireSelected,
                    onNbTablesChanged = viewModel::onAddZoneNbTablesChanged,
                    onSave = viewModel::saveAddZone,
                    onCancel = viewModel::closeAddZoneForm,
                )
            }
        }

        // Placement form (if open)
        if (state.showPlacementForm) {
            item {
                PlacementFormCard(
                    form = state.placementForm,
                    unplacedGames = state.games.filter { it.zonePlanId == null },
                    stock = state.stock,
                    isSaving = state.isSaving,
                    onWithGameChanged = viewModel::onWithGameChanged,
                    onGameSelected = viewModel::onGameSelected,
                    onPlacePerCopyChanged = viewModel::onPlacePerCopyChanged,
                    onNbCopiesChanged = viewModel::onNbCopiesChanged,
                    onTableTypeChanged = viewModel::onTableTypeChanged,
                    onChairsChanged = viewModel::onChairsChanged,
                    onNbTablesChanged = viewModel::onNbTablesChanged,
                    onUseM2Changed = viewModel::onUseM2Changed,
                    onM2ValueChanged = viewModel::onM2ValueChanged,
                    onSave = viewModel::savePlacement,
                    onCancel = viewModel::closePlacementForm,
                )
            }
        }
    }
}

@Composable
private fun StockSummaryCard(stock: StockState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Stock du festival", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StockItem("Standard", stock.tablesStandard - stock.tablesStandardOccupied, stock.tablesStandard)
                StockItem("Grande", stock.tablesGrande - stock.tablesGrandeOccupied, stock.tablesGrande)
                StockItem("Mairie", stock.tablesMairie - stock.tablesMairieOccupied, stock.tablesMairie)
                StockItem("Chaises", stock.chaisesAvailable, stock.chaisesTotal)
            }
        }
    }
}

@Composable
private fun StockItem(label: String, available: Int, total: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(
            text = "$available/$total",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (available <= 0) MaterialTheme.colorScheme.error else Color.Unspecified,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddZoneFormCard(
    form: AddZoneFormState,
    zonesTarifaires: List<ZoneTarifaireOptionState>,
    isSaving: Boolean,
    onNameChanged: (String) -> Unit,
    onZoneTarifaireSelected: (Int) -> Unit,
    onNbTablesChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedZt = zonesTarifaires.find { it.id == form.selectedZoneTarifaireId }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nouvelle zone de plan", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = form.name,
                onValueChange = onNameChanged,
                label = { Text("Nom") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedZt?.name ?: "Zone tarifaire",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Zone tarifaire") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    zonesTarifaires.forEach { zt ->
                        DropdownMenuItem(
                            text = { Text(zt.name) },
                            onClick = { onZoneTarifaireSelected(zt.id); expanded = false },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = form.nbTables,
                onValueChange = onNbTablesChanged,
                label = { Text("Nombre de tables") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = selectedZt?.let { { Text("Max : ${it.nbTables}") } },
                isError = form.nbTables.toIntOrNull()?.let { it > form.maxTables } == true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onCancel, modifier = Modifier.weight(1f), enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    Text("Annuler")
                }
                Button(onClick = onSave, modifier = Modifier.weight(1f), enabled = !isSaving) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("Créer")
                }
            }
        }
    }
}


@Composable
private fun ZonePlanCard(
    zone: ZonePlanZoneState,
    games: List<GameAllocationState>,
    isSaving: Boolean,
    onAddPlacement: () -> Unit,
    onDeleteSimple: () -> Unit,
    onRemoveGame: (Int) -> Unit,
) {
    val tablesRestantes = zone.totalTables - zone.allocatedTables

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = zone.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Zone tarifaire : ${zone.zoneTarifaireName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
                }
                Text(
                    text = "$tablesRestantes/${zone.totalTables} tables",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (tablesRestantes <= 0) MaterialTheme.colorScheme.error else Color.Unspecified,
                )
            }

            if (!zone.hasReservationInLinkedZone) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠ Pas de réservation dans la zone tarifaire liée",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            // My simple allocation
            if (zone.mySimpleAllocationTables > 0 || zone.mySimpleAllocationChaises > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Mon placement : ${zone.mySimpleAllocationTables} tables, ${zone.mySimpleAllocationChaises} chaises",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    IconButton(onClick = onDeleteSimple, enabled = !isSaving) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Games placed in this zone
            if (games.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Jeux placés :", style = MaterialTheme.typography.labelMedium)
                games.forEach { game ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = game.gameTitle, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${game.nbExemplaires.toInt()}x • ${game.nbTablesOccupees} table/ex • ${game.tailleTableRequise} • ${game.nbChaises} chaises",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                            )
                        }
                        IconButton(onClick = { onRemoveGame(game.allocationId) }, enabled = !isSaving) {
                            Icon(Icons.Default.Delete, contentDescription = "Retirer", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Add placement button
            if (zone.hasReservationInLinkedZone && tablesRestantes > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAddPlacement,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ajouter un placement")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlacementFormCard(
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
private fun GameSelectorDropdown(
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
private fun TableTypeDropdown(
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
