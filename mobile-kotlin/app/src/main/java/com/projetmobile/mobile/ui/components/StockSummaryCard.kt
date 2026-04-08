/**
 * Rôle : Affiche sous forme de carte un résumé des stocks restants et potentiellement en pénurie pour un festival.
 * Affiche la répartition des tables (Standards, Grandes, Mairies) et des chaises globalement réservées.
 * Précondition : Un état `StockState` à jour doit être fourni en paramètre contenant les totaux et occupations.
 * Postcondition : Affiche les jauges textuelles, en marquant en rouge les valeurs dépassant la capacité limite.
 */
package com.projetmobile.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.StockState

@Composable
fun StockSummaryCard(stock: StockState) {
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

/**
 * Rôle : Affiche l'état partiel d'un item du stock (disponible/total).
 * Met en évidence textuellement toute jauge de ressource négative (sur-réservation).
 * Précondition : Appelé à l'intérieur du conteneur parent `StockSummaryCard`.
 * Postcondition : Affiche un couple Clé/Valeur textuel stylisé.
 */
@Composable
fun StockItem(label: String, available: Int, total: Int) {
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
