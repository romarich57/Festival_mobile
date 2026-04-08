/**
 * Rôle : Fichier clé de l'architecture gérant l'Injection de Dépendances Manuelle.
 * La classe `AppContainer` a pour rôle d'instancier et de stocker les différents 
 * composants uniques (Singletons) de l'application : base de données, 
 * gestionnaires de préférences (SharedPreferences), client réseau HTTP (OkHttp et Retrofit), 
 * et tous les "Repositories" métier. 
 * Précondition : Aucune.
 * Postcondition : Son utilisation permet d'avoir un accès centralisé et persistant (grâce à `lazy`)
 * aux objets fondamentaux sans qu'ils ne soient recréés.
 */
package com.projetmobile.mobile

import android.content.Context
import com.projetmobile.mobile.data.database.AppDatabase
import com.projetmobile.mobile.data.database.SyncPreferenceStore
import com.projetmobile.mobile.data.remote.admin.AdminApiService
import com.projetmobile.mobile.data.remote.auth.AuthApiService
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.remote.festival.FestivalApiService
import com.projetmobile.mobile.data.remote.games.GamesApiService
import com.projetmobile.mobile.data.remote.profile.ProfileApiService
import com.projetmobile.mobile.data.remote.reservants.ReservantsApiService
import com.projetmobile.mobile.data.database.AuthPreferenceStore
import com.projetmobile.mobile.data.database.PersistentCookieJar
import com.projetmobile.mobile.data.remote.auth.AuthRefreshInterceptor
import com.projetmobile.mobile.data.remote.reservation.ReservationApiService
import com.projetmobile.mobile.data.remote.zoneplan.ZonePlanApiService
import com.projetmobile.mobile.data.repository.admin.AdminRepository
import com.projetmobile.mobile.data.repository.admin.AdminRepositoryImpl
import com.projetmobile.mobile.data.repository.auth.AuthRepository
import com.projetmobile.mobile.data.repository.auth.AuthRepositoryImpl
import com.projetmobile.mobile.data.repository.festival.FestivalRepository
import com.projetmobile.mobile.data.repository.festival.FestivalRepositoryImpl
import com.projetmobile.mobile.data.repository.games.GamesRepository
import com.projetmobile.mobile.data.repository.games.GamesRepositoryImpl
import com.projetmobile.mobile.data.repository.profile.ProfileRepository
import com.projetmobile.mobile.data.repository.profile.ProfileRepositoryImpl
import com.projetmobile.mobile.data.repository.reservation.ReservationRepository
import com.projetmobile.mobile.data.repository.reservation.ReservationRepositoryImpl
import com.projetmobile.mobile.data.repository.reservants.ReservantsRepository
import com.projetmobile.mobile.data.repository.reservants.ReservantsRepositoryImpl
import com.projetmobile.mobile.data.repository.workflow.WorkflowRepository
import com.projetmobile.mobile.data.repository.workflow.WorkflowRepositoryImpl
import com.projetmobile.mobile.data.repository.zonePlan.ZonePlanRepository
import com.projetmobile.mobile.data.repository.zonePlan.ZonePlanRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Rôle : Conteneur d'injection de dépendances principal.
 * Prend en charge la création des services de domaine pour fournir une instance partagée 
 * par fonctionnalité à travers toute l'application.
 * Précondition : Le contexte applicatif Android doit être fourni pour accéder aux systèmes locaux (Base de données et Préférences).
 * Postcondition : Permet l'instanciation et l'accès centralisé aux dépendances.
 */
class AppContainer(context: Context) {
    // Récupération sécurisée du contexte d'application (qui survit aux destructions d'activité).
    private val applicationContext = context.applicationContext

    // ── Base de données locale (SSOT offline-first) ──────────────────────────

    // Initialisation paresseuse de l'instance unique (Singleton) de la base de données Room.
    private val appDatabase by lazy {
        AppDatabase.getInstance(applicationContext)
    }

    // Instance unique pour accéder aux préférences liées à la synchronisation en tâche de fond.
    private val syncPreferenceStore by lazy {
        SyncPreferenceStore(applicationContext)
    }

    // ── Auth preferences ─────────────────────────────────────────────────────

    // Instance unique pour lire/écrire les informations de session/auth (jeton utilisateur, données du profil).
    private val authPreferenceStore by lazy {
        AuthPreferenceStore(applicationContext)
    }

