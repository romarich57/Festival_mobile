package com.projetmobile.mobile.data.room

import androidx.room.TypeConverter
import com.projetmobile.mobile.data.entity.games.MechanismOption
import com.projetmobile.mobile.data.remote.common.ApiJson
import kotlinx.serialization.builtins.ListSerializer

/**
 * Rôle : Définit les TypeConverters pour Room, ce qui permet de stocker
 * des types de données que Room ne gère pas nativement (telle qu'une liste
 * d'objets [MechanismOption]), le tout converti en format JSON.
 *
 * Précondition : RoomConverters doit être annoté `@TypeConverters` dans la classe [AppDatabase].
 * L'outil utilise [ApiJson.instance] pré-existante afin d'éviter la redondance d'un objet JSON.
 * Postcondition : Sérialisation et Désérialisation transparentes entre la mémoire vive
 * de l'appli et le format textuel interne à SQLite.
 */
class RoomConverters {

    /**
     * Rôle : Transforme une liste complexe d'options de mécanismes en une simple chaîne JSON.
     * 
     * Précondition : Fournir une liste (même vide) d'objets MechanismOption.
     * Postcondition : Retourne la liste encodée en format String stockable par Room.
     */
    @TypeConverter
    fun mechanismsToJson(mechanisms: List<MechanismOption>): String =
        ApiJson.instance.encodeToString(
            ListSerializer(MechanismOption.serializer()),
            mechanisms,
        )

    /**
     * Rôle : Re-transforme une chaîne JSON sauvegardée en base locale vers une liste de mechanisms.
     * 
     * Précondition : La chaîne JSON correspond bien à la sérialisation attendue.
     * Postcondition : Renvoie l'objet en mémoire List<MechanismOption>. Les vides donneront null ou emptyList.
     */
    @TypeConverter
    fun jsonToMechanisms(json: String): List<MechanismOption> =
        if (json.isBlank() || json == "[]") emptyList()
        else ApiJson.instance.decodeFromString(
            ListSerializer(MechanismOption.serializer()),
            json,
        )
}
