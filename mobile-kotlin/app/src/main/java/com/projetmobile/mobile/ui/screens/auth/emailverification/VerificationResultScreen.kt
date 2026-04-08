/**
 * Rôle : Compose l'écran l'authentification emailverification et orchestre l'affichage de l'état et des actions utilisateur.
 */

package com.projetmobile.mobile.ui.screens.auth.emailverification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.auth.VerificationResultStatus
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.PrimaryAuthButton

/**
 * Rôle : Écran affichant le résultat de la vérification de l'email (succès, expiré, invalide, erreur).
 *
 * Précondition : Le statut de vérification doit être fourni.
 *
 * Postcondition : Affiche un message adapté au statut et propose un bouton pour retourner à la connexion.
 */
@Composable
fun VerificationResultScreen(
    status: VerificationResultStatus,
    onNavigateLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = when (status) {
        VerificationResultStatus.Success ->
            "Votre adresse email est vérifiée. Vous pouvez maintenant vous connecter."
        VerificationResultStatus.Expired ->
            "Le lien de vérification a expiré. Demandez un nouvel email depuis l’écran de connexion."
        VerificationResultStatus.Invalid ->
            "Ce lien n’est pas valide. Vérifiez le dernier email reçu ou demandez un renvoi."
        VerificationResultStatus.Error ->
            "Une erreur est survenue pendant la vérification. Réessayez plus tard."
    }

    AuthCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
            )
            PrimaryAuthButton(
                text = "Aller à la connexion",
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateLogin,
            )
        }
    }
}
