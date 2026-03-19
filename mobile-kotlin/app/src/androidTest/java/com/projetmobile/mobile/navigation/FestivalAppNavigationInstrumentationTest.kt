package com.projetmobile.mobile.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.entity.auth.VerificationResultStatus
import com.projetmobile.mobile.data.entity.auth.RegisterAccountInput
import com.projetmobile.mobile.data.entity.festival.FestivalSummary
import com.projetmobile.mobile.data.entity.profile.AvatarUploadResult
import com.projetmobile.mobile.data.entity.profile.OptionalField
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateInput
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateResult
import com.projetmobile.mobile.data.repository.auth.AuthRepository
import com.projetmobile.mobile.data.repository.festival.FestivalRepository
import com.projetmobile.mobile.data.repository.profile.ProfileRepository
import com.projetmobile.mobile.ui.screens.app.FestivalApp
import com.projetmobile.mobile.ui.theme.FestivalMobileTheme
import com.projetmobile.mobile.ui.utils.navigation.AppNavKey
import com.projetmobile.mobile.ui.utils.navigation.ResetPassword
import com.projetmobile.mobile.ui.utils.navigation.VerificationResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class FestivalAppNavigationInstrumentationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun publicChrome_showsPublicTabsOnly() {
        setFestivalAppContent()

        waitForTag("bottom-tab-Festivals")

        composeRule.onNodeWithTag("bottom-tab-Festivals").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-tab-Login").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-tab-Register").assertIsDisplayed()
        composeRule.onAllNodesWithTag("bottom-tab-Reservants").assertCountEquals(0)
        composeRule.onAllNodesWithTag("bottom-tab-Games").assertCountEquals(0)
        composeRule.onAllNodesWithTag("bottom-tab-Profile").assertCountEquals(0)
        composeRule.onAllNodesWithTag("bottom-tab-Admin").assertCountEquals(0)
        composeRule.onNodeWithTag("app-top-bar-title").assertIsDisplayed()
    }

    @Test
    fun authenticatedChrome_showsBusinessTabsForNonAdmin() {
        setFestivalAppContent(
            authRepository = FakeAuthRepository(initialUser = sampleUser(role = "organizer")),
        )

        waitForTag("bottom-tab-Profile")

        composeRule.onNodeWithTag("bottom-tab-Festivals").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-tab-Reservants").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-tab-Games").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-tab-Profile").assertIsDisplayed()
        composeRule.onAllNodesWithTag("bottom-tab-Login").assertCountEquals(0)
        composeRule.onAllNodesWithTag("bottom-tab-Register").assertCountEquals(0)
        composeRule.onAllNodesWithTag("bottom-tab-Admin").assertCountEquals(0)
    }

    @Test
    fun authenticatedChrome_showsAdminTabForAdmin() {
        setFestivalAppContent(
            authRepository = FakeAuthRepository(initialUser = sampleUser(role = "admin")),
        )

        waitForTag("bottom-tab-Admin")

        composeRule.onNodeWithTag("bottom-tab-Festivals").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-tab-Reservants").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-tab-Games").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-tab-Profile").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-tab-Admin").assertIsDisplayed()
    }

    @Test
    fun forgotPassword_showsBackButton_andKeepsLoginTabSelected() {
        setFestivalAppContent()

        composeRule.onNodeWithTag("bottom-tab-Login").performClick()
        composeRule.onNodeWithText("Réinitialiser").performClick()

        waitForText("Mot de passe oublié")

        composeRule.onNodeWithTag("app-back-button").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-tab-Login").assertIsSelected()

        composeRule.onNodeWithTag("app-back-button").performClick()

        waitForText("Connexion")
        composeRule.onNodeWithTag("bottom-tab-Login").assertIsSelected()
    }

    @Test
    fun loginSuccess_switchesToFestivals() {
        setFestivalAppContent(
            authRepository = FakeAuthRepository(loginUser = sampleUser()),
        )

        composeRule.onNodeWithTag("bottom-tab-Login").performClick()
        composeRule.onNodeWithTag("login-identifier-field").performTextInput("romain@example.com")
        composeRule.onNodeWithTag("login-password-field").performTextInput("password123")
        composeRule.onNodeWithTag("login-submit-button").performClick()

        waitForText("Festival Lumiere")

        composeRule.onNodeWithTag("bottom-tab-Festivals").assertIsSelected()
        composeRule.onNodeWithText("Festival Lumiere").assertIsDisplayed()
    }

    @Test
    fun logoutSuccess_switchesToLogin() {
        setFestivalAppContent(
            authRepository = FakeAuthRepository(initialUser = sampleUser(role = "organizer")),
        )

        composeRule.onNodeWithTag("bottom-tab-Profile").performClick()
        composeRule.onNodeWithTag("logout-button").performClick()

        waitForText("Connexion")

        composeRule.onNodeWithTag("bottom-tab-Login").assertIsSelected()
        composeRule.onNodeWithTag("login-submit-button").assertIsDisplayed()
    }

    @Test
    fun profileScreen_showsAccountSections_andPasswordResetAction() {
        setFestivalAppContent(
            authRepository = FakeAuthRepository(initialUser = sampleUser(role = "organizer")),
            profileRepository = FakeProfileRepository(initialProfile = sampleUser(role = "organizer")),
        )

        composeRule.onNodeWithTag("bottom-tab-Profile").performClick()

        waitForTag("profile-summary-card")

        composeRule.onNodeWithTag("profile-summary-card").assertIsDisplayed()
        composeRule.onNodeWithText("Romain").assertIsDisplayed()
        composeRule.onNodeWithText("Richard").assertIsDisplayed()
        composeRule.onNodeWithText("@romain").assertIsDisplayed()
        composeRule.onNodeWithTag("profile-login-edit-button").assertIsDisplayed()
        composeRule.onNodeWithTag("profile-email-edit-button").assertIsDisplayed()
        composeRule.onNodeWithTag("profile-password-card").assertIsDisplayed()
        composeRule.onNodeWithTag("profile-password-reset-button").assertIsDisplayed()
        composeRule.onNodeWithTag("logout-button").assertIsDisplayed()
    }

    @Test
    fun profileScreen_allowsInlineEditingFromFieldActions() {
        setFestivalAppContent(
            authRepository = FakeAuthRepository(initialUser = sampleUser(role = "organizer")),
            profileRepository = FakeProfileRepository(initialProfile = sampleUser(role = "organizer")),
        )

        composeRule.onNodeWithTag("bottom-tab-Profile").performClick()
        waitForTag("profile-login-edit-button")

        composeRule.onNodeWithTag("profile-login-edit-button").performClick()

        waitForTag("profile-login-field")

        composeRule.onNodeWithTag("profile-login-field").assertIsDisplayed()
        composeRule.onNodeWithTag("profile-save-button").assertIsDisplayed()
        composeRule.onNodeWithTag("profile-cancel-button").assertIsDisplayed()

        composeRule.onNodeWithTag("profile-email-edit-button").performClick()
        waitForTag("profile-email-field")
        composeRule.onNodeWithTag("profile-email-field").assertIsDisplayed()
    }

    @Test
    fun profileScreen_successBanner_disappearsAfterDelay() {
        setFestivalAppContent(
            authRepository = FakeAuthRepository(initialUser = sampleUser(role = "organizer")),
            profileRepository = FakeProfileRepository(initialProfile = sampleUser(role = "organizer")),
        )

        composeRule.onNodeWithTag("bottom-tab-Profile").performClick()
        waitForTag("profile-first-name-edit-button")

        composeRule.onNodeWithTag("profile-first-name-edit-button").performClick()
        waitForTag("profile-first-name-field")
        composeRule.onNodeWithTag("profile-first-name-field").performTextInput(" Jr")
        composeRule.onNodeWithTag("profile-save-button").performClick()

        waitForText("Profil mis a jour.")
        composeRule.onNodeWithText("Profil mis a jour.").assertIsDisplayed()
        waitForTextGone("Profil mis a jour.", timeoutMillis = 6_000)
    }

    @Test
    fun registerSuccess_routesToPendingVerification_andKeepsRegisterTabSelected() {
        setFestivalAppContent(
            authRepository = FakeAuthRepository(),
        )

        composeRule.onNodeWithTag("bottom-tab-Register").performClick()
        composeRule.onNodeWithTag("register-username-field").performTextInput("romain")
        composeRule.onNodeWithTag("register-first-name-field").performTextInput("Romain")
        composeRule.onNodeWithTag("register-last-name-field").performTextInput("Richard")
        composeRule.onNodeWithTag("register-email-field").performTextInput("romain@example.com")
        composeRule.onNodeWithTag("register-password-field").performTextInput("password123")
        composeRule.onNodeWithTag("register-submit-button").performClick()

        waitForText("Vérifiez votre email")

        composeRule.onNodeWithTag("bottom-tab-Register").assertIsSelected()
        composeRule.onNodeWithText(
            "Un email de vérification a été envoyé à romain@example.com. Ouvrez-le depuis votre téléphone pour revenir directement dans l’app.",
        ).assertIsDisplayed()
    }

    @Test
    fun registerScreen_hidesIntroCopy_andShowsPrimaryActionsWithoutScrolling() {
        setFestivalAppContent()

        composeRule.onNodeWithTag("bottom-tab-Register").performClick()

        waitForTag("register-username-field")

        composeRule.onAllNodesWithText(
            "Créez votre compte avec les informations demandées. Un email de vérification vous sera envoyé automatiquement.",
        ).assertCountEquals(0)
        composeRule.onNodeWithTag("register-username-field").assertIsDisplayed()
        composeRule.onNodeWithTag("register-first-name-field").assertIsDisplayed()
        composeRule.onNodeWithTag("register-last-name-field").assertIsDisplayed()
        composeRule.onNodeWithTag("register-submit-button").assertIsDisplayed()
        composeRule.onNodeWithText("Retour à la connexion").assertIsDisplayed()
    }

    @Test
    fun authDeepLinks_attachSecondaryScreensToLoginTab() {
        val destinations = MutableSharedFlow<AppNavKey>(extraBufferCapacity = 2)
        setFestivalAppContent(incomingDestinations = destinations)

        runBlocking {
            destinations.emit(ResetPassword("abc123"))
        }

        waitForText("Réinitialiser votre mot de passe")

        composeRule.onNodeWithTag("bottom-tab-Login").assertIsSelected()
        composeRule.onNodeWithText("Choisissez un nouveau mot de passe pour votre compte. Ce lien est sécurisé et valable pendant 1 heure.").assertIsDisplayed()

        runBlocking {
            destinations.emit(VerificationResult(VerificationResultStatus.Expired))
        }

        waitForText("Lien expiré")

        composeRule.onNodeWithTag("bottom-tab-Login").assertIsSelected()
        composeRule.onNodeWithText(
            "Le lien de vérification a expiré. Demandez un nouvel email depuis l’écran de connexion.",
        ).assertIsDisplayed()
    }

    @Test
    fun loginSuccess_nonAdminDoesNotShowAdminTab() {
        setFestivalAppContent(
            authRepository = FakeAuthRepository(loginUser = sampleUser(role = "organizer")),
        )

        composeRule.onNodeWithTag("bottom-tab-Login").performClick()
        composeRule.onNodeWithTag("login-identifier-field").performTextInput("orga@example.com")
        composeRule.onNodeWithTag("login-password-field").performTextInput("password123")
        composeRule.onNodeWithTag("login-submit-button").performClick()

        waitForTag("bottom-tab-Profile")

        composeRule.onNodeWithTag("bottom-tab-Festivals").assertIsSelected()
        composeRule.onNodeWithTag("bottom-tab-Reservants").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-tab-Games").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-tab-Profile").assertIsDisplayed()
        composeRule.onAllNodesWithTag("bottom-tab-Admin").assertCountEquals(0)
    }

    @Test
    fun loginSuccess_adminShowsAdminTab() {
        setFestivalAppContent(
            authRepository = FakeAuthRepository(loginUser = sampleUser(role = "admin")),
        )

        composeRule.onNodeWithTag("bottom-tab-Login").performClick()
        composeRule.onNodeWithTag("login-identifier-field").performTextInput("admin@example.com")
        composeRule.onNodeWithTag("login-password-field").performTextInput("password123")
        composeRule.onNodeWithTag("login-submit-button").performClick()

        waitForTag("bottom-tab-Admin")

        composeRule.onNodeWithTag("bottom-tab-Festivals").assertIsSelected()
        composeRule.onNodeWithTag("bottom-tab-Admin").assertIsDisplayed()
    }

    @Test
    fun placeholderTabs_showImplementationMessage() {
        setFestivalAppContent(
            authRepository = FakeAuthRepository(initialUser = sampleUser(role = "admin")),
        )

        composeRule.onNodeWithTag("bottom-tab-Reservants").performClick()
        waitForText("Section en cours d'implémentation")
        composeRule.onNodeWithTag("bottom-tab-Reservants").assertIsSelected()

        composeRule.onNodeWithTag("bottom-tab-Games").performClick()
        waitForText("Section en cours d'implémentation")
        composeRule.onNodeWithTag("bottom-tab-Games").assertIsSelected()

        composeRule.onNodeWithTag("bottom-tab-Admin").performClick()
        waitForText("Section en cours d'implémentation")
        composeRule.onNodeWithTag("bottom-tab-Admin").assertIsSelected()
    }

    private fun setFestivalAppContent(
        authRepository: FakeAuthRepository = FakeAuthRepository(),
        festivalRepository: FestivalRepository = FakeFestivalRepository(),
        profileRepository: ProfileRepository = FakeProfileRepository(),
        incomingDestinations: MutableSharedFlow<AppNavKey> = MutableSharedFlow(extraBufferCapacity = 1),
    ) {
        composeRule.setContent {
            FestivalMobileTheme {
                FestivalApp(
                    authRepository = authRepository,
                    festivalRepository = festivalRepository,
                    profileRepository = profileRepository,
                    incomingDestinations = incomingDestinations,
                )
            }
        }
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForTextGone(text: String, timeoutMillis: Long) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()
        }
    }
}

