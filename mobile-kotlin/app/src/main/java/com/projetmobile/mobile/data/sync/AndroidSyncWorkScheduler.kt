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

/**
 * Rôle : Implémenter le planificateur des envois/demandes en file d'attente (Offline-First) grâce à `WorkManager` : 
 * il détermine quand rétablir la connexion (`CONNECTED`), comment relancer et quel Worker lancer.
 * 
 * Précondition : Le service demande le descripteur d'application complet et une référence valide vers `AppDatabase`.
 * Postcondition : Attache des écouteurs au gestionnaire de réseau mobile, scrute les tables SQLite concernées et place les `OneTimeWorkRequestBuilder`.
 */
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

    /**
     * Rôle : Détecter dynamiquement les retours à la connectivité pour dépiler immédiatement les actions de fond en attente de synchronisation.
     * 
     * Précondition : Le dispositif Android implémente un module réseau. Callback attaché sur `start`.
     * Postcondition : Épuise, relance ou valide les actions réseau via appel à `schedulePendingSyncAsync` sur changement net.
     */
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

    /**
     * Rôle : Interroger chaque DAO (donnée) pour déceler la présence de modifications locales différées et programmer un worker associé.
     * 
     * Précondition : Le processus a requis une synchronisation. Appelé depuis Flow Coroutines/Worker.
     * Postcondition : Enqueue les `SyncWorker` correspondants s'il existe plus de 0 tâche pendiente dans les bases de données respectives.
     */
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

    /**
     * Rôle : Lancement non-bloquant du `schedulePendingSync()`.
     * 
     * Précondition : L'application n'est pas obligée d'assumer elle-même le cycle de vie Coroutine.
     * Postcondition : Une tâche en arrière plan est générée et ajoutée dans SupervisorJob.
     */
    override fun schedulePendingSyncAsync() {
        scope.launch {
            schedulePendingSync()
        }
    }

    /**
     * Rôle : Engager une écoute permanente sur le réseau grâce au Service de connectivité native.
     * 
     * Précondition : Un gestionnaire de connectivité valide.
     * Postcondition : Lance un check de tâches (`schedulePendingSyncAsync()`) et inscrit l'écouteur `networkCallback`.
     */
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
