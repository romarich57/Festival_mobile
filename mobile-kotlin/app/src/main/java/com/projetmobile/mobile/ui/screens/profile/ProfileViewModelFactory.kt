/**
 * Rôle : Factory chargeant de créer et instancier correctement le ViewModel du profil.
 *
 * Précondition : Les repositories d'authentification et profil doivent exister.
 *
 * Postcondition : Initialise le cycle de vie du `ProfileViewModel` en l'injectant.
 */
package com.projetmobile.mobile.ui.screens.profile

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.repository.profile.ProfileRepository

/**
 * Rôle : Exécute l'action profil vue modèle factory du module le profil.
 *
 * Précondition : L'état UI et les callbacks ou dépendances nécessaires doivent être disponibles.
 *
 * Postcondition : Une instance correctement configurée est retournée.
 */
internal fun profileViewModelFactory(
    profileRepository: ProfileRepository,
    initialUser: AuthUser?,
): ViewModelProvider.Factory {
    return viewModelFactory {
        initializer {
            ProfileViewModel(
                profileRepository = profileRepository,
                initialUser = initialUser,
            )
        }
    }
}
