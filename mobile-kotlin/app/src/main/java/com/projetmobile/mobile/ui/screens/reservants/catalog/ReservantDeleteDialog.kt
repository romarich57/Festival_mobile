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
