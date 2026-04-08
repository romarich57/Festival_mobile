package com.projetmobile.mobile.data.remote.common

import kotlinx.serialization.json.Json

/**
 * Rôle : Fournir une instance unique et configurée du désérialiseur JSON de Kotlinx Serialization.
 * Ceci standardise la manière dont les objets JSON reçus ou envoyés via l'API sont traités,
 * évitant une recréation constante de l'objet Json.
 * 
 * Précondition : La librairie kotlinx.serialization doit être incluse dans le projet.
 * Postcondition : Met à disposition [ApiJson.instance] tolérant aux clés inconnues et ignorant les valeurs nulles explicites si non nécessaires.
 */
object ApiJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
}
