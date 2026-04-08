package com.projetmobile.mobile.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun interface NetworkStatusProvider {
    fun hasValidatedNetwork(): Boolean
}

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
