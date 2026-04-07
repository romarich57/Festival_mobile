package com.projetmobile.mobile.ui.components.festival

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.festival.FestivalSummary

/**
 * Liste des festivals.
 *
 * Traduction du FestivalListComponent Angular :
 *  - Ne connaît PAS le ViewModel.
 *  - Reçoit les données et callbacks depuis FestivalScreen.
 *  - `canAdd`     = droit de création selon le rôle courant
 *  - `onAddClick` = navigue vers FestivalFormScreen via FestivalScreen
 *
 * Principe S : itérer sur la liste + déléguer à FestivalCard. Rien d'autre.
 */
@Composable
fun FestivalList(
    festivals: List<FestivalSummary>,
    currentFestivalId: Int?,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    canDelete: Boolean = false,
    canAdd: Boolean = false,
    onSelect: (id: Int?) -> Unit = {},
    onDeleteRequest: (id: Int) -> Unit = {},
    onAddClick: () -> Unit = {},            // → FestivalFormScreen
    onRetry: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── En-tête ───────────────────────────────────────────────────────
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

            // ── Corps : loading / erreur / liste ──────────────────────────────
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
                    // Padding bottom pour ne pas masquer le dernier item avec le FAB
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 8.dp,
                            bottom = if (canAdd) 88.dp else 8.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(festivals, key = { it.id }) { festival ->
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

        // ── FAB + en bas à droite ─────────────────────────────────────────────
        // Visible seulement si l'utilisateur a le droit de créer un festival.
        if (canAdd && !isLoading && errorMessage == null) {
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter un festival",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
