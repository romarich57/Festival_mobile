package com.projetmobile.mobile.data.remote.auth

import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * Rôle : Intercepteur OkHttp global pour l'application. Surveille les requêtes HTTP
 * échouant en 401 (Non Autorisé) ou 403 (Interdit), tente un rafraîchissement du
 * jeton d'authentification en appelant `/auth/refresh`, et re-tente la requête
 * initiale si le rafraîchissement est un succès.
 * 
 * Précondition : Doit être ajouté au client OkHttpClient principal de Retrofit.
 * Postcondition : Assure la persistance d'une session sans coupure pour l'utilisateur
 * transparent aux autres couches en renouvelant automatiquement les cookies périmés.
 */
class AuthRefreshInterceptor(
    baseUrl: String,
    cookieJar: CookieJar,
) : Interceptor {
    private val refreshUrl = checkNotNull(baseUrl.toHttpUrl().resolve("auth/refresh")) {
        "Impossible de résoudre l'endpoint auth/refresh depuis $baseUrl"
    }

    // Instance Http isolée pour faire l'appel de refresh afin d'éviter une boucle infinie
    // dans le circuit des intercepteurs de l'instance principale.
    private val refreshClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()

    // Verrou pour empêcher de multiples threads (appels asynchrones concurrents) de rafraichir
    // le token en même temps.
    private val refreshLock = Any()

    /**
     * Rôle : Traverse la requête et gère la logique de retry si non autorisé.
     * 
     * Précondition : Invoqué de base par OkHttp à chaque requête.
     * Postcondition : Retourne la Response validée ou l'erreur formelle 401 si le refresh échoue.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!shouldAttemptRefresh(request, response)) {
            return response
        }

        // Bloque le thread pour le premier appelant afin de refresh, les autres 
        // attendront de savoir si ça a marché avant de réessayer.
        val refreshSucceeded = synchronized(refreshLock) {
            executeRefresh()
        }

        if (!refreshSucceeded) {
            return response
        }

        response.close()
        return chain.proceed(
            request.newBuilder()
                .header(RETRY_HEADER, RETRY_HEADER_VALUE)
                .build(),
        )
    }

    /**
     * Rôle : Appelle le endpoint de refresh pour réclamer un nouveau JWT actif.
     * 
     * Précondition : Le refresh token valide doit être présent dans les cookies HTTP du [CookieJar].
     * Postcondition : Renvoie `true` si le serveur a renouvelé le jeton.
     */
    private fun executeRefresh(): Boolean {
        val refreshRequest = Request.Builder()
            .url(refreshUrl)
            .post(ByteArray(0).toRequestBody(null))
            .build()

        return runCatching {
            refreshClient.newCall(refreshRequest).execute().use { refreshResponse ->
                refreshResponse.isSuccessful
            }
        }.getOrDefault(false)
    }

    /**
     * Rôle : Conditionne la nécessité d'une tentative de rafraîchissement
     * de session face à une réponse d'erreur donnée.
     * 
     * Précondition : Une réponse HTTP d'erreur a été interceptée.
     * Postcondition : Empêche la boucle infinie de retry et exclut les routes de login public.
     */
    private fun shouldAttemptRefresh(request: Request, response: Response): Boolean {
        // Uniquement tenter le retry sur expiration (401) ou refus de droit strict (403)
        if (response.code != 401 && response.code != 403) {
            return false
        }
        // Si un header personnalisé de retry est déjà présent, alors c'est un second échec, STOP
        if (request.header(RETRY_HEADER) == RETRY_HEADER_VALUE) {
            return false
        }

        val path = request.url.encodedPath
        // Inutile de refresh le refresh qui a lui-même échoué
        if (path.endsWith("/auth/refresh")) {
            return false
        }
        // Inutile de refresh sur les routes auth publiques (login/register) 
        // L'exception étant whoami qui a besoin d'être authentifié
        if (path.contains("/auth/") && !path.endsWith("/auth/whoami")) {
            return false
        }

        return true
    }

    private companion object {
        private const val RETRY_HEADER = "X-Auth-Refresh-Retry"
        private const val RETRY_HEADER_VALUE = "1"
    }
}
