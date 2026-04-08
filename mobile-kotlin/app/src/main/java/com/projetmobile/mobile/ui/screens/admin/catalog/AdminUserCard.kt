/**
 * Rôle : Composant visuel représentant une carte (Card) d'un utilisateur dans la liste d'administration.
 *
 * Précondition : Doit recevoir un objet de type utilisateur contenant au minimum le nom et le rôle.
 *
 * Postcondition : Affiche les informations de base de l'utilisateur et gère l'interaction de clic.
 */
package com.projetmobile.mobile.ui.screens.admin.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.ui.screens.admin.shared.ADMIN_AVAILABLE_ROLES
import com.projetmobile.mobile.ui.screens.admin.shared.roleDisplayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Rôle : Exécute l'action administration utilisateur carte du module l'administration catalogue.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
internal fun AdminUserCard(
    user: AuthUser,
    isDeleting: Boolean,
    isUpdatingRole: Boolean,
    onCardClick: () -> Unit,
    onRoleChanged: (String) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = user.login,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${user.firstName} ${user.lastName}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
            )

            Spacer(modifier = Modifier.height(8.dp))

            EmailVerifiedBadge(verified = user.emailVerified)

            Spacer(modifier = Modifier.height(8.dp))

            if (isUpdatingRole) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.height(32.dp))
                }
            } else {
                Text(
                    text = "Role",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6B7280),
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = roleDisplayName(user.role),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false },
                    ) {
                        ADMIN_AVAILABLE_ROLES.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(roleDisplayName(role)) },
                                onClick = {
                                    roleDropdownExpanded = false
                                    if (role != user.role) onRoleChanged(role)
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDeleteClick,
                enabled = !isDeleting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFEF2F2),
                    contentColor = Color(0xFFB91C1C),
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = Color(0xFFB91C1C),
                    )
                } else {
                    Text("Supprimer", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
/**
 * Rôle : Exécute l'action email verified badge du module l'administration catalogue.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
private fun EmailVerifiedBadge(verified: Boolean) {
    val bgColor = if (verified) Color(0xFFDCFCE7) else Color(0xFFFEF2F2)
    val textColor = if (verified) Color(0xFF16A34A) else Color(0xFFB91C1C)
    val label = if (verified) "Vérifié" else "Non vérifié"

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}
