package com.projetmobile.mobile.data.room

import androidx.room.TypeConverter
import com.projetmobile.mobile.data.entity.games.MechanismOption
import com.projetmobile.mobile.data.remote.common.ApiJson
import kotlinx.serialization.builtins.ListSerializer

/**
 * TypeConverters Room pour les types complexes.
 *
 * Actuellement, les entités Room stockent les listes complexes en JSON String brut,
 * donc ces converters sont utilisés par les mappers plutôt que directement par Room.
 * La classe est déclarée en @TypeConverters dans AppDatabase pour extensibilité future.
 *
 * Réutilise [ApiJson.instance] déjà présent dans le projet.
 */
class RoomConverters {

    @TypeConverter
    fun mechanismsToJson(mechanisms: List<MechanismOption>): String =
        ApiJson.instance.encodeToString(
            ListSerializer(MechanismOption.serializer()),
            mechanisms,
        )

    @TypeConverter
    fun jsonToMechanisms(json: String): List<MechanismOption> =
        if (json.isBlank() || json == "[]") emptyList()
        else ApiJson.instance.decodeFromString(
            ListSerializer(MechanismOption.serializer()),
            json,
        )
}
