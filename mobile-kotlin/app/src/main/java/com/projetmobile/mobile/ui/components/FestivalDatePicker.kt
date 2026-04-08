/**
 * Rôle : Champ textuel combiné à un sélecteur de date (DatePicker) de Jetpack Compose (Material 3).
 * Offre un format de calendrier natif pour la sélection, tout en affichant l'icône de calendrier familière.
 * Précondition : Un gestionnaire de l'état (ViewModel) observe et sauvegarde la chaîne choisie.
 * Postcondition : Affiche un champ bloqué qui, une fois cliqué, déploie le calendrier modal. La sélection renvoie le jour "yyyy-MM-dd".
 */
package com.projetmobile.mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalDatePicker(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    var showPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Box(modifier = modifier) {
        FestivalTextField(
            value = value,
            onValueChange = {},
            label = label,
            isError = isError,
            supportingText = supportingText,
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            trailingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
            },
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true }
        )
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        onValueChange(date.format(formatter))
                    }
                    showPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
