package com.projetmobile.mobile.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
 * Version 1 : Games, Réservants, Festivals, Réservations.
 * En cas de migration non gérée, [fallbackToDestructiveMigration] recrée la base.
 */
@Database(
    entities = [
        GameRoomEntity::class,
        ReservantRoomEntity::class,
        FestivalRoomEntity::class,
        ReservationRoomEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao
    abstract fun reservantDao(): ReservantDao
    abstract fun festivalDao(): FestivalDao
    abstract fun reservationDao(): ReservationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "festival_app.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
