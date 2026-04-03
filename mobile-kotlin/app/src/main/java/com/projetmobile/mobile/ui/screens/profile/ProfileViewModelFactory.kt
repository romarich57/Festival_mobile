package com.projetmobile.mobile.ui.screens.profile

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.repository.profile.ProfileRepository

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
