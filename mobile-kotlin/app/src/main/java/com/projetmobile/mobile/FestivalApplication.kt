package com.projetmobile.mobile

import android.app.Application
import com.projetmobile.mobile.data.repository.AndroidNetworkStatusProvider
import com.projetmobile.mobile.data.repository.RepositoryNetworkStatus

class FestivalApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        RepositoryNetworkStatus.initialize(AndroidNetworkStatusProvider(this))
        appContainer = AppContainer(this)
    }
}
