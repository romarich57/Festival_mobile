package com.projetmobile.mobile.data.database

import android.content.Context
import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
 * Base de données Room — Source de Vérité Unique (SSOT) pour l'architecture offline-first.
 *
 * Toutes les données affichées par l'UI proviennent de cette base.
 * Le réseau ne fait que mettre à jour cette base, jamais l'UI directement.
 *
 * Version 2 : métadonnées de retry/error pour fiabiliser la synchronisation.
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
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao
    abstract fun reservantDao(): ReservantDao
    abstract fun festivalDao(): FestivalDao
    abstract fun reservationDao(): ReservationDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE games ADD COLUMN retryAction TEXT")
                database.execSQL("ALTER TABLE games ADD COLUMN lastSyncErrorMessage TEXT")
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

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "festival_app.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
