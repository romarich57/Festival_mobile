/**
 * Rôle : Utilitaires et constantes gérant les rôles disponibles pour la partie administration (ex: formater les noms de rôles).
 *
 * Précondition : Utilisé dans les menus déroulants ou l'affichage textuel liés aux rôles administrateurs.
 *
 * Postcondition : Fournit un affichage lisible et uniformisé des différents niveaux d'accréditation.
 */
package com.projetmobile.mobile.ui.screens.admin.shared

val ADMIN_AVAILABLE_ROLES = listOf("benevole", "organizer", "super-organizer", "admin")

/**
 * Rôle : Exécute l'action role display name du module l'administration partagé.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
fun roleDisplayName(role: String): String = when (role) {
    "benevole" -> "Bénévole"
    "organizer" -> "Organisateur"
    "super-organizer" -> "Super-organisateur"
    "admin" -> "Admin"
    else -> role.replaceFirstChar { it.uppercase() }
}
