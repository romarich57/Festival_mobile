package com.projetmobile.mobile.ui.screens.festival

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.festival.FestivalSummary
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.AuthLinkButton

@Composable
fun FestivalScreen(
    uiState: FestivalUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> {
            AuthCard(modifier = modifier) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        uiState.errorMessage != null -> {
            AuthCard(modifier = modifier) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
                    AuthLinkButton(text = "Réessayer", onClick = onRetry)
                }
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.festivals) { festival ->
                    FestivalItemCard(festival)
                }
            }
        }
    }
}

@Composable
private fun FestivalItemCard(festival: FestivalSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = festival.name,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Du ${festival.startDate} au ${festival.endDate}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Tables standard: ${festival.stockTablesStandard} • Chaises: ${festival.stockChaises}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
