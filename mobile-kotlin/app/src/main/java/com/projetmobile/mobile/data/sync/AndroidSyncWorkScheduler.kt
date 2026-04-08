package com.projetmobile.mobile.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.projetmobile.mobile.data.database.AppDatabase
import com.projetmobile.mobile.data.worker.FestivalSyncWorker
import com.projetmobile.mobile.data.worker.GameSyncWorker
import com.projetmobile.mobile.data.worker.ReservantSyncWorker
import com.projetmobile.mobile.data.worker.ReservationSyncWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AndroidSyncWorkScheduler(
    context: Context,
    private val database: AppDatabase,
) : SyncWorkScheduler {
    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val connectivityManager = appContext.getSystemService(ConnectivityManager::class.java)

    @Volatile
    private var started = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (hasValidatedNetwork(network)) {
                schedulePendingSyncAsync()
            }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                schedulePendingSyncAsync()
            }
        }
    }

    override suspend fun schedulePendingSync() {
        if (database.gameDao().countPendingWork() > 0) {
            enqueue<GameSyncWorker>(GameSyncWorker.WORK_NAME)
        }
        if (database.reservantDao().countPendingWork() > 0) {
            enqueue<ReservantSyncWorker>(ReservantSyncWorker.WORK_NAME)
        }
        if (database.reservationDao().countPendingWork() > 0) {
            enqueue<ReservationSyncWorker>(ReservationSyncWorker.WORK_NAME)
        }
        if (database.festivalDao().countPendingWork() > 0) {
            enqueue<FestivalSyncWorker>(FestivalSyncWorker.WORK_NAME)
        }
    }

    override fun schedulePendingSyncAsync() {
        scope.launch {
            schedulePendingSync()
        }
    }

    override fun start() {
        if (started) {
            return
        }
        started = true
        schedulePendingSyncAsync()
        runCatching {
            connectivityManager?.registerDefaultNetworkCallback(networkCallback)
        }
    }

    private inline fun <reified T : androidx.work.ListenableWorker> enqueue(workName: String) {
        val request = OneTimeWorkRequestBuilder<T>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    private fun hasValidatedNetwork(network: Network): Boolean {
        val capabilities = connectivityManager?.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
