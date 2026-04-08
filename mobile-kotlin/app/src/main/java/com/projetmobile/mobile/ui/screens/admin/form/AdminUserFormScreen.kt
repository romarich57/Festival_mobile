/**
 * Rôle : Compose l'écran l'administration formulaire et orchestre l'affichage de l'état et des actions utilisateur.
 */

package com.projetmobile.mobile.ui.screens.admin.form

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone
import com.projetmobile.mobile.ui.components.FestivalTextField
import com.projetmobile.mobile.ui.screens.admin.shared.ADMIN_AVAILABLE_ROLES
import com.projetmobile.mobile.ui.screens.admin.shared.roleDisplayName

/**
 * Rôle : Affiche le formulaire en tant qu'écran d'édition ou création d'une fiche utilisateur administrateur.
 *
 * Précondition : Des données initiales issues de [uiState] ou un éditeur vierge.
 *
 * Postcondition : Affiche les champs de login, email, role, et transmet les modifications via les callbacks associés.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdminUserFormScreen(
    uiState: AdminUserFormUiState,
    onLoginChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onRoleSelected: (String) -> Unit,
    onEmailVerifiedChanged: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    val isEdit = uiState.mode is AdminUserFormMode.Edit

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "ADMINISTRATION",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B7280),
                letterSpacing = androidx.compose.ui.unit.TextUnit(
                    1.5f,
                    androidx.compose.ui.unit.TextUnitType.Sp,
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isEdit) "Modifier un utilisateur" else "Créer un utilisateur",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        if (uiState.errorMessage != null) {
            item {
                // Les erreurs métier sont affichées avant le formulaire pour rester immédiatement visibles.
                AuthFeedbackBanner(
                    message = uiState.errorMessage,
                    tone = AuthFeedbackTone.Error,
                )
            }
        }

        if (uiState.isLoading) {
            // L'écran garde un état de progression tant que la fiche utilisateur n'est pas prête.
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
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        FestivalTextField(
                            value = uiState.form.login,
                            onValueChange = onLoginChanged,
                            label = "Identifiant",
                            isError = uiState.form.loginError != null,
                            supportingText = uiState.form.loginError,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (!isEdit) {
                            // Le mot de passe n'est demandé qu'à la création afin d'éviter une saisie inutile en édition.
                            FestivalTextField(
                                value = uiState.form.password,
                                onValueChange = onPasswordChanged,
                                label = "Mot de passe",
                                isError = uiState.form.passwordError != null,
                                supportingText = uiState.form.passwordError,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        FestivalTextField(
                            value = uiState.form.firstName,
                            onValueChange = onFirstNameChanged,
                            label = "Prénom",
                            isError = uiState.form.firstNameError != null,
                            supportingText = uiState.form.firstNameError,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        FestivalTextField(
                            value = uiState.form.lastName,
                            onValueChange = onLastNameChanged,
                            label = "Nom",
                            isError = uiState.form.lastNameError != null,
                            supportingText = uiState.form.lastNameError,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        FestivalTextField(
                            value = uiState.form.email,
                            onValueChange = onEmailChanged,
                            label = "Email",
                            isError = uiState.form.emailError != null,
                            supportingText = uiState.form.emailError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        FestivalTextField(
                            value = uiState.form.phone,
                            onValueChange = onPhoneChanged,
                            label = "Téléphone (optionnel)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Text(
                            text = "Rôle",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF6B7280),
                        )
                        ExposedDropdownMenuBox(
                            expanded = roleDropdownExpanded,
                            onExpandedChange = { roleDropdownExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = roleDisplayName(uiState.form.role),
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
                                            onRoleSelected(role)
                                        },
                                    )
                                }
                            }
                        }

                        if (isEdit) {
                            // Le basculeur de vérification email n'existe qu'en mode édition.
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Checkbox(
                                    checked = uiState.form.emailVerified,
                                    onCheckedChange = onEmailVerifiedChanged,
                                )
                                Text(
                                    text = "Email vérifié",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onSubmit,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            color = Color.White,
                        )
                    } else {
                        Text(
                            text = if (isEdit) "Enregistrer les modifications" else "Créer l'utilisateur",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}
