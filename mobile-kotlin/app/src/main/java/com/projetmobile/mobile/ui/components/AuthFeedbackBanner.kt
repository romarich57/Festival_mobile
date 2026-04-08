/**
 * Rôle : Composant visuel affichant un bandeau de retour (erreur ou succès) pour les actions d'authentification.
 * Aligne les couleurs du texte et de l'arrière-plan en fonction du ton spécifié.
 * Précondition : Doit recevoir un message non vide et une valeur de `AuthFeedbackTone`.
 * Postcondition : Affiche un encadré de confirmation coloré dans l'UI.
 */
package com.projetmobile.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AuthFeedbackBanner(
    message: String,
    tone: AuthFeedbackTone,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when (tone) {
        AuthFeedbackTone.Success -> Color(0xFFE3F6E9)
        AuthFeedbackTone.Error -> Color(0xFFFDE6E6)
    }
    val textColor = when (tone) {
        AuthFeedbackTone.Success -> Color(0xFF087443)
        AuthFeedbackTone.Error -> Color(0xFFB42318)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 18.dp, vertical = 20.dp),
    ) {
        Text(
            text = message,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
