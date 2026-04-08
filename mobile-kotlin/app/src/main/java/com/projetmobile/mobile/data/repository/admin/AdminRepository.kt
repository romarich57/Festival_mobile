package com.projetmobile.mobile.data.repository.admin

import com.projetmobile.mobile.data.entity.admin.AdminUserCreateInput
import com.projetmobile.mobile.data.entity.admin.AdminUserUpdateInput
import com.projetmobile.mobile.data.entity.auth.AuthUser

/**
 * Rôle : Interface définissant la gestion et le pilotage des comptes utilisateurs 
 * par un administrateur (CRUD sur les rôles, permissions, identités).
 * 
 * Précondition : Le profil appelant doit bénéficier des habilitations "Admin".
 * Postcondition : Offre un panel d'opérations asynchrones (`Result`) de lecture et d'écriture.
 */
interface AdminRepository {
    
    /**
     * Rôle : Récupérer la collection intégrale de tous les utilisateurs inscrits.
     * 
     * Précondition : Session admin valide.
     * Postcondition : Retourne la liste complète modélisée sous forme [AuthUser].
     */
    suspend fun getUsers(): Result<List<AuthUser>>
    
    /**
     * Rôle : Obtenir la fiche détaillée d'un utilisateur spécifique.
     * 
     * Précondition : Fournir l'identifiant exact de l'utilisateur.
     * Postcondition : Cible unique restituée si trouvée.
     */
    suspend fun getUserById(id: Int): Result<AuthUser>
    
    /**
     * Rôle : Enregistrer de force (par l'administration) un nouvel utilisateur dans le système.
     * 
     * Précondition : Un [AdminUserCreateInput] comportant login, mot de passe et rôle.
     * Postcondition : Le backend génère le profil, et retourne un message de confirmation.
     */
    suspend fun createUser(input: AdminUserCreateInput): Result<String>
    
    /**
     * Rôle : Modifier ou écraser les attributs d'un utilisateur préexistant.
     * 
     * Précondition : Un ID correct et un payload [AdminUserUpdateInput] valide.
     * Postcondition : Le profil cible est persisté côté backend et sa nouvelle version est ramenée.
     */
    suspend fun updateUser(id: Int, input: AdminUserUpdateInput): Result<AuthUser>
    
    /**
     * Rôle : Demander la destruction définitive d'un profil distant.
     * 
     * Précondition : Confirmer l'ID du compte.
     * Postcondition : Profil annihilé avec mention explicite renvoyée.
     */
    suspend fun deleteUser(id: Int): Result<String>
}
