/**
 * Rôle : Affiche la confirmation de suppression d'un réservant et ses dépendances associées.
 * Ce composant guide l'utilisateur avant une action potentiellement destructrice.
 * Précondition : L'item ciblé et le résumé de suppression doivent être disponibles pour contextualiser l'action.
 * Postcondition : L'utilisateur peut confirmer ou annuler la suppression avec une vue claire des conséquences.
 */
package com.projetmobile.mobile.ui.screens.reservants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.reservants.ReservantListItem

@Composable
/**
 * Rôle : Présente la boîte de dialogue de confirmation avant suppression d'un réservant.
 * Précondition : `summary` doit refléter l'état du chargement des dépendances liées à la suppression.
 * Postcondition : L'utilisateur peut confirmer la suppression ou revenir en arrière sans ambiguïté.
 */
internal fun ReservantDeleteDialog(
    reservant: ReservantListItem,
    summary: ReservantDeleteSummaryDialogModel?,
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer ${reservant.name} ?") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (summary == null) {
                    CircularProgressIndicator()
                } else {
                    Text("Cette suppression entraînera aussi la suppression des éléments liés.")
                    Text("Contacts: ${summary.contactsCount}")
                    Text("Workflows: ${summary.workflowsCount}")
                    Text("Réservations: ${summary.reservationsCount}")
                    summary.highlights.forEach { line ->
                        Text(line)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = summary != null && !isDeleting,
            ) {
                Text(if (isDeleting) "Suppression…" else "Supprimer")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isDeleting,
            ) {
                Text("Annuler")
            }
        },
    )
}