private class FakeFestivalRepository : FestivalRepository {
    override suspend fun getFestivals(): Result<List<FestivalSummary>> {
        return Result.success(
            listOf(
                FestivalSummary(
                    id = 1,
                    name = "Festival Lumiere",
                    startDate = "2026-06-10",
                    endDate = "2026-06-12",
                    stockTablesStandard = 10,
                    stockTablesGrande = 2,
                    stockTablesMairie = 1,
                    stockChaises = 48,
                    prixPrises = 18.0,
                ),
            ),
        )
    }
}

private class FakeAuthRepository(
    initialUser: AuthUser? = null,
    private val loginUser: AuthUser? = null,
    private val logoutFailureMessage: String? = null,
) : AuthRepository {
    private var currentUser: AuthUser? = initialUser
    private var pendingVerificationEmail: String? = null
    private var lastLoginIdentifier: String? = null

    override suspend fun login(identifier: String, password: String): Result<AuthUser> {
        val resolvedUser = loginUser ?: sampleUser(email = identifier)
        currentUser = resolvedUser
        lastLoginIdentifier = identifier
        return Result.success(resolvedUser)
    }

    override suspend fun register(input: RegisterAccountInput): Result<String> {
        pendingVerificationEmail = input.email.trim()
        lastLoginIdentifier = input.email.trim()
        return Result.success("Un email de vérification a été envoyé.")
    }

    override suspend fun resendVerification(email: String): Result<String> {
        pendingVerificationEmail = email.trim()
        return Result.success("Email de vérification renvoyé.")
    }

    override suspend fun requestPasswordReset(email: String): Result<String> {
        lastLoginIdentifier = email.trim()
        return Result.success("Si un compte existe, un email a été envoyé.")
    }

    override suspend fun resetPassword(token: String, password: String): Result<String> {
        return if (token.isBlank()) {
            Result.failure(IllegalStateException("Le lien de réinitialisation est invalide ou incomplet."))
        } else {
            Result.success("Votre mot de passe a été mis à jour.")
        }
    }

    override suspend fun logout(): Result<String> {
        val failureMessage = logoutFailureMessage
        if (failureMessage != null) {
            return Result.failure(IllegalStateException(failureMessage))
        }
        currentUser = null
        return Result.success("Déconnecté.")
    }

    override suspend fun restoreSession(): Result<AuthUser?> {
        return Result.success(currentUser)
    }

    override suspend fun getCurrentUser(): Result<AuthUser> {
        val user = currentUser ?: return Result.failure(IllegalStateException("Aucune session active."))
        return Result.success(user)
    }

    override suspend fun getPendingVerificationEmail(): String? = pendingVerificationEmail

    override suspend fun setPendingVerificationEmail(email: String) {
        pendingVerificationEmail = email
    }

    override suspend fun clearPendingVerificationEmail() {
        pendingVerificationEmail = null
    }

    override suspend fun getLastLoginIdentifier(): String? = lastLoginIdentifier

    override suspend fun setLastLoginIdentifier(identifier: String) {
        lastLoginIdentifier = identifier
    }
}

