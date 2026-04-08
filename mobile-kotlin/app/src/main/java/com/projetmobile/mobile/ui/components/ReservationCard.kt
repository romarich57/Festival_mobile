/**
 * Rôle : Composant visuel affichant un résumé individuel pour une réservation listée dans un dashboard.
 * Présente le nom du réservant, son type, et l'état d'avancement de la réservation, ainsi qu'un bouton de suppression.
 * Précondition : La classe `ReservationDashboardRowEntity` d'entrée doit posséder des données non manquantes.
 * Postcondition : Affiche une carte interactive naviguant vers les détails au clic principal, ou demandant une suppression.
 */
package com.projetmobile.mobile.ui.components

import android.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity

@Composable
fun ReservationCard(
    reservation: ReservationDashboardRowEntity,
    onViewDetailsClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { onViewDetailsClick(reservation.id) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface, // Couleur standard du thème
            contentColor = MaterialTheme.colorScheme.onSurface  // Couleur du texte adaptée
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Nom du réservant : ${reservation.reservantName}", style = MaterialTheme.typography.titleMedium)
                Text(text = "Type de réservant : ${reservation.reservantType}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Statut de la réservation : ${reservation.workflowState}", style = MaterialTheme.typography.bodySmall)
            }

            // On ne laisse que la poubelle ici
            IconButton(onClick = { onDeleteClick(reservation.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
            }
        }
    }



}