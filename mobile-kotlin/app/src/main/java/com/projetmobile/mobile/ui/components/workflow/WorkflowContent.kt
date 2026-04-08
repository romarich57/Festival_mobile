/**
 * Rôle : Affiche et gère le formulaire du Cycle de Vie (Workflow) d'une réservation (ex: "Contact_pris", "Facture_payee").
 * Fournit une série de cases à cocher logistiques (jeux reçus, présents etc.) et un menu déroulant d'états.
 * Précondition : Appelé dans l'écran de détail de réservation avec l'actuel `WorkflowDto` en paramètre.
 * Postcondition : Déclenche l'événement `onSave` avec un `WorkflowUpdatePayload` mis à jour selon les clics.
 */
package com.projetmobile.mobile.ui.components.workflow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.remote.reservation.WorkflowDto
import com.projetmobile.mobile.data.remote.reservation.WorkflowUpdatePayload

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Rôle : Exécute l'action workflow content du module workflow.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
fun WorkflowContent(
    workflow: WorkflowDto,
    isSaving: Boolean,
    onSave: (WorkflowUpdatePayload) -> Unit
) {
    // États locaux pour le formulaire
    var selectedState by remember(workflow) { mutableStateOf(workflow.state) }
    var listDemande by remember { mutableStateOf(workflow.liste_jeux_demandee) }
    var listObtenue by remember { mutableStateOf(workflow.liste_jeux_obtenue) }
    var jeuxRecus by remember { mutableStateOf(workflow.jeux_recus) }
    var presenteraJeux by remember { mutableStateOf(workflow.presentera_jeux) }

    var expanded by remember { mutableStateOf(false) }
    val states = listOf(
        "Pas_de_contact", "Contact_pris", "Discussion_en_cours",
        "Reservation_confirmee", "Facture", "Facture_payee"
    )

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("État de la réservation", style = MaterialTheme.typography.titleMedium)

        // MENU DÉROULANT (ÉTAT)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedState.replace("_", " "),
                onValueChange = {},
                readOnly = true,
                isError = false,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth() ,
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                states.forEach { state ->
                    DropdownMenuItem(
                        text = { Text(state.replace("_", " ")) },
                        onClick = {
                            selectedState = state
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Actions logistiques", style = MaterialTheme.typography.titleMedium)

        // 2. LES CASES À COCHER (CHECKBOXES)
        WorkflowCheckbox(label = "Liste de jeux demandée", checked = listDemande) { listDemande = it }
        WorkflowCheckbox(label = "Liste de jeux obtenue", checked = listObtenue) { listObtenue = it }
        WorkflowCheckbox(label = "Jeux reçus", checked = jeuxRecus) { jeuxRecus = it }
        WorkflowCheckbox(label = "Présentera des jeux au festival", checked = presenteraJeux) { presenteraJeux = it }

        Spacer(Modifier.height(32.dp))

        // 3. BOUTON ENREGISTRER
        Button(
            onClick = {
                onSave(WorkflowUpdatePayload(selectedState, listDemande, listObtenue, jeuxRecus, presenteraJeux))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            if (isSaving) {
                Text("Enregistrement...")
            } else {
                Text("Enregistrer les modifications")
            }
        }
    }
}

/**
 * Rôle : Extrait la ligne d'interface visuelle pour afficher côte à côte une case à cocher (`Checkbox`) et son étiquette textuelle (`label`).
 * Permet qu'un clic n'importe où sur la rangée coche/décoche la ligne.
 * Précondition : Reçoit l'état courant (checked) et le callback d'inversion.
 * Postcondition : Affiche la Row cliquable Material 3.
 */
@Composable
fun WorkflowCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(vertical = 8.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}
