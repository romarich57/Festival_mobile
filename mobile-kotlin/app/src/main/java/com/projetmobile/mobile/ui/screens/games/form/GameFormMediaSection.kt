/**
 * Rôle : Regroupe les médias (images et affiches) dans l'écran d'édition d'un jeu.
 *
 * Précondition : Un gestionnaire de Pick Media URI et réseau.
 *
 * Postcondition : Affiche, permet la suppression ou modifie l'affiche principale du jeu de société.
 */
package com.projetmobile.mobile.ui.screens.games

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.FestivalTextField

@Composable
/**
 * Rôle : Exécute l'action jeu formulaire media section du module les jeux formulaire.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
internal fun GameFormMediaSection(
    imageSourceMode: GameImageSourceMode,
    imageUrl: String,
    localImageSelection: LocalGameImageSelection?,
    rulesVideoUrl: String,
    onImageSourceModeChanged: (GameImageSourceMode) -> Unit,
    onImageUrlChanged: (String) -> Unit,
    onRulesVideoUrlChanged: (String) -> Unit,
    onPickFile: () -> Unit,
) {
    GameImageSourceSection(
        imageSourceMode = imageSourceMode,
        imageUrl = imageUrl,
        localImageSelection = localImageSelection,
        onImageSourceModeChanged = onImageSourceModeChanged,
        onImageUrlChanged = onImageUrlChanged,
        onPickFile = onPickFile,
    )

    GameRulesVideoSection(
        rulesVideoUrl = rulesVideoUrl,
        onRulesVideoUrlChanged = onRulesVideoUrlChanged,
    )
}

@Composable
/**
 * Rôle : Exécute l'action jeu image source section du module les jeux formulaire.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
private fun GameImageSourceSection(
    imageSourceMode: GameImageSourceMode,
    imageUrl: String,
    localImageSelection: LocalGameImageSelection?,
    onImageSourceModeChanged: (GameImageSourceMode) -> Unit,
    onImageUrlChanged: (String) -> Unit,
    onPickFile: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Source de l'image",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
            color = Color(0xFF1B2740),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = imageSourceMode == GameImageSourceMode.Url,
                onClick = { onImageSourceModeChanged(GameImageSourceMode.Url) },
                label = { Text("URL") },
            )
            FilterChip(
                selected = imageSourceMode == GameImageSourceMode.File,
                onClick = { onImageSourceModeChanged(GameImageSourceMode.File) },
                label = { Text("Fichier") },
            )
        }
        when (imageSourceMode) {
            GameImageSourceMode.Url -> FestivalTextField(
                value = imageUrl,
                onValueChange = onImageUrlChanged,
                label = "Image (URL)",
                modifier = Modifier.fillMaxWidth(),
            )

            GameImageSourceMode.File -> {
                OutlinedButton(onClick = onPickFile) {
                    Text(if (localImageSelection == null) "Choisir un fichier" else "Changer le fichier")
                }
                if (localImageSelection != null) {
                    Text(
                        text = localImageSelection.fileName,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
/**
 * Rôle : Exécute l'action jeu rules video section du module les jeux formulaire.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
private fun GameRulesVideoSection(
    rulesVideoUrl: String,
    onRulesVideoUrlChanged: (String) -> Unit,
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FestivalTextField(
            value = rulesVideoUrl,
            onValueChange = onRulesVideoUrlChanged,
            label = "Vidéo des règles (URL)",
            modifier = Modifier.fillMaxWidth(),
        )
        GamesRulesVideoPreview(
            rulesVideoUrl = rulesVideoUrl,
            title = "Aperçu de la vidéo des règles",
            onPlayVideo = { videoReference -> openVideoExternally(context, videoReference) },
        )
    }
}

/**
 * Rôle : Charge jeu image selection.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
internal fun loadGameImageSelection(context: Context, uri: Uri): GameImageSelectionPayload? {
    val contentResolver = context.contentResolver
    val fileName = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                cursor.getString(index)
            } else {
                null
            }
        }
        ?: "game-image"
    val mimeType = contentResolver.getType(uri) ?: "image/*"
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null

    return GameImageSelectionPayload(
        fileName = fileName,
        mimeType = mimeType,
        bytes = bytes,
        previewUriString = uri.toString(),
    )
}