private fun sampleUser(
    email: String = "romain@example.com",
    role: String = "organizer",
): AuthUser {
    return AuthUser(
        id = 7,
        login = "romain",
        role = role,
        firstName = "Romain",
        lastName = "Richard",
        email = email,
        phone = "0600000000",
        avatarUrl = null,
        emailVerified = true,
        createdAt = "2026-03-18T09:00:00Z",
    )
}

private class FakeProfileRepository(
    initialProfile: AuthUser = sampleUser(),
) : ProfileRepository {
    private var currentProfile: AuthUser = initialProfile

    override suspend fun getProfile(): Result<AuthUser> = Result.success(currentProfile)

    override suspend fun updateProfile(input: ProfileUpdateInput): Result<ProfileUpdateResult> {
        val previousEmail = currentProfile.email
        val emailChanged = input.email != null && input.email != previousEmail
        currentProfile = currentProfile.copy(
            login = input.login ?: currentProfile.login,
            firstName = input.firstName ?: currentProfile.firstName,
            lastName = input.lastName ?: currentProfile.lastName,
            email = input.email ?: currentProfile.email,
            phone = when (val phone = input.phone) {
                OptionalField.Unchanged -> currentProfile.phone
                is OptionalField.Value -> phone.value
            },
            avatarUrl = when (val avatar = input.avatarUrl) {
                OptionalField.Unchanged -> currentProfile.avatarUrl
                is OptionalField.Value -> avatar.value
            },
            emailVerified = if (emailChanged) {
                false
            } else {
                currentProfile.emailVerified
            },
        )

        return Result.success(
            ProfileUpdateResult(
                message = "Profil mis a jour.",
                user = currentProfile,
                emailVerificationSent = emailChanged,
            ),
        )
    }

    override suspend fun uploadAvatar(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Result<AvatarUploadResult> {
        return Result.success(
            AvatarUploadResult(
                url = "/uploads/avatars/$fileName",
                message = "Avatar uploade.",
            ),
        )
    }

    override suspend fun requestPasswordReset(email: String): Result<String> {
        return Result.success("Si un compte existe, un email a ete envoye.")
    }
}
