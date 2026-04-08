package com.projetmobile.mobile.data.room

/**
 * Rôle : Constantes permettant de classifier le type de tentative de synchronisation
 * réactive. Détermine quelle requête POST/PUT/DELETE envoyer au backend lors
 * de l'envoi des données locales non-synchronisées.
 *
 * Précondition : Composant utilisé par le Synchronizer et lié aux colonnes de retry
 * dans les tables Room.
 * Postcondition : Identifie l'action que le système doit refaire sur un échec ou un
 * changement hors ligne.
 */
object SyncRetryAction {
    const val CREATE = "CREATE"
    const val UPDATE = "UPDATE"
    const val DELETE = "DELETE"
}
