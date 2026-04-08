package com.projetmobile.mobile.data.repository.auth

import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.entity.auth.RegisterAccountInput

/**
 * Rôle : Définit toutes les interactions métier liées à l'authentification et à la 
 * gestion de la session utilisateur (inscription, connexion, réinitialisation, état).
 * 
 * Précondition : Appelé majoritairamente depuis les ViewModels d'authentification.
 * Postcondition : Délivre un bloc `Result` standardisant le succès ou l'échec de la requête.
 */
interface AuthRepository {
    
    /**
     * Rôle : Authentifier un profil en comparant son identifiant (email/login) et mot de passe.
     * 
     * Précondition : Format d'inputs valides depuis le formulaire.
     * Postcondition : Ramène les données du `AuthUser` si connexion validée, enregistre le cache.
     */
    suspend fun login(identifier: String, password: String): Result<AuthUser>

    /**
     * Rôle : Demander la création d'un compte inédit via les identités fournies.
     * 
     * Précondition : Champs vérifiés côté UI et mail non existant.
     * Postcondition : Retourne un message stipulant l'envoi de l'email de vérification.
     */
    suspend fun register(input: RegisterAccountInput): Result<String>

    /**
     * Rôle : Provoque le renvoi par le serveur de l'email d'activation du compte.
     * 
     * Précondition : Compte à l'étape "Non validé".
     * Postcondition : Confirmation d'envoi renvoyée en string.
     */
    suspend fun resendVerification(email: String): Result<String>

    /**
     * Rôle : Initialise la procédure formelle "Mot de passe oublié" en envoyant un lien.
     * 
     * Précondition : Email associé à un compte reconnu en DB.
     * Postcondition : Informe de la bonne réalisation de la demande.
     */
    suspend fun requestPasswordReset(email: String): Result<String>

    /**
     * Rôle : Fournit un nouveau mode de passe en utilisant le jeton de secours reçu par mail.
     * 
     * Précondition : Le code de reset `token` est valide et non expiré.
     * Postcondition : Mot de passe écrasé par le nouveau, profil prêt à la reconnexion.
     */
    suspend fun resetPassword(token: String, password: String): Result<String>

    /**
     * Rôle : Invalide formellement la session des deux côtés (serveur + base locale interne mobile).
     * 
     * Précondition : L'utilisateur est toujours loggué dans l'appli.
     * Postcondition : Cookies et cache supprimés.
     */
    suspend fun logout(): Result<String>

    /**
     * Rôle : Tente de relancer et vérifier la session active de l'utilisateur stockée (Cold Start).
     * 
     * Précondition : Jetons potentiellement stockés en cookie HttpOnly.
     * Postcondition : Rapatrie le profil ou donne Null si la session n'est plus valable.
     */
    suspend fun restoreSession(): Result<AuthUser?>

    /**
     * Rôle : Appelle le backend pour rafraichir les traits de la session sécurisée existante.
     * 
     * Précondition : Session supposément active.
     * Postcondition : Renouvelle les informations `AuthUser` et cache synchronisé.
     */
    suspend fun getCurrentUser(): Result<AuthUser>

    /**
     * Rôle : Récupère au besoin le mail du dernier inscrit pour fluidifier la saisie dans l'UI.
     * 
     * Précondition : Caching local via DataStore `authPreferenceStore` ok.
     * Postcondition : Le string de l'email, s'il figure en mémoire.
     */
    suspend fun getPendingVerificationEmail(): String?

    /**
     * Rôle : Pousse le mail d'une requête d'inscription dans le cache interne permanent.
     * 
     * Précondition : L'inscription vient d'avoir lieu.
     * Postcondition : Le contexte peut réutiliser ce mail (ex: vue Validation).
     */
    suspend fun setPendingVerificationEmail(email: String)

    /**
     * Rôle : Nettoie l'email mis en attente suite à un processus achevé ou annulé.
     * 
     * Précondition : Aucune.
     * Postcondition : Champ interne du DataStore remis à vide/null.
     */
    suspend fun clearPendingVerificationEmail()

    /**
     * Rôle : Lis l'identifiant pour pré-remplir le champ User ID (mémoire de la dernière session).
     * 
     * Précondition : DataStore accessible.
     * Postcondition : Le login/email mémorisé.
     */
    suspend fun getLastLoginIdentifier(): String?

    /**
     * Rôle : Mémorise l'ID à peine réussi ou tenté dans la page de Login.
     * 
     * Précondition : Chaine de caractère non nulle.
     * Postcondition : Retenu par les préférences locales de l'appareil.
     */
    suspend fun setLastLoginIdentifier(identifier: String)
}
