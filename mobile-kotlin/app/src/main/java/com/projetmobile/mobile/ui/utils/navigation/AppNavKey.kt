/**
 * Rôle : Décrit l'ensemble des routes (destinations de navigation) permises de l'application.
 * Les différents écrans y sont définis sous forme de `data object` et `data class` sérialisables pour passer des paramètres typés.
 * Précondition : Utilisé par la bibliothèque de navigation expérimentale (Navigation 3) propre au projet.
 * Postcondition : Garantit une navigation fortement typée, sans concaténation manuelle de paramètres d'URL (sécurité).
 */
package com.projetmobile.mobile.ui.utils.navigation

import androidx.navigation3.runtime.NavKey
import com.projetmobile.mobile.data.entity.auth.VerificationResultStatus
import kotlinx.serialization.Serializable

@Serializable
/**
 * Rôle : Définit le contrat du module navigation.
 */
sealed interface AppNavKey : NavKey

@Serializable
/**
 * Rôle : Expose un singleton de support pour le module navigation.
 */
data object Festivals : AppNavKey

@Serializable
/**
 * Rôle : Expose un singleton de support pour le module navigation.
 */
data object Reservants : AppNavKey

@Serializable
/**
 * Rôle : Expose un singleton de support pour le module navigation.
 */
data object ReservantCreate : AppNavKey

@Serializable
/**
 * Rôle : Décrit le composant réservant détails du module navigation.
 */
data class ReservantDetails(val reservantId: Int) : AppNavKey

@Serializable
/**
 * Rôle : Décrit le composant réservant édition du module navigation.
 */
data class ReservantEdit(val reservantId: Int) : AppNavKey

@Serializable
/**
 * Rôle : Décrit le composant réservant jeu création du module navigation.
 */
data class ReservantGameCreate(
    val reservantId: Int,
    val editorId: Int,
) : AppNavKey

@Serializable
/**
 * Rôle : Expose un singleton de support pour le module navigation.
 */
data object Games : AppNavKey

@Serializable
/**
 * Rôle : Expose un singleton de support pour le module navigation.
 */
data object GameCreate : AppNavKey

@Serializable
/**
 * Rôle : Décrit le composant jeu détails du module navigation.
 */
data class GameDetails(val gameId: Int) : AppNavKey

@Serializable
/**
 * Rôle : Décrit le composant jeu édition du module navigation.
 */
data class GameEdit(val gameId: Int) : AppNavKey

@Serializable
/**
 * Rôle : Expose un singleton de support pour le module navigation.
 */
data object Login : AppNavKey

@Serializable
/**
 * Rôle : Expose un singleton de support pour le module navigation.
 */
data object Register : AppNavKey

@Serializable
/**
 * Rôle : Expose un singleton de support pour le module navigation.
 */
data object Profile : AppNavKey

@Serializable
/**
 * Rôle : Expose un singleton de support pour le module navigation.
 */
data object Admin : AppNavKey

@Serializable
/**
 * Rôle : Décrit le composant administration utilisateur détail du module navigation.
 */
data class AdminUserDetail(val userId: Int) : AppNavKey

@Serializable
/**
 * Rôle : Expose un singleton de support pour le module navigation.
 */
data object AdminUserCreate : AppNavKey

@Serializable
/**
 * Rôle : Décrit le composant administration utilisateur édition du module navigation.
 */
data class AdminUserEdit(val userId: Int) : AppNavKey

@Serializable
/**
 * Rôle : Expose un singleton de support pour le module navigation.
 */
data object ForgotPassword : AppNavKey

@Serializable
/**
 * Rôle : Décrit le composant en attente verification du module navigation.
 */
data class PendingVerification(val email: String?) : AppNavKey

@Serializable
/**
 * Rôle : Décrit le composant verification result du module navigation.
 */
data class VerificationResult(val status: VerificationResultStatus) : AppNavKey

@Serializable
/**
 * Rôle : Décrit le composant réinitialisation mot de passe du module navigation.
 */
data class ResetPassword(val token: String?) : AppNavKey

@Serializable
/**
 * Rôle : Décrit le composant réservation dashboard du module navigation.
 */
data class ReservationDashboard(val festivalId: Int) : AppNavKey

@Serializable
/**
 * Rôle : Décrit le composant réservation formulaire du module navigation.
 */
data class ReservationForm(val festivalId: Int) : AppNavKey

@Serializable
/**
 * Rôle : Expose un singleton de support pour le module navigation.
 */
data object FestivalForm : AppNavKey

@Serializable
/**
 * Rôle : Décrit le composant réservation détails du module navigation.
 */
data class ReservationDetails(val reservationId: Int): AppNavKey

/**
 * Rôle : Décrit le composant top level onglet du module navigation.
 */
enum class TopLevelTab {
    Festivals,
    Reservants,
    Games,
    Login,
    Register,
    Profile,
    Admin,
}
