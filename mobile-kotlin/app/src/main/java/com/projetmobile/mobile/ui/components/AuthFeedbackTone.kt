/**
 * Rôle : Énumération listant les tons possibles pour les notifications de retour d'authentification.
 * Précondition : Utilisé par `AuthFeedbackBanner` pour déterminer la couleur du message.
 * Postcondition : Fournit un typage fort bloquant le choix à `Success` ou `Error`.
 */
package com.projetmobile.mobile.ui.components

/**
 * Rôle : Décrit le composant auth feedback tone du module components.
 */
enum class AuthFeedbackTone {
    Success,
    Error,
}
