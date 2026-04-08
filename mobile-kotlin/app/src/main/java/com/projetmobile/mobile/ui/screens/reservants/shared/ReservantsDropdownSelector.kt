/**
 * Rôle : Fournit un sélecteur déroulant réutilisable pour les écrans de réservants.
 * Ce composant aligne l'apparence des menus de choix utilisés dans les formulaires et les filtres.
 * Précondition : L'appelant doit fournir un label, la valeur affichée et la liste des options.
 * Postcondition : L'utilisateur peut choisir une option et le callback reçoit la valeur sélectionnée.
 */
package com.projetmobile.mobile.ui.screens.reservants

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
/**
 * Rôle : Affiche un menu déroulant stylisé pour sélectionner une valeur parmi des options de réservants.
 * Précondition : `options` doit contenir des paires libellé/valeur cohérentes avec `selectedLabel`.
 * Postcondition : L'option sélectionnée est renvoyée via `onValueSelected` et le menu se referme.
 */
internal fun <T> ReservantsDropdownSelector(
    label: String,
    selectedLabel: String,
    options: List<Pair<String, T>>,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF5D6981),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = selectedLabel,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF18233A),
                )
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = null,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { (optionLabel, optionValue) ->
                    DropdownMenuItem(
                        text = { Text(optionLabel) },
                        onClick = {
                            expanded = false
                            onValueSelected(optionValue)
                        },
                    )
                }
            }
        }
    }
}
