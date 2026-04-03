package com.projetmobile.mobile.ui.screens.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.games.EditorOption
import com.projetmobile.mobile.data.entity.games.GameSort
import com.projetmobile.mobile.data.entity.games.GameTypeOption
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.FestivalTextField
import com.projetmobile.mobile.ui.components.PrimaryAuthButton

@Composable
internal fun GamesCatalogHeaderCard(
    canManageGames: Boolean,
    filters: GameCatalogFilterState,
    availableTypes: List<GameTypeOption>,
    availableEditors: List<EditorOption>,
    visibleColumns: Set<GameVisibleColumn>,
    actions: GamesCatalogActions,
    windowSizeClass: GamesWindowSizeClass,
) {
    val isCompact = windowSizeClass == GamesWindowSizeClass.Compact
    var filtersExpanded by rememberSaveable(isCompact) {
        mutableStateOf(!isCompact)
    }

    AuthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("games-header-card"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (isCompact) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (canManageGames) {
                        PrimaryAuthButton(
                            text = "Nouveau jeu",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("games-create-button"),
                            onClick = actions.onCreateGame,
                        )
                    }
                    OutlinedButton(
                        onClick = { filtersExpanded = !filtersExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("games-filters-toggle"),
                    ) {
                        Text(if (filtersExpanded) "Masquer les filtres" else "Afficher les filtres")
                        Icon(
                            imageVector = if (filtersExpanded) {
                                Icons.Outlined.KeyboardArrowUp
                            } else {
                                Icons.Outlined.KeyboardArrowDown
                            },
                            contentDescription = null,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            } else {
                if (canManageGames) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PrimaryAuthButton(
                            text = "Nouveau jeu",
                            modifier = Modifier
                                .width(148.dp)
                                .height(56.dp)
                                .testTag("games-create-button"),
                            onClick = actions.onCreateGame,
                        )
                    }
                }
            }

            if (filtersExpanded) {
                FestivalTextField(
                    value = filters.title,
                    onValueChange = actions.onTitleChanged,
                    label = "Titre",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("games-title-filter"),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                        )
                    },
                )

                GamesDropdownSelector(
                    label = "Type",
                    selectedLabel = filters.selectedType ?: "Tous",
                    options = listOf("Tous" to null) + availableTypes.map { it.value to it.value },
                    onValueSelected = actions.onTypeSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("games-type-filter"),
                )

                GamesDropdownSelector(
                    label = "Éditeur",
                    selectedLabel = availableEditors.firstOrNull { it.id == filters.selectedEditorId }?.name ?: "Tous",
                    options = listOf("Tous" to null) + availableEditors.map { it.name to it.id },
                    onValueSelected = actions.onEditorSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("games-editor-filter"),
                )

                FestivalTextField(
                    value = filters.minAgeInput,
                    onValueChange = actions.onMinAgeChanged,
                    label = "Âge min.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("games-age-filter"),
                )

                GamesDropdownSelector(
                    label = "Tri",
                    selectedLabel = filters.sort.toLabel(),
                    options = GameSort.entries.map { it.toLabel() to it },
                    onValueSelected = actions.onSortSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("games-sort-filter"),
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Colonnes visibles",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                        color = Color(0xFF1B2740),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        GameVisibleColumn.entries.forEach { column ->
                            FilterChip(
                                selected = column in visibleColumns,
                                onClick = { actions.onToggleVisibleColumn(column) },
                                label = { Text(column.label) },
                                modifier = Modifier.testTag("games-visible-column-${column.name}"),
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = actions.onRefresh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Actualiser les résultats")
                }
            }
        }
    }
}

internal fun GameSort.toLabel(): String {
    return when (this) {
        GameSort.TitleAsc -> "Titre A → Z"
        GameSort.TitleDesc -> "Titre Z → A"
        GameSort.MinAgeAsc -> "Âge min croissant"
        GameSort.MinAgeDesc -> "Âge min décroissant"
        GameSort.EditorAsc -> "Éditeur A → Z"
        GameSort.EditorDesc -> "Éditeur Z → A"
    }
}
