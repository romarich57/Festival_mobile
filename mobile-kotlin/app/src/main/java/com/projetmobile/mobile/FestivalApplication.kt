package com.projetmobile.mobile

import android.app.Application
import com.projetmobile.mobile.data.repository.AndroidNetworkStatusProvider
import com.projetmobile.mobile.data.repository.RepositoryNetworkStatus
import com.projetmobile.mobile.data.database.AppDatabase
import com.projetmobile.mobile.data.sync.AndroidSyncWorkScheduler
import com.projetmobile.mobile.data.sync.RepositorySyncScheduler

class FestivalApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        RepositoryNetworkStatus.initialize(AndroidNetworkStatusProvider(this))
        appContainer = AppContainer(this)
        RepositorySyncScheduler.initialize(
            AndroidSyncWorkScheduler(
                context = this,
                database = AppDatabase.getInstance(this),
            ),
        )
        RepositorySyncScheduler.start()
    }
}
