/**
 * Rôle : Affiche une carte de catalogue pour résumer un réservant et ses actions rapides.
 * Ce fichier contient la présentation d'un item individuel ainsi que ses sous-lignes d'information.
 * Précondition : L'item `ReservantListItem` doit contenir les données minimales à afficher dans le catalogue.
 * Postcondition : L'utilisateur peut ouvrir, éditer ou demander la suppression d'un réservant.
 */
package com.projetmobile.mobile.ui.screens.reservants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.reservants.ReservantListItem
import com.projetmobile.mobile.ui.components.AuthCard

@Composable
/**
 * Rôle : Affiche la carte d'un réservant dans le catalogue.
 * Précondition : `reservant` doit contenir l'identifiant et les informations principales du réservant.
 * Postcondition : La carte affiche les informations résumées et déclenche les callbacks associés aux actions.
 */
internal fun ReservantCatalogCard(
    reservant: ReservantListItem,
    canManageReservants: Boolean,
    canDeleteReservants: Boolean,
    isDeleting: Boolean,
    onOpenReservantDetails: (Int) -> Unit,
    onEditReservant: (Int) -> Unit,
    onRequestDelete: (ReservantListItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("reservant-card-${reservant.id}"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = reservant.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF18233A),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val typeLabel = defaultReservantTypes()
                            .firstOrNull { it.value == reservant.type }
                            ?.label ?: reservant.type
                        ReservantMetaChip(typeLabel)
                    }
                }
                if (canManageReservants) {
                    Row {
                        IconButton(
                            onClick = { onEditReservant(reservant.id) },
                            modifier = Modifier.testTag("reservant-edit-${reservant.id}"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Modifier",
                            )
                        }
                        if (canDeleteReservants) {
                            IconButton(
                                onClick = { onRequestDelete(reservant) },
                                enabled = !isDeleting,
                                modifier = Modifier.testTag("reservant-delete-${reservant.id}"),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "Supprimer",
                                )
                            }
                        }
                    }
                }
            }

            CatalogCardLine(Icons.Outlined.Email, reservant.email)
            reservant.phoneNumber?.takeIf { it.isNotBlank() }?.let { phoneNumber ->
                CatalogCardLine(Icons.Outlined.Call, phoneNumber)
            }
            reservant.editorId?.let { editorId ->
                CatalogCardLine(Icons.Outlined.Storefront, "Éditeur lié #$editorId")
            }

            reservant.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF5D6981),
                )
            }

            OutlinedButton(
                onClick = { onOpenReservantDetails(reservant.id) },
                modifier = Modifier.testTag("reservant-open-${reservant.id}"),
            ) {
                Text("Voir le détail")
            }
        }
    }
}

@Composable
/**
 * Rôle : Affiche une ligne d'information simple avec une icône et une valeur textuelle.
 * Précondition : `icon` et `value` doivent décrire une information lisible en une ligne.
 * Postcondition : Le contenu est présenté de manière compacte avec un alignement homogène.
 */
private fun CatalogCardLine(
    icon: ImageVector,
    value: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF5D6981),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF18233A),
        )
    }
}
