/**
 * Rôle : Regroupe les briques UI réutilisables propres aux écrans de réservants.
 * Ce fichier contient les éléments visuels partagés pour les badges, cartes de chargement et lignes d'information.
 * Précondition : Les écrans de réservants doivent composer ces briques au lieu de dupliquer le style.
 * Postcondition : L'interface garde une présentation homogène dans tout le sous-domaine réservants.
 */
package com.projetmobile.mobile.ui.screens.reservants

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthCard

@Composable
/**
 * Rôle : Affiche un chip compact pour signaler un attribut ou un type de réservant.
 * Précondition : `text` doit être un libellé court et déjà prêt à afficher.
 * Postcondition : Retourne un badge arrondi cohérent avec le style des écrans de réservants.
 */
internal fun ReservantMetaChip(
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
            color = Color(0xFF255EC8),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
/**
 * Rôle : Affiche une carte de chargement standardisée pour les écrans de réservants.
 * Précondition : Le composant doit être utilisé pendant une phase de chargement asynchrone.
 * Postcondition : L'utilisateur voit une carte avec progression et message d'attente.
 */
internal fun ReservantsLoadingCard(
    text: String = "Chargement…",
    modifier: Modifier = Modifier,
) {
    AuthCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator()
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF18233A),
            )
        }
    }
}

@Composable
/**
 * Rôle : Affiche un état vide standardisé pour les écrans de réservants.
 * Précondition : Le titre et le corps doivent résumer clairement l'absence de données.
 * Postcondition : L'utilisateur voit une carte explicative à la place d'une liste vide silencieuse.
 */
internal fun ReservantsEmptyCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    AuthCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF18233A),
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF5D6981),
            )
        }
    }
}

@Composable
/**
 * Rôle : Affiche une ligne d'information clé/valeur dans les écrans de réservants.
 * Précondition : `label` et `value` doivent décrire une information simple à présenter.
 * Postcondition : La valeur est rendue sur une ligne lisible, avec un fallback visuel si la donnée est vide.
 */
internal fun ReservantInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF5D6981),
        )
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF18233A),
        )
    }
}
