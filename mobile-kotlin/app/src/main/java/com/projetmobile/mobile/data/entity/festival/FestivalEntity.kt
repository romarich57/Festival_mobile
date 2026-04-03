package com.projetmobile.mobile.data.entity.festival
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.projetmobile.mobile.data.entity.festival.FestivalSummary

/**
 * Table Room "festivals" — persistance locale pour le mode offline.
 *
 * Séparée de FestivalSummary (entité métier) et FestivalDto (réseau) :
 * chaque couche a sa propre représentation → principe S.
 */
@Entity(tableName = "festivals")
data class FestivalEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val startDate: String,
    val endDate: String,
    val stockTablesStandard: Int,
    val stockTablesGrande: Int,
    val stockTablesMairie: Int,
    val stockChaises: Int,
    val prixPrises: Double,
)