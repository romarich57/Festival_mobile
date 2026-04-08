package com.projetmobile.mobile.data.sync

interface SyncWorkScheduler {
    suspend fun schedulePendingSync()
    fun schedulePendingSyncAsync()
    fun start()
}

object RepositorySyncScheduler {
    @Volatile
    private var scheduler: SyncWorkScheduler? = null

    fun initialize(syncWorkScheduler: SyncWorkScheduler) {
        scheduler = syncWorkScheduler
    }

    suspend fun schedulePendingSync() {
        scheduler?.schedulePendingSync()
    }

    fun schedulePendingSyncAsync() {
        scheduler?.schedulePendingSyncAsync()
    }

    fun start() {
        scheduler?.start()
    }
}
