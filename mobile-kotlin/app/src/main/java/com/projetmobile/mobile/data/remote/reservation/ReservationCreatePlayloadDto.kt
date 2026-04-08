package com.projetmobile.mobile.data.remote.reservation

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rôle : Formulaire réseau (DTO) expédié à l'API afin d'instancier un lien formel de réservation.
 * Mixte les données propres au contact (réservant) et au festival, combinées 
 * au besoin brut en prises électriques, tout en incluant le support natif de 
 * champs vides sérialisés à zéro par défaut (@EncodeDefault).
 * 
 * Précondition : `festivalId` et identité (`name`, `email`, `type`) sont obligatoires.
 * Postcondition : Produit la chaine JSON adéquate convertie pour la requête HTTP POST.
 */
@Serializable
@OptIn(ExperimentalSerializationApi::class) // Nécessaire pour @EncodeDefault
data class ReservationCreatePayloadDto(
    @SerialName("reservant_name") val reservantName: String,
    @SerialName("reservant_email") val reservantEmail: String,
    @SerialName("reservant_type") val reservantType: String,
    @SerialName("reservant_id") val reservantId: Int? = null,
    @SerialName("festival_id") val festivalId: Int,
    @EncodeDefault @SerialName("start_price") val startPrice: Double = 0.0,
    @EncodeDefault @SerialName("nb_prises") val nbPrises: Int = 0,
    @EncodeDefault @SerialName("final_price") val finalPrice: Double = 0.0
)