package com.projetmobile.mobile.ui.screens.admin.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone
import com.projetmobile.mobile.ui.screens.admin.shared.roleDisplayName

/**
 * Rôle : Composant pour l'affichage public ou privé d'une fiche d'informations globale de l'utilisateur (Pseudo, Email, Role).
 *
 * Précondition : Récupère les données depuis [uiState] et nécessite un contact/admin valide.
 *
 * Postcondition : Construit la vue en Scrolled Column et offre les boutons d'action Edit ou Delete à l'admin.
 */
@Composable
internal fun AdminUserDetailScreen(
    uiState: AdminUserDetailUiState,
    onEditUser: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onEditUser,
                    enabled = uiState.user != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF3F4F6),
                        contentColor = Color(0xFF111827),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                    )
                    Text(" Modifier", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (uiState.errorMessage != null) {
            item {
                AuthFeedbackBanner(
                    message = uiState.errorMessage,
                    tone = AuthFeedbackTone.Error,
                )
            }
        }

        if (uiState.isLoading) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (uiState.user != null) {
            item {
                Text(
                    text = "FICHE UTILISATEUR",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6B7280),
                    letterSpacing = androidx.compose.ui.unit.TextUnit(
                        1.5f,
                        androidx.compose.ui.unit.TextUnitType.Sp,
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Détails du compte",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )

            }

            item {
                AdminUserDetailCard(user = uiState.user)
            }
        }
    }
}

@Composable
private fun AdminUserDetailCard(
    user: AuthUser,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailField(label = "IDENTIFIANT", value = user.login)
            DetailField(label = "NOM", value = "${user.firstName} ${user.lastName}")
            DetailField(label = "ROLE", value = roleDisplayName(user.role))
            DetailField(label = "EMAIL", value = user.email)
            DetailField(label = "TELEPHONE", value = user.phone ?: "Non renseigné")
            DetailField(
                label = "STATUT EMAIL",
                value = if (user.emailVerified) "Vérifié" else "Non vérifié",
                valueColor = if (user.emailVerified) Color(0xFF16A34A) else Color(0xFFB91C1C),
            )
            DetailField(label = "COMPTE CRÉÉ LE", value = user.createdAt.take(10))
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF111827),
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF9CA3AF),
            letterSpacing = androidx.compose.ui.unit.TextUnit(
                1.2f,
                androidx.compose.ui.unit.TextUnitType.Sp,
            ),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = valueColor,
        )
    }
}
