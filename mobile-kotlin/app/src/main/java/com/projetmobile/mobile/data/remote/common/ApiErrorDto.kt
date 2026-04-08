package com.projetmobile.mobile.data.remote.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Rôle : Représente le format standard de l'objet d'erreur renvoyé par l'API Backend.
 * (Data Transfer Object - DTO). Permet de désérialiser proprement le body d'une réponse
 * HTTP ayant échoué (ex: 400 Bad Request, 500 Internal Server Error).
 * 
 * Précondition : Une requête réseau a retourné un code d'erreur contenant un body JSON.
 * Postcondition : Fournit un objet Kotlin manipulable avec un potentiel message lisible.
 */
@Serializable
data class ApiErrorDto(
    val error: String? = null,
    val message: String? = null,
    val details: JsonElement? = null,
)
