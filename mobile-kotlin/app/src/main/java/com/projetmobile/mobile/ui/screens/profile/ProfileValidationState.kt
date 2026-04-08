/**
 * Rôle : Représente visuellement l'état de validité des divers champs d'un profil (erreur email, mdp).
 *
 * Précondition : Généré pendant l'enregistrement local ou après une vérification depuis le serveur.
 *
 * Postcondition : Conditionne l'affichage des messages d'erreur en rouge dans les champs UI.
 */
package com.projetmobile.mobile.ui.screens.profile

import com.projetmobile.mobile.ui.utils.validation.ProfileUpdateValidationResult

/**
 * Rôle : Injecte les erreurs de validation du profil dans l'état UI courant.
 *
 * Précondition : `validation` doit provenir d'un contrôle local déjà effectué sur les champs du formulaire.
 *
 * Postcondition : Les champs invalides portent leurs erreurs et l'UI peut les mettre en évidence sans contact réseau.
 */
internal fun ProfileUiState.withValidationErrors(
    validation: ProfileUpdateValidationResult,
): ProfileUiState {
    val validatedForm = form.withValidation(validation)
    return copy(
        form = validatedForm,
        editingFields = editingFields + editableFieldsWithErrors(validatedForm),
        errorMessage = null,
        infoMessage = null,
    ).recalculated()
}

/**
 * Rôle : Copie les erreurs de validation dans le sous-état formulaire du profil.
 *
 * Précondition : `validation` doit contenir les messages calculés pour chaque champ concerné.
 *
 * Postcondition : Le formulaire retourné expose les erreurs de champ prêtes à être affichées.
 */
private fun ProfileFormState.withValidation(
    validation: ProfileUpdateValidationResult,
): ProfileFormState {
    return copy(
        loginError = validation.usernameError,
        firstNameError = validation.firstNameError,
        lastNameError = validation.lastNameError,
        emailError = validation.emailError,
        phoneError = validation.phoneError,
    )
}
