package com.projetmobile.mobile.ui.screens.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.ui.components.AuthCard

@Composable
internal fun LoadingGamesCard() {
    AuthCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
internal fun EmptyGamesCard() {
    AuthCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Aucun jeu trouvé",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF18233A),
            )
            Text(
                text = "Ajustez les filtres ou créez un nouveau jeu pour enrichir le catalogue.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
internal fun GamesDeleteDialog(
    pendingDeletion: GameListItem,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissDeleteDialog,
        title = { Text("Supprimer le jeu") },
        text = {
            Text("Voulez-vous vraiment supprimer ${pendingDeletion.title} ?")
        },
        confirmButton = {
            OutlinedButton(onClick = onConfirmDelete) {
                Text("Supprimer", color = Color(0xFFB42318))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissDeleteDialog) {
                Text("Annuler")
            }
        },
    )
}
