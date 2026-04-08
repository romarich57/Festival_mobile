/**
 * Rôle : Fournit un chip visuel réutilisable pour afficher une meta-information courte dans les écrans de jeux.
 * Précondition : Le texte à afficher doit déjà être formaté pour tenir dans un badge compact.
 * Postcondition : L'UI reçoit un élément arrondi et lisible pour signaler une propriété de jeu.
 */
package com.projetmobile.mobile.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
/**
 * Rôle : Affiche un badge compact de meta-donnée dans les écrans de jeux.
 * Précondition : `text` doit contenir une information courte et contextuelle.
 * Postcondition : Le texte est rendu sous forme de chip avec un style cohérent avec le thème de l'application.
 */
internal fun GamesMetaChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0xFFEAF0FB),
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color(0xFF255EC8),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}
