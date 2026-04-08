/**
 * Rôle : Composant unique de saisie permettant la modification d'un élément du profil.
 *
 * Précondition : Exige le StateFlow et l'accès à un Validateur.
 *
 * Postcondition : Affiche les erreurs locales ou valide un TextField Jetpack Compose.
 */
package com.projetmobile.mobile.ui.screens.profile

/**
 * Rôle : Décrit le composant profil editable champ du module le profil.
 */
enum class ProfileEditableField {
    Login,
    FirstName,
    LastName,
    Email,
    Phone,
}

/**
 * Rôle : Exécute l'action editable champs with erreurs du module le profil.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
internal fun editableFieldsWithErrors(form: ProfileFormState): Set<ProfileEditableField> {
    val fields = mutableSetOf<ProfileEditableField>()
    if (form.loginError != null) {
        fields += ProfileEditableField.Login
    }
    if (form.firstNameError != null) {
        fields += ProfileEditableField.FirstName
    }
    if (form.lastNameError != null) {
        fields += ProfileEditableField.LastName
    }
    if (form.emailError != null) {
        fields += ProfileEditableField.Email
    }
    if (form.phoneError != null) {
        fields += ProfileEditableField.Phone
    }
    return fields
}
