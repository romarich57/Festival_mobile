/**
 * Rôle : Composant visuel (Card) présentant le formulaire d'ajout de zone.
 *
 * Précondition : Nécessite l'état du formulaire, et un callback pour écouter les actions.
 *
 * Postcondition : Interfère avec l'utilisateur pour ajouter visuellement une nouvelle configuration de zone.
 */
package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.addzone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Rôle : Exécute l'action add zone formulaire carte du module la zone plan des réservations.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
fun AddZoneFormCard(
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),) {
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
                supportingText = if (form.maxTables < Int.MAX_VALUE) {
                    { Text("Disponibles : ${form.maxTables}") }
                } else null,
                isError = form.nbTables.toIntOrNull()?.let { it > form.maxTables } == true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text("Annuler")
                }
                Button(onClick = onSave, modifier = Modifier.weight(1f), enabled = !isSaving) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Créer")
                    }
                }
            }
        }
    }
}