    // Intercepteur de requêtes HTTP chargé d'écrire les logs complets de chaque appel réseau (Body, Headers).
    // Restreint en fonction du niveau de configuration de compilation (Debug/Release).
    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                // Affiche l'intégralité de la requête et de la réponse en phase de débat / développement (Debug).
                HttpLoggingInterceptor.Level.BODY
            } else {
                // Version asceptisée et limitative en production pour la sécurité.
                HttpLoggingInterceptor.Level.BASIC
            }
        }
    }

    // Gestionnaire de persistance des cookies HTTP, afin de conserver les sessions d'API ouvertes.
    private val cookieJar by lazy {
        PersistentCookieJar(applicationContext)
    }

    // Intercepteur réseau en charge de renouveler (refresh) automatiquement un token expiré
    // si un code 401 ou 403 est reçu (sécurité de session continue).
    private val authRefreshInterceptor by lazy {
        AuthRefreshInterceptor(
            baseUrl = BuildConfig.API_BASE_URL,
            cookieJar = cookieJar,
        )
    }

    // Création du client OkHttpClient configuré avec le mécanisme de rafraichissement,
    // de journalisation des requêtes et de gestion de cookie persistant.
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authRefreshInterceptor) // Ajout de la gestion automatique des tokens expiré.
            .addInterceptor(loggingInterceptor)     // Ajout des logs HTTP.
            .cookieJar(cookieJar)                   // Ajout du maintien des cookies entre appels.
            .build()
    }

    // Création de l'instance unique Retrofit qui va traduire les interfaces Kotlin en vraies requêtes HTTP REST.
    private val retrofit by lazy {
        Retrofit.Builder()
            // URL de base de l'API tirée du BuildConfig lié aux variables d'environnement distantes
            .baseUrl(BuildConfig.API_BASE_URL)
            // Client OkHttp configuré contenant tous nos paramètres de sécurité et persistance
            .client(httpClient)
            // Utilisation du convertisseur JSON (Kotlinx Serialization Factory) pour sérialiser / désérialiser 
            // les entités JSON réseau vers des objets type Data Class Kotlin
            .addConverterFactory(
                ApiJson.instance.asConverterFactory("application/json".toMediaType()),
            )
            .build()
    }

    // =========================================================================
    // SECTION : API Services (Déclarations des interfaces Retrofit)
    // Instanciation de l'interface qui expose des requêtes backend liées à l'authentification.
    // =========================================================================

    private val authApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    // Api liée à la gestion et fetch des Festivals.
    val festivalApiService by lazy {
        retrofit.create(FestivalApiService::class.java)
    }

    // Api liée à la récupération et la sauvegarde des profils d'utilisateurs.
    private val profileApiService by lazy {
        retrofit.create(ProfileApiService::class.java)
    }

    // Api liée aux objets des jeux du festival.
    val gamesApiService by lazy {
        retrofit.create(GamesApiService::class.java)
    }

    // Api gérant les appel REST liés aux réservants/exposants du festival.
    val reservantsApiService by lazy {
        retrofit.create(ReservantsApiService::class.java)
    }

    // Api liée aux accès ou modification de configurations Admin du festival.
    private val adminApiService by lazy {
        retrofit.create(AdminApiService::class.java)
    }

    // Api traitant de la logique des réservations sur stand / événementielles.
    val reservationApiService: ReservationApiService by lazy {
        retrofit.create(ReservationApiService::class.java)
    }

    // Api qui récupère la cartographie ou les plans des zones du festival.
    val zonePlanApiService: ZonePlanApiService by lazy {
        retrofit.create(ZonePlanApiService::class.java)
    }


    // =========================================================================
    // SECTION : Repositories (Dépôts métier de l'application)
    // Ces objets récupèrent les instances `ApiService` et `DAO` (base de données) pertinentes.
    // C'est la couche métier exposée aux ViewModels via l'injection.
    // =========================================================================

    // Dépôt s'occupant des processus d'inscription et de login.
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            authApiService = authApiService,               // API pour contacter le serveur Web.
            authPreferenceStore = authPreferenceStore,     // Préférence locale pour récupérer le jeton hors-ligne.
        )
    }

    // Dépôt dédié aux opérations liées aux festivals existants.
    val festivalRepository: FestivalRepository by lazy {
        FestivalRepositoryImpl(
            festivalApiService = festivalApiService,       // Requêtes distantes des données du festival
            festivalDao = appDatabase.festivalDao(),       // Lecture/Ecriture locale (cache database via Room)
            syncPreferenceStore = syncPreferenceStore,     // Métadonnées liées à la fraîcheur des données pour les Background Worker.
        )
    }

    // Dépôt lié au compte profil de l'utilisateur actif.
    val profileRepository: ProfileRepository by lazy {
        ProfileRepositoryImpl(
            profileApiService = profileApiService,
            authApiService = authApiService,
        )
    }

    // Dépôt assurant les traitements sur des données des jeux / sessions du festival.
    val gamesRepository: GamesRepository by lazy {
        GamesRepositoryImpl(
            gamesApiService = gamesApiService,             // Lecture Remote
            gameDao = appDatabase.gameDao(),               // Persistance DB Room locale
            syncPreferenceStore = syncPreferenceStore,
        )
    }

    // Dépôt gérant les traitements pour les entités des réservants.
    val reservantsRepository: ReservantsRepository by lazy {
        ReservantsRepositoryImpl(
            reservantsApiService = reservantsApiService,   // Lecture Remote
            reservantDao = appDatabase.reservantDao(),     // Persistance DB Room locale
            syncPreferenceStore = syncPreferenceStore,
        )
    }

    // Dépôt gérant les actions d'Administration.
    val adminRepository: AdminRepository by lazy {
        AdminRepositoryImpl(adminApiService)
    }

    // Dépôt lié au Workflow des tables de réservations (Création, liste, modification) 
    // Il mixe API distante et persistance locale.
    val reservationRepository: ReservationRepository by lazy {
        ReservationRepositoryImpl(
            api = reservationApiService,
            reservationDao = appDatabase.reservationDao(),
            syncPreferenceStore = syncPreferenceStore,
        )
    }

    // Dépôt métier spécifique pour les flux de processus internes (Workflow).
    val workflowRepository: WorkflowRepository by lazy {
        WorkflowRepositoryImpl(reservationApiService)
    }

    // Dépôt lié aux objets du Plan et des agencements de Zone de l'application.
    val zonePlanRepository: ZonePlanRepository by lazy {
        ZonePlanRepositoryImpl(
            zonePlanApiService
        )
    }
}
