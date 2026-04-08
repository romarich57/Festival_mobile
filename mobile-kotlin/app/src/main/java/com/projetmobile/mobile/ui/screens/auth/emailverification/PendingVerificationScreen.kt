/**
 * Rôle : Affiche l'écran de mise en attente invitant l'utilisateur à vérifier sa boîte mail.
 * Propose une interface simplifiée permettant de demander un renvoi du lien de confirmation.
 * Précondition : Appelé juste après une inscription réussie ou lors d'une tentative de renvoi d'email.
 * Postcondition : Informe visuellement qu'un email de connexion a été expédié.
 */
package com.projetmobile.mobile.ui.screens.auth.emailverification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.AuthLinkButton
import com.projetmobile.mobile.ui.components.PrimaryAuthButton

@Composable
/**
 * Rôle : Exécute l'action en attente verification écran du module l'authentification emailverification.
 *
 * Précondition : L'état UI et les callbacks ou dépendances nécessaires doivent être disponibles.
 *
 * Postcondition : L'interface reflète l'état courant et propage les événements utilisateur.
 */
fun PendingVerificationScreen(
    uiState: PendingVerificationUiState,
    onResendVerification: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (uiState.email.isBlank()) {
                    "Votre compte a été créé. Consultez votre boîte mail pour confirmer votre adresse."
                } else {
                    "Un email de vérification a été envoyé à ${uiState.email}. Ouvrez-le depuis votre téléphone pour revenir directement dans l’app."
                },
                style = MaterialTheme.typography.bodyLarge,
            )

            uiState.errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
            uiState.infoMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.secondary)
            }

            PrimaryAuthButton(
                text = if (uiState.isLoading) "Envoi..." else "Renvoyer un email",
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                onClick = onResendVerification,
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            AuthLinkButton(
                text = "Retour à la connexion",
                onClick = onBackToLogin,
            )
        }
    }
}
