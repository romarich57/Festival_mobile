/**
 * Rôle : Carte d'affichage unique résumant les informations d'un festival 
 * (dates, nom, stock global restant) sous forme sélectionnable.
 * Précondition : Le parent "FestivalList" fournit l'état de sélection et les données `FestivalSummary`.
 * Postcondition : Affiche une carte Material 3 cliquable. N'interagit pas directement avec un ViewModel.
 */
package com.projetmobile.mobile.ui.components.festival

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.festival.FestivalSummary

/**
 * Carte d'affichage d'un festival.
 *
 * Traduction du FestivalCardComponent Angular :
 *  - `festival`     = input.required<FestivalDto>()
 *  - `isSelected`   = computed(() => selected)       ← calculé par le parent (FestivalScreen)
 *  - `canDelete`    = input(false)
 *  - `onSelect`     = output<number | null>()        ← lambda au lieu d'EventEmitter
 *  - `onDelete`     = output<number>()               ← lambda au lieu d'EventEmitter
 *
 * ⚠️ Ce composant ne connaît PAS le ViewModel.
 *    C'est FestivalScreen qui calcule `isSelected` et branche les lambdas.
 *    → Principe S (Single Responsibility) : afficher + émettre, rien d'autre.
 */
@Composable
fun FestivalCard(
    festival: FestivalSummary,
    isSelected: Boolean,                          // calculé par le parent
    onSelect: (id: Int?) -> Unit,                 // équivalent : select = output<number | null>()
    modifier: Modifier = Modifier,
    canDelete: Boolean = false,                   // équivalent : canDelete = input(false)
    onDelete: (id: Int) -> Unit = {},             // équivalent : deleteFestival = output<number>()
) {
    // Animation couleur de la bordure selon la sélection (comme la classe CSS active Angular)
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "borderColor",
    )

    Card(
        onClick = {
            // Équivalent onFestivalClick() Angular :
            // si déjà sélectionné → émet null (désélection), sinon émet l'id
            if (isSelected) onSelect(null) else onSelect(festival.id)
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 3.dp,
        ),
        border = BorderStroke(width = 2.dp, color = borderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            // ── En-tête : nom + bouton suppression ───────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = festival.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )

                // Équivalent onDeleteClick() Angular (stopPropagation géré nativement
                // car le bouton est dans la carte mais a son propre onClick)
                if (canDelete) {
                    IconButton(
                        onClick = { onDelete(festival.id) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer le festival",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // ── Dates ─────────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Du ${festival.startDate} au ${festival.endDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ── Stock tables ──────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.TableBar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "${festival.totalTables} tables restantes · ${festival.stockChaises} chaises",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
