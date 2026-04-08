/**
 * Rôle : Fournit un arrière-plan (fond) avec dégradé subtil à utiliser dans toute l'application.
 * Contribue à homogénéiser l'identité visuelle de l'app de bout en bout.
 * Précondition : Encapsule les écrans composables ayant besoin d'être sur fond coloré (en opposition à fond blanc uni).
 * Postcondition : Un conteneur englobant tout son contenu visuel, avec un arrière-plan dégradé coloré.
 */
package com.projetmobile.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun GradientScreenBackground(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopCenter,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF4F6FD),
                        Color(0xFFE8EEF9),
                        Color(0xFFF7F9FF),
                    ),
                ),
            ),
        contentAlignment = contentAlignment,
        content = content,
    )
}
