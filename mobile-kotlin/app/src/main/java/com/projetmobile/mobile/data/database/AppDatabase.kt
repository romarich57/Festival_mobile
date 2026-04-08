/**
 * Rôle du fichier :
 * Ce fichier définit et instancie la base de données SQLite locale de l'application via la librairie Room.
 * Il agit comme la "Source de Vérité Unique" (Single Source of Truth - SSOT) de l'application, 
 * garantissant que les écrans (interface utilisateur) s'alimentent exclusivement des données locales 
 * pour le mode "offline-first".
 */
package com.projetmobile.mobile.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.projetmobile.mobile.data.dao.FestivalDao
import com.projetmobile.mobile.data.dao.GameDao
import com.projetmobile.mobile.data.dao.ReservantDao
import com.projetmobile.mobile.data.dao.ReservationDao
import com.projetmobile.mobile.data.room.FestivalRoomEntity
import com.projetmobile.mobile.data.room.GameRoomEntity
import com.projetmobile.mobile.data.room.ReservantRoomEntity
import com.projetmobile.mobile.data.room.ReservationRoomEntity
import com.projetmobile.mobile.data.room.RoomConverters

/**
 * Rôle : Base de données Room — Source de Vérité Unique (SSOT) pour l'architecture offline-first.
 * L'annotation @Database enregistre toutes les entités (tables) de la base.
 *
 * Précondition : Les entités listées doivent être des classes annotées avec @Entity.
 * Postcondition : Instancie une base de données SQLite configurée avec les entités et convertisseurs fournis.
 */
@Database(
    entities = [
        GameRoomEntity::class,
        ReservantRoomEntity::class,
        FestivalRoomEntity::class,
        ReservationRoomEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
// TypeConverters s'applique globalement à la base de données : ces classes aident Room
// à comprendre comment sérialiser / désérialiser des objets complexes (ex: Dates, Listes) d'une colonne SQL.
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {

    // =========================================================================
    // SECTION : Définitions des DAOs (Data Access Objects)
    // Interfaces listées permettant l'interaction (Requêtes SQL/CRUD) avec chaque entité.
    // =========================================================================

    /** Retourne l'objet d'accès local pour la table 'games'. */
    abstract fun gameDao(): GameDao
    
    /** Retourne l'objet d'accès local pour la table 'reservants'. */
    abstract fun reservantDao(): ReservantDao
    
    /** Retourne l'objet d'accès local pour l'entité 'festivals' de Room. */
    abstract fun festivalDao(): FestivalDao
    
    /** Retourne l'objet pour requêter et modifier localement les 'reservations'. */
    abstract fun reservationDao(): ReservationDao

    /**
     * Objet compagnon statique utilisé pour héberger les migrations de requêtes SQL ainsi que 
     * le pattern de création "Singleton" de la base.
     */
    companion object {
        
        /**
         * Rôle : Gérer la transition structurelle de la base de données lors d'une montée de version
         * de la version 1 vers la version 2 sans perte des données utilisateur.
         * Précondition : La base de données locale actuelle doit être en version 1.
         * Postcondition : Les colonnes de synchronisation sont ajoutées et les données existantes sont rétrocompatibles.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(database: SupportSQLiteDatabase) {
                // GAMES: Ajout de colonnes de synchronisation dans la table (retryAction et message d'erreur).
                database.execSQL("ALTER TABLE games ADD COLUMN retryAction TEXT")
                database.execSQL("ALTER TABLE games ADD COLUMN lastSyncErrorMessage TEXT")
                // Mise à jour rétroactive des valeurs de retryAction à partir de l'ancien champ 'syncStatus'.
                database.execSQL(
                    """
                    UPDATE games
                    SET retryAction = CASE syncStatus
                        WHEN 'PENDING_CREATE' THEN 'CREATE'
                        WHEN 'PENDING_UPDATE' THEN 'UPDATE'
                        WHEN 'PENDING_DELETE' THEN 'DELETE'
                        ELSE NULL
                    END
                    """.trimIndent(),
                )

                // RESERVANTS: Altération et migration identique pour la table des exposants/reservants.
                database.execSQL("ALTER TABLE reservants ADD COLUMN retryAction TEXT")
                database.execSQL("ALTER TABLE reservants ADD COLUMN lastSyncErrorMessage TEXT")
                database.execSQL(
                    """
                    UPDATE reservants
                    SET retryAction = CASE syncStatus
                        WHEN 'PENDING_CREATE' THEN 'CREATE'
                        WHEN 'PENDING_UPDATE' THEN 'UPDATE'
                        WHEN 'PENDING_DELETE' THEN 'DELETE'
                        ELSE NULL
                    END
                    """.trimIndent(),
                )

                // RESERVATIONS: Altération et migration identique.
                database.execSQL("ALTER TABLE reservations ADD COLUMN retryAction TEXT")
                database.execSQL("ALTER TABLE reservations ADD COLUMN lastSyncErrorMessage TEXT")
                database.execSQL(
                    """
                    UPDATE reservations
                    SET retryAction = CASE syncStatus
                        WHEN 'PENDING_CREATE' THEN 'CREATE'
                        WHEN 'PENDING_UPDATE' THEN 'UPDATE'
                        WHEN 'PENDING_DELETE' THEN 'DELETE'
                        ELSE NULL
                    END
                    """.trimIndent(),
                )

                // FESTIVALS: Moins de conditions de retry (seulement DELETE pris en compte ici).
                database.execSQL("ALTER TABLE festivals ADD COLUMN retryAction TEXT")
                database.execSQL("ALTER TABLE festivals ADD COLUMN lastSyncErrorMessage TEXT")
                database.execSQL(
                    """
                    UPDATE festivals
                    SET retryAction = CASE syncStatus
                        WHEN 'PENDING_DELETE' THEN 'DELETE'
                        ELSE NULL
                    END
                    """.trimIndent(),
                )
            }
        }

        // L'annotation @Volatile garantit que la variable est lue depuis la mémoire principale, 
        // ce qui rend sa visibilité immédiate aux autres threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Rôle : Implémenter le design pattern "Singleton Thread-Safe" pour empêcher qu'il y ait plusieurs
         * connexions ouvertes vers la base de données simultanément dans l'application.
         *
         * Précondition : Le contexte d'application doit être valide.
         * Postcondition : Retourne l'instance unique de AppDatabase, la créant si elle n'existait pas encore.
         */
        fun getInstance(context: Context): AppDatabase {
            // Si INSTANCE n'est pas nulle on la retourne directement. (Visitable dès le premier IF)
            return INSTANCE ?: synchronized(this) {
                // Bloc synchronisé pour éviter la condition de course (Race condition).
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "festival_app.db", // Nom du fichier SQLite sur le disque du téléphone.
                )
                    .addMigrations(MIGRATION_1_2) // Attache le script de migration défini plus haut.
                    .build()
                    .also { INSTANCE = it } // Assigne la valeur à notre Singleton avant de le rendre.
            }
        }
    }
}
