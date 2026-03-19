package com.projetmobile.mobile.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone
import com.projetmobile.mobile.ui.components.PrimaryAuthButton

@Composable
fun ProfileScreen(
    currentUser: AuthUser?,
    isLoggingOut: Boolean,
    errorMessage: String?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (currentUser != null) {
                Text(
                    text = "${currentUser.firstName} ${currentUser.lastName}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = currentUser.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Connecté en tant que ${currentUser.login}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "Aucune session active.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            errorMessage?.let { message ->
                AuthFeedbackBanner(
                    message = message,
                    tone = AuthFeedbackTone.Error,
                )
            }

            PrimaryAuthButton(
                text = if (isLoggingOut) "Déconnexion..." else "Se déconnecter",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("logout-button"),
                enabled = currentUser != null && !isLoggingOut,
                onClick = onLogout,
            )
        }
    }
}
