/**
 * Rôle : Carte visuelle affichant la configuration et le contenu d'un espace géographique ("ZonePlan") lors d'un festival.
 * Elle détaille ses statistiques de remplissage et liste les jeux/tables alloués par les différents exposants au sein de cette zone.
 * Précondition : L'ensemble des états locaux (`ZonePlanZoneState`, réservation courante) est passé pour un affichage interactif.
 * Postcondition : Interface avec un header descriptif, la liste exhaustive des placements, et des boutons ajout/suppression.
 */
package com.projetmobile.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.PlacementDisplayItem
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.ZonePlanZoneState

@Composable
fun ZonePlanCard(
    zone: ZonePlanZoneState,
    currentReservationId: Int,
    isSaving: Boolean,
    onAddPlacement: () -> Unit,
    onDeletePlacement: (Int) -> Unit,
    onRemoveGame: (Int) -> Unit,
    onDeleteZone: () -> Unit,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$tablesRestantes/${zone.totalTables} tables",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (tablesRestantes <= 0) MaterialTheme.colorScheme.error else Color.Unspecified,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDeleteZone, enabled = !isSaving) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer la zone de plan",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            if (!zone.hasReservationInLinkedZone) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠ Pas de réservation dans la zone tarifaire liée",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            // All placements (simple + games) from all reservants
            if (zone.placements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Placements :", style = MaterialTheme.typography.labelMedium)
                zone.placements.forEach { placement ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = formatPlacementLine(placement),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        // Show delete button only for current reservation's placements
                        if (placement.reservationId == currentReservationId) {
                            if (placement.isGamePlacement) {
                                IconButton(
                                    onClick = { placement.allocationId?.let { onRemoveGame(it) } },
                                    enabled = !isSaving,
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Retirer le jeu",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = { onDeletePlacement(placement.id) },
                                    enabled = !isSaving,
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Supprimer",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
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

/**
 * Rôle : Formate la ligne de placement en suivant la règle concaténée :
 * [Nom du réservant] : [Nom du jeu] - [X] table(s) - [Type de table] - [Y] chaises.
 * Masque implicitement les mentions de jeu "aucun" ou de type de table "aucun".
 * Précondition : L'objet `PlacementDisplayItem` fournit les statistiques brutes non formatées.
 * Postcondition : Retourne la chaîne textuelle prête à être imprimée dans l'interface utilisateur.
 */
fun formatPlacementLine(placement: PlacementDisplayItem): String {
    val parts = mutableListOf<String>()

    // Game title (only if not null/blank/aucun)
    val gameTitle = placement.gameTitle
    if (!gameTitle.isNullOrBlank() && gameTitle.lowercase() != "aucun") {
        parts.add(gameTitle)
    }

    // Tables
    val tableLabel = if (placement.nbTables <= 1) "table" else "tables"
    parts.add("${placement.nbTables} $tableLabel")

    // Table type (only if not "aucun")
    if (placement.tailleTable.lowercase() != "aucun" && placement.tailleTable.isNotBlank()) {
        val typeLabel = when (placement.tailleTable.lowercase()) {
            "standard" -> "Table Standard"
            "grande" -> "Table Grande"
            "mairie" -> "Table Mairie"
            else -> placement.tailleTable
        }
        parts.add(typeLabel)
    }

    // Chairs
    val chairLabel = if (placement.nbChaises <= 1) "chaise" else "chaises"
    parts.add("${placement.nbChaises} $chairLabel")

    return "${placement.reservantName} : ${parts.joinToString(" - ")}"
}
