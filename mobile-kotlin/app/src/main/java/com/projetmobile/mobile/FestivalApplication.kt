/**
 * Rôle : Classe point d'entrée principal de l'application Android.
 * Elle a pour rôle d'initialiser les composants de base de l'application (injection de dépendances, 
 * vérificateur de connexion réseau, et la synchronisation des données en arrière-plan).
 * Précondition : Le système Android lance cette classe au démarrage de l'application.
 * Postcondition : Les composants globaux essentiels au fonctionnement de l'app sont initialisés et prêts.
 */
package com.projetmobile.mobile

import android.app.Application
import com.projetmobile.mobile.data.repository.AndroidNetworkStatusProvider
import com.projetmobile.mobile.data.repository.RepositoryNetworkStatus
import com.projetmobile.mobile.data.database.AppDatabase
import com.projetmobile.mobile.data.sync.AndroidSyncWorkScheduler
import com.projetmobile.mobile.data.sync.RepositorySyncScheduler

/**
 * Rôle : Classe principale de l'application Festival.
 * Hérite d'Application pour configurer les propriétés globales dès l'amorçage.
 * Précondition : Doit être enregistrée dans le manifeste Android (`AndroidManifest.xml`).
 * Postcondition : Initialise et fournit l'accès à `AppContainer` ainsi qu'aux synchroniseurs.
 */
class FestivalApplication : Application() {
    
    // Conteneur d'injection de dépendances manuel pour fournir les instances dans toute l'application.
    lateinit var appContainer: AppContainer
: Initialise les composants critiques de l'application lors de sa création.
     * Cette méthode configure le gestionnaire de réseau, l'injection de dépendances, 
     * et la synchronisation de données en fond.
     * Précondition : Le contexte Android de base est disponible pour l'appel à `super.onCreate()`.
     * Postcondition : `AppContainer` et `RepositorySyncScheduler` sont initialisés et démarrés
     * @param Aucun paramètre n'est reçu.
     * @return Ne renvoie rien (Unit).
     */
    override fun onCreate() {
        // Appel de la méthode parente pour initialiser le contexte Android de base.
        super.onCreate()
        
        // Initialisation du provider de l'état du réseau (pour savoir si on est en ligne/hors ligne)
        // en lui fournissant le contexte global de l'application ('this').
        RepositoryNetworkStatus.initialize(AndroidNetworkStatusProvider(this))
        
        // Initialisation de notre conteneur de dépendances (AppContainer) contenant nos repositories et APIs.
        appContainer = AppContainer(this)
        
        // Configuration de la synchronisation de données en arrière-plan en lui fournissant :
        // 1. Le contexte global de l'application.
        // 2. L'instance Singleton de la base de données Room (AppDatabase).
        RepositorySyncScheduler.initialize(
            AndroidSyncWorkScheduler(
                context = this,
                database = AppDatabase.getInstance(this),
            ),
        )
        
        // Démarrage de la planification de la synchronisation des données (ex: fetch de fond, notifications de mise à jour).
        RepositorySyncScheduler.start()
    }
}
