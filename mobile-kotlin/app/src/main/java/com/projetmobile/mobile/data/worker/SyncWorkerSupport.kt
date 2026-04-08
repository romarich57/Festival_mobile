package com.projetmobile.mobile.data.worker

import com.projetmobile.mobile.data.repository.RepositoryException
import com.projetmobile.mobile.data.repository.RepositoryFailureKind
import com.projetmobile.mobile.data.repository.parseBackendErrorMessage
import com.projetmobile.mobile.data.repository.toRepositoryException
import retrofit2.HttpException

/**
 * Rôle : Identifier parmi les erreurs remontées par l'API si l'élément qu'on tente de supprimer en arrière-plan a déjà été supprimé (gestion de conflit optimiste).
 * 
 * Précondition : Une exception Retrofit/HttpException capturée pendant le catch du worker.
 * Postcondition : `true` si le code HTTP est 404/410 ou si le message du body HTTP indique "not found", ce qui valide fictivement l'action locale (pas de retry nécessaire).
 */
internal fun Throwable.isDeleteAlreadyApplied(): Boolean {
    if (this is HttpException && (code() == 404 || code() == 410)) {
        return true
    }

    val backendMessage = (this as? HttpException)
        ?.parseBackendErrorMessage()
        ?.lowercase()
        .orEmpty()

    return backendMessage.contains("introuv") ||
        backendMessage.contains("not found") ||
        backendMessage.contains("already deleted") ||
        backendMessage.contains("deja supprim")
}

/**
 * Rôle : Évaluer l'opportunité de conserver une tâche (pending) dans la base de données Room pour la retenter au prochain passage du WorkManager.
 * 
 * Précondition : Le code d'exception `Throwable` généré lors du plantage serveur/réseau.
 * Postcondition : Renvoie vrai si l'erreur était purement liée à un Timeout/Offline passager (retry legit), faux si c'était une validation cassée "en dur" (Auth/Validation JSON) où relancer la même action relèvera toujours de l'échec.
 */
internal fun Throwable.isRetryableSyncFailure(defaultMessage: String): Boolean {
    if (isDeleteAlreadyApplied()) {
        return false
    }

    val repositoryException = toRepositoryException(defaultMessage) as? RepositoryException
        ?: return true

    return when (repositoryException.kind) {
        RepositoryFailureKind.Offline,
        RepositoryFailureKind.BackendUnreachable,
        RepositoryFailureKind.Timeout,
        RepositoryFailureKind.Server,
        RepositoryFailureKind.Unknown -> true

        RepositoryFailureKind.Auth,
        RepositoryFailureKind.Validation -> false
    }
}

/**
 * Rôle : Extraire un texte propre (fallback vers une chaîne basique) sur le motif d'erreur de la resynchronisation en cours d'un item.
 * 
 * Précondition : Le déclencheur Throwable.
 * Postcondition : Construit une chaîne de String finale à insérer dans la colonne `lastSyncErrorMessage` (Room) pour diagnostiquer le mal depuis l'UI Android.
 */
internal fun Throwable.toSyncFailureMessage(defaultMessage: String): String {
    val repositoryException = toRepositoryException(defaultMessage) as? RepositoryException
    return repositoryException?.message?.takeIf { it.isNotBlank() }
        ?: localizedMessage?.takeIf { it.isNotBlank() }
        ?: defaultMessage
}
