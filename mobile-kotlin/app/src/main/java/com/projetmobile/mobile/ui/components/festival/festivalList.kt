package com.projetmobile.mobile.ui.components.festival

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.festival.FestivalSummary

/**
 * Liste des festivals.
 *
 * Traduction du FestivalListComponent Angular :
 *  - Ne connaît PAS le ViewModel (pas d'inject ici).
 *  - Reçoit les données et les callbacks depuis FestivalScreen (le parent).
 *  - `festivals`         = this._festivalService.festivals (signal) → ici StateFlow lu par Screen
 *  - `currentFestivalId` = this._festivalState.currentFestival()?.id → calculé par Screen
 *  - `canDelete`         = this._authService.isSuperOrganizer
 *  - `onSelect`          = select output de FestivalCard remonté
 *  - `onDeleteRequest`   = requestDeleteFestival()
 *  - `onRetry`           = rechargement si erreur
 *
 * Principe S : itérer sur la liste + déléguer à FestivalCard. Rien d'autre.
 */
@Composable
fun FestivalList(
    festivals: List<FestivalSummary>,
    currentFestivalId: Int?,                          // pour calculer isSelected dans chaque card
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    canDelete: Boolean = false,
    onSelect: (id: Int?) -> Unit = {},                // remonte vers FestivalScreen → ViewModel
    onDeleteRequest: (id: Int) -> Unit = {},          // remonte vers FestivalScreen → ViewModel
    onRetry: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize()) {

        // ── En-tête ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Accueil",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Liste des festivals",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (errorMessage != null) {
                IconButton(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, contentDescription = "Réessayer")
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(8.dp))

        // ── Corps : loading / erreur / liste ──────────────────────────────────
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Erreur de chargement",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onRetry) { Text("Réessayer") }
                    }
                }
            }

            festivals.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Aucun festival disponible",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(festivals, key = { it.id }) { festival ->
                        // isSelected calculé ici — équivalent computed() Angular dans la Card
                        FestivalCard(
                            festival = festival,
                            isSelected = currentFestivalId == festival.id,
                            canDelete = canDelete,
                            onSelect = onSelect,
                            onDelete = onDeleteRequest,
                        )
                    }
                }
            }
        }
    }
}