package com.projetmobile.mobile.data.sync

/**
 * Rôle : Contrat d'interface définissant le mécanisme de planification de tâches de synchronisation en arrière-plan.
 * 
 * Précondition : N/A (Contrat d'implémentation).
 * Postcondition : Propose des méthodes pour programmer la résolution des envois API asynchrones via WorkManager.
 */
interface SyncWorkScheduler {
    /** 
     * Rôle : Lève explicitement, sur un scope asynchrone suspensif, une pile de requêtes WorkManager selon le cache.
     * 
     * Précondition : Contexte d'exécution de coroutine (suspend).
     * Postcondition : Les tâches sont allouées au queue worker.
     */
    suspend fun schedulePendingSync()
    
    /** 
     * Rôle : Déclenche le schedule sans obliger le thread appelant (ex : ViewModel ou Activity) à être au sein de coroutines.
     * 
     * Précondition : -
     * Postcondition : Crée ou réutilise le Job asynchrone pointant implicitement vers `schedulePendingSync()`.
     */
    fun schedulePendingSyncAsync()
    
    /** 
     * Rôle : Amorce ou déclenche toute routine initiale ou écoute d'état réseau du scheduler unique.
     * 
     * Précondition : Configuration basique effectuée à l'application.
     * Postcondition : Registres en cours.
     */
    fun start()
}

/**
 * Rôle : Fournir un accès Singleton global et abstrait à l'outil de planification de synchronisation (`SyncWorkScheduler`).
 * Évite les injections complexes (Hilt/Dagger) aux cœurs des `Repository`.
 * 
 * Précondition : La méthode `initialize()` doit valablement être appelée après la construction des dépendances dans l'App ou Main.
 * Postcondition : Permet l'envoi de signaux WorkManager via de simples fonctions objets statiques de substitution.
 */
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
