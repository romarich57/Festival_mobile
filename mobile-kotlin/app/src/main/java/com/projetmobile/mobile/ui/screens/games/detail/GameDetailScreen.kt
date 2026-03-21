package com.projetmobile.mobile.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.projetmobile.mobile.data.entity.games.GameDetail
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone
import com.projetmobile.mobile.ui.components.PrimaryAuthButton

@Composable
internal fun GameDetailScreen(
    uiState: GameDetailUiState,
    actions: GameDetailActions,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("game-detail-root"),
    ) {
        var activeVideoReference by androidx.compose.runtime.remember {
            mutableStateOf<YoutubeVideoReference?>(null)
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .testTag("game-detail-list")
                    .fillMaxWidth()
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.errorMessage != null) {
                    item {
                        AuthFeedbackBanner(
                            message = uiState.errorMessage,
                            tone = AuthFeedbackTone.Error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("game-detail-error-banner"),
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = actions.onDismissErrorMessage) {
                                Text("Fermer")
                            }
                            OutlinedButton(onClick = actions.onRetry) {
                                Text("Réessayer")
                            }
                        }
                    }
                }

                when {
                    uiState.isLoading && uiState.game == null -> item { LoadingGamesCard() }
                    uiState.game != null -> {
                        val game = uiState.game
                        item {
                            GameDetailHeroCard(
                                game = game,
                                canManageGames = uiState.canManageGames,
                                onEditGame = actions.onEditGame,
                            )
                        }
                        item {
                            GameDetailInformationCard(game = game)
                        }
                        item {
                            AuthCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    GamesRulesVideoPreview(
                                        rulesVideoUrl = game.rulesVideoUrl.orEmpty(),
                                        title = "Vidéo des règles",
                                        onPlayVideo = { videoReference -> activeVideoReference = videoReference },
                                    )
                                }
                            }
                        }
                        if (game.mechanisms.isNotEmpty()) {
                            item {
                                AuthCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Text(
                                            text = "Mécanismes",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color(0xFF18233A),
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
                            }
                        }
                    }

                    else -> item { EmptyGamesCard() }
                }
            }
        }

        activeVideoReference?.let { videoReference ->
            GamesYoutubePlayerDialog(
                videoReference = videoReference,
                onDismiss = { activeVideoReference = null },
            )
        }
    }
}

@Composable
private fun GameDetailHeroCard(
    game: GameDetail,
    canManageGames: Boolean,
    onEditGame: (Int) -> Unit,
) {
    AuthCard(modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            val useRowLayout = maxWidth >= 560.dp
            if (useRowLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    GameDetailCover(
                        game = game,
                        modifier = Modifier.size(220.dp),
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        GameDetailHeroContent(
                            game = game,
                            canManageGames = canManageGames,
                            onEditGame = onEditGame,
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    GameDetailCover(
                        game = game,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                    )
                    GameDetailHeroContent(
                        game = game,
                        canManageGames = canManageGames,
                        onEditGame = onEditGame,
                    )
                }
            }
        }
    }
}

@Composable
private fun GameDetailCover(
    game: GameDetail,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0xFFEAF0FB),
                shape = RoundedCornerShape(28.dp),
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
                modifier = Modifier.size(44.dp),
            )
        }
    }
}

@Composable
private fun GameDetailHeroContent(
    game: GameDetail,
    canManageGames: Boolean,
    onEditGame: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = game.title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF18233A),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GamesMetaChip(game.type)
            game.editorName?.let { GamesMetaChip(it) }
            GamesMetaChip("${game.minAge}+")
            if (game.minPlayers != null && game.maxPlayers != null) {
                GamesMetaChip("${game.minPlayers} - ${game.maxPlayers} joueurs")
            }
            game.durationMinutes?.let { GamesMetaChip("${it} min") }
            game.theme?.takeIf { it.isNotBlank() }?.let { GamesMetaChip(it) }
            if (game.prototype) {
                GamesMetaChip("Prototype")
            }
        }
        if (canManageGames) {
            PrimaryAuthButton(
                text = "Modifier",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("game-detail-edit-button"),
                onClick = { onEditGame(game.id) },
            )
        }
    }
}

@Composable
private fun GameDetailInformationCard(game: GameDetail) {
    AuthCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            GameDetailField(label = "Auteurs", value = game.authors)
            game.description?.takeIf { it.isNotBlank() }?.let { description ->
                GameDetailField(label = "Description", value = description)
            }
        }
    }
}

@Composable
private fun GameDetailField(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF5D6981),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF18233A),
        )
    }
}
