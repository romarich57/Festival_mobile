package com.projetmobile.mobile.ui.screens.admin.catalog

import com.projetmobile.mobile.data.entity.auth.AuthUser

enum class AdminUserSortOption(val label: String) {
    CreatedAtDesc("Date de création"),
    NameAsc("Nom"),
    RoleAsc("Rôle"),
}

enum class AdminEmailFilter(val label: String) {
    All("Tous les statuts"),
    Verified("Vérifié"),
    NotVerified("Non vérifié"),
}

/**
 * Rôle : Décrit l'ensemble des administrateurs et membres filtrables, listés ou en chargement.
 *
 * Précondition : Met à jour par [AdminCatalogViewModel].
 *
 * Postcondition : Unifie les données brutes sur la session de catalogue de membres.
 */
data class AdminCatalogUiState(
    val allUsers: List<AuthUser> = emptyList(),
    val filteredUsers: List<AuthUser> = emptyList(),
    val searchQuery: String = "",
    val roleFilter: String? = null,
    val emailFilter: AdminEmailFilter = AdminEmailFilter.All,
    val sortOption: AdminUserSortOption = AdminUserSortOption.CreatedAtDesc,
    val sortAscending: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val pendingDeletion: AuthUser? = null,
    val deletingUserId: Int? = null,
    val updatingRoleForUserId: Int? = null,
) {
    val totalCount: Int get() = allUsers.size
    val verifiedCount: Int get() = allUsers.count { it.emailVerified }
    val notVerifiedCount: Int get() = allUsers.count { !it.emailVerified }
    val adminCount: Int get() = allUsers.count { it.role == "admin" }
}

internal fun AdminCatalogUiState.withAppliedFilters(): AdminCatalogUiState {
    var result = allUsers

    if (searchQuery.isNotBlank()) {
        val q = searchQuery.trim().lowercase()
        result = result.filter { user ->
            user.login.lowercase().contains(q) ||
                user.email.lowercase().contains(q) ||
                "${user.firstName} ${user.lastName}".lowercase().contains(q)
        }
    }

    if (roleFilter != null) {
        result = result.filter { it.role == roleFilter }
    }

    result = when (emailFilter) {
        AdminEmailFilter.Verified -> result.filter { it.emailVerified }
        AdminEmailFilter.NotVerified -> result.filter { !it.emailVerified }
        AdminEmailFilter.All -> result
    }

    result = when (sortOption) {
        AdminUserSortOption.CreatedAtDesc -> result.sortedByDescending { it.createdAt }
        AdminUserSortOption.NameAsc -> result.sortedBy { "${it.firstName} ${it.lastName}" }
        AdminUserSortOption.RoleAsc -> result.sortedBy { it.role }
    }

    if (sortAscending && sortOption == AdminUserSortOption.CreatedAtDesc) {
        result = result.reversed()
    }

    return copy(filteredUsers = result)
}
