/**
 * Rôle : Affiche un message de bloc "En cours d'implémentation" mis au milieu d'une vue.
 * Construit un contenu mock rapidement dans des sections de route non terminées.
 * Précondition : Placé temporairement à l'intérieur de zones UI en développement.
 * Postcondition : Affiche le texte par défaut avec une typographie moyenne centrée.
 */
package com.projetmobile.mobile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
/**
 * Rôle : Exécute l'action implementation placeholder du module components.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
fun ImplementationPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Section en cours d'implémentation",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
    }
}
