package com.projetmobile.mobile.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Rôle : Abstraction définissant la vérification du statut du réseau.
 * 
 * Précondition : Aucune.
 * Postcondition : Permet de découpler la logique de vérification réseau (mockable) de celle d'Android.
 */
fun interface NetworkStatusProvider {
    fun hasValidatedNetwork(): Boolean
}

/**
 * Rôle : Singleton centralisant l'accès à l'état de la connexion internet pour la couche Repository.
 * 
 * Précondition : Doit être initialisé (`initialize()`) au lancement avec un [NetworkStatusProvider] valide.
 * Postcondition : Vérifie la connectivité sans dépendre statiquement du framework Android (+ facilite les tests avec `resetForTests()`).
 */
object RepositoryNetworkStatus {
    private val offlineProvider = NetworkStatusProvider { false }

    @Volatile
    private var provider: NetworkStatusProvider = offlineProvider

    fun initialize(provider: NetworkStatusProvider) {
        this.provider = provider
    }

    fun hasValidatedNetwork(): Boolean {
        return runCatching { provider.hasValidatedNetwork() }.getOrDefault(false)
    }

    fun resetForTests() {
        provider = offlineProvider
    }
}

/**
 * Rôle : Implémentation réelle de la vérification réseau via le `ConnectivityManager` d'Android.
 * 
 * Précondition : Nécessite un `Context` valide de l'application et les permissions associées telles qu'ACCESS_NETWORK_STATE.
 * Postcondition : Confirme formellement si l'appareil a une connexion internet active *et* validée (route vers l'extérieur).
 */
class AndroidNetworkStatusProvider(
    context: Context,
) : NetworkStatusProvider {
    private val appContext = context.applicationContext

    override fun hasValidatedNetwork(): Boolean {
        val connectivityManager = appContext.getSystemService(ConnectivityManager::class.java)
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
