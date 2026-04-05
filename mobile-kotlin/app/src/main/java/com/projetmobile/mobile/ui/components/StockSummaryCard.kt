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
