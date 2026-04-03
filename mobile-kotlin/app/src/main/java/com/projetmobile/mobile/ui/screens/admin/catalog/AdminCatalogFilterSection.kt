package com.projetmobile.mobile.ui.screens.admin.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.FestivalTextField
import com.projetmobile.mobile.ui.screens.admin.shared.ADMIN_AVAILABLE_ROLES
import com.projetmobile.mobile.ui.screens.admin.shared.roleDisplayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdminCatalogFilterSection(
    uiState: AdminCatalogUiState,
    onSearchQueryChanged: (String) -> Unit,
    onRoleFilterSelected: (String?) -> Unit,
    onEmailFilterSelected: (AdminEmailFilter) -> Unit,
    onSortOptionSelected: (AdminUserSortOption) -> Unit,
    onToggleSortOrder: () -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var emailDropdownExpanded by remember { mutableStateOf(false) }
    var sortDropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Recherche", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(4.dp))
            FestivalTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged,
                label = "Nom, email, identifiant",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Role", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = roleDropdownExpanded,
                onExpandedChange = { roleDropdownExpanded = it },
            ) {
                OutlinedTextField(
                    value = uiState.roleFilter?.let { roleDisplayName(it) } ?: "Tous les roles",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp),
                )
                ExposedDropdownMenu(
                    expanded = roleDropdownExpanded,
                    onDismissRequest = { roleDropdownExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Tous les roles") },
                        onClick = { roleDropdownExpanded = false; onRoleFilterSelected(null) },
                    )
                    ADMIN_AVAILABLE_ROLES.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(roleDisplayName(role)) },
                            onClick = { roleDropdownExpanded = false; onRoleFilterSelected(role) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Statut email", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = emailDropdownExpanded,
                onExpandedChange = { emailDropdownExpanded = it },
            ) {
                OutlinedTextField(
                    value = uiState.emailFilter.label,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = emailDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp),
                )
                ExposedDropdownMenu(
                    expanded = emailDropdownExpanded,
                    onDismissRequest = { emailDropdownExpanded = false },
                ) {
                    AdminEmailFilter.entries.forEach { filter ->
                        DropdownMenuItem(
                            text = { Text(filter.label) },
                            onClick = { emailDropdownExpanded = false; onEmailFilterSelected(filter) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Tri", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = sortDropdownExpanded,
                onExpandedChange = { sortDropdownExpanded = it },
            ) {
                OutlinedTextField(
                    value = uiState.sortOption.label,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp),
                )
                ExposedDropdownMenu(
                    expanded = sortDropdownExpanded,
                    onDismissRequest = { sortDropdownExpanded = false },
                ) {
                    AdminUserSortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = { sortDropdownExpanded = false; onSortOptionSelected(option) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onToggleSortOrder,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = if (uiState.sortAscending) "Ascendant" else "Descendant",
                        color = Color(0xFF111827),
                    )
                }
                Button(
                    onClick = onResetFilters,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Réinitialiser", color = Color(0xFF111827))
                }
            }
        }
    }
}
