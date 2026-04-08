package com.projetmobile.mobile.ui.screens.admin.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone

/**
 * Rôle : Modélise et rend à l'écran l'Annuaire des administrateurs et membres des permissions globales.
 *
 * Précondition : S'appuie sur un système de grille ou de listes fournies par [uiState].
 *
 * Postcondition : Construit une barre de recherche couplée aux profils triables (Pseudo, Email, Role) et notifie les sélections de détails.
 */
@Composable
internal fun AdminCatalogScreen(
    uiState: AdminCatalogUiState,
    onSearchQueryChanged: (String) -> Unit,
    onRoleFilterSelected: (String?) -> Unit,
    onEmailFilterSelected: (AdminEmailFilter) -> Unit,
    onSortOptionSelected: (AdminUserSortOption) -> Unit,
    onToggleSortOrder: () -> Unit,
    onResetFilters: () -> Unit,
    onCreateUser: () -> Unit,
    onOpenUserDetail: (Int) -> Unit,
    onRequestDelete: (AuthUser) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
    onUpdateRole: (Int, String) -> Unit,
    onDismissInfoMessage: () -> Unit,
    onDismissErrorMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.pendingDeletion != null) {
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = { Text("Supprimer l'utilisateur") },
            text = {
                Text("Confirmer la suppression de « ${uiState.pendingDeletion.login} » ?")
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("Supprimer", color = Color(0xFFB91C1C))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialog) { Text("Annuler") }
            },
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Utilisateurs",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${uiState.totalCount} utilisateur(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                    )
                }
            }
        }

        item {
            Button(
                onClick = onCreateUser,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Créer un utilisateur", fontWeight = FontWeight.SemiBold)
            }
        }

        if (uiState.errorMessage != null) {
            item {
                AuthFeedbackBanner(
                    message = uiState.errorMessage,
                    tone = AuthFeedbackTone.Error,
                )
            }
        }

        if (uiState.infoMessage != null) {
            item {
                AuthFeedbackBanner(
                    message = uiState.infoMessage,
                    tone = AuthFeedbackTone.Success,
                )
            }
        }

        item {
            AdminCatalogFilterSection(
                uiState = uiState,
                onSearchQueryChanged = onSearchQueryChanged,
                onRoleFilterSelected = onRoleFilterSelected,
                onEmailFilterSelected = onEmailFilterSelected,
                onSortOptionSelected = onSortOptionSelected,
                onToggleSortOrder = onToggleSortOrder,
                onResetFilters = onResetFilters,
            )
        }

        item {
            AdminStatsRow(uiState = uiState)
        }

        if (uiState.isLoading) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (uiState.filteredUsers.isEmpty()) {
            item {
                Text(
                    text = "Aucun utilisateur trouvé.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        } else {
            item {
                Text(
                    text = "Cliquez sur un utilisateur pour ouvrir sa fiche.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                )
            }

            items(uiState.filteredUsers, key = { it.id }) { user ->
                AdminUserCard(
                    user = user,
                    isDeleting = uiState.deletingUserId == user.id,
                    isUpdatingRole = uiState.updatingRoleForUserId == user.id,
                    onCardClick = { onOpenUserDetail(user.id) },
                    onRoleChanged = { newRole -> onUpdateRole(user.id, newRole) },
                    onDeleteClick = { onRequestDelete(user) },
                )
            }
        }
    }
}

@Composable
private fun AdminStatsRow(
    uiState: AdminCatalogUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AdminStatCard(
            value = uiState.totalCount,
            label = "Utilisateurs",
            color = Color(0xFF1D4ED8),
            background = Color(0xFFEFF6FF),
            modifier = Modifier.weight(1f),
        )
        AdminStatCard(
            value = uiState.verifiedCount,
            label = "Vérifiés",
            color = Color(0xFF16A34A),
            background = Color(0xFFF0FDF4),
            modifier = Modifier.weight(1f),
        )
        AdminStatCard(
            value = uiState.notVerifiedCount,
            label = "Non vérifiés",
            color = Color(0xFFB91C1C),
            background = Color(0xFFFEF2F2),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AdminStatCard(
    value: Int,
    label: String,
    color: Color,
    background: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B7280),
            )
        }
    }
}
