package com.projetmobile.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity

@Composable
fun ReservationCard(
    reservation: ReservationDashboardRowEntity,
    onViewDetailsClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = reservation.reservantName, style = MaterialTheme.typography.titleMedium)
            Text(text = "Type: ${reservation.reservantType}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Badge { Text(reservation.workflowState) } //Badge c'est un composant de Material3 pour afficher des statuts ou des étiquettes

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { onViewDetailsClick(reservation.id) }) {
                Text("Voir Détails")
            }
        }
    }

    fun onViewDetailsClick(reservationId: Int) {
        // Ici tu peux faire la navigation vers l'écran de détails
        // Par exemple, si tu utilises NavController, tu pourrais faire :
        // navController.navigate("reservation_details/$reservationId") ATTENTION C'EST PAS NAV3
    }
}