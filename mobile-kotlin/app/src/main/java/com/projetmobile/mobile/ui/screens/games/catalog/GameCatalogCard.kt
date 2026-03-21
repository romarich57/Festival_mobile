package com.projetmobile.mobile.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.PrimaryAuthButton

@Composable
internal fun GameCatalogCard(
    game: GameListItem,
    visibleColumns: Set<GameVisibleColumn>,
    canManageGames: Boolean,
    isDeleting: Boolean,
    onOpenGameDetails: (Int) -> Unit,
    onEditGame: (Int) -> Unit,
    onRequestDelete: (GameListItem) -> Unit,
) {
    AuthCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenGameDetails(game.id) }
            .testTag("game-card-${game.id}"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            color = Color(0xFFEAF0FB),
                            shape = RoundedCornerShape(20.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    val imageModel = game.imageUrl?.let(::toAbsoluteBackendUrl)
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = game.title,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.SportsEsports,
                            contentDescription = null,
                            tint = Color(0xFF5D6981),
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = game.title,
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        color = Color(0xFF18233A),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (GameVisibleColumn.Type in visibleColumns) {
                            GamesMetaChip(game.type)
                        }
                        if (GameVisibleColumn.Editor in visibleColumns && game.editorName != null) {
                            GamesMetaChip(game.editorName)
                        }
                        if (GameVisibleColumn.Age in visibleColumns) {
                            GamesMetaChip("${game.minAge}+")
                        }
                        if (GameVisibleColumn.Players in visibleColumns &&
                            game.minPlayers != null &&
                            game.maxPlayers != null
                        ) {
                            GamesMetaChip("${game.minPlayers} - ${game.maxPlayers}")
                        }
                    }
                }
            }

            if (GameVisibleColumn.Authors in visibleColumns) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Auteurs",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Color(0xFF5D6981),
                    )
                    Text(
                        text = game.authors,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = Color(0xFF18233A),
                    )
                }
            }

            if (GameVisibleColumn.Mechanisms in visibleColumns && game.mechanisms.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Mécanismes",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Color(0xFF5D6981),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        game.mechanisms.forEach { mechanism ->
                            GamesMetaChip(mechanism.name)
                        }
                    }
                }
            }

            if (canManageGames) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val useHorizontalActions = maxWidth >= 360.dp
                    if (useHorizontalActions) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            PrimaryAuthButton(
                                text = "Modifier",
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("game-edit-${game.id}"),
                                onClick = { onEditGame(game.id) },
                            )
                            OutlinedButton(
                                onClick = { onRequestDelete(game) },
                                enabled = !isDeleting,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("game-delete-${game.id}"),
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFB42318),
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "Supprimer",
                                    color = Color(0xFFB42318),
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PrimaryAuthButton(
                                text = "Modifier",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("game-edit-${game.id}"),
                                onClick = { onEditGame(game.id) },
                            )
                            OutlinedButton(
                                onClick = { onRequestDelete(game) },
                                enabled = !isDeleting,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("game-delete-${game.id}"),
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFB42318),
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "Supprimer",
                                    color = Color(0xFFB42318),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
