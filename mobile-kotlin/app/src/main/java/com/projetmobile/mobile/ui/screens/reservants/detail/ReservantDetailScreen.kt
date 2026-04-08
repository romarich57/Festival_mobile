package com.projetmobile.mobile.ui.screens.reservants

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.reservants.ReservantContact
import com.projetmobile.mobile.data.entity.reservants.ReservantDetail
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone
import com.projetmobile.mobile.ui.components.FestivalTextField
import com.projetmobile.mobile.ui.components.PrimaryAuthButton
import com.projetmobile.mobile.ui.screens.games.toAbsoluteBackendUrl
import kotlinx.coroutines.delay

/**
 * Rôle : Présente l'entièreté des onglets et informations disponibles sur une fiche "Réservant".
 *
 * Précondition : Appliqué lorsque l'utilisateur a cliqué sur un contact depuis le catalogue.
 *
 * Postcondition : Construit et répartit les Scaffold, Tabs, Modifier, ainsi que les grilles de jeux alloués ou des informations basiques.
 */
@Composable
internal fun ReservantDetailScreen(
    uiState: ReservantDetailUiState,
    actions: ReservantDetailActions,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(uiState.infoMessage) {
        if (uiState.infoMessage == null) {
            return@LaunchedEffect
        }
        delay(4_000)
        actions.onDismissInfoMessage()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("reservant-detail-root"),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
                    .testTag("reservant-detail-list"),
                contentPadding = PaddingValues(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.errorMessage != null) {
                    item {
                        AuthFeedbackBanner(
                            message = uiState.errorMessage,
                            tone = AuthFeedbackTone.Error,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = actions.onDismissErrorMessage) {
                                Text("Fermer")
                            }
                            OutlinedButton(onClick = actions.onRetry) {
                                Text("Réessayer")
                            }
                        }
                    }
                }

                if (uiState.infoMessage != null) {
                    item {
                        AuthFeedbackBanner(
                            message = uiState.infoMessage,
                            tone = AuthFeedbackTone.Success,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                when {
                    uiState.isLoading && uiState.reservant == null -> item {
                        ReservantsLoadingCard(text = "Chargement du réservant…")
                    }

                    uiState.reservant != null -> {
                        val reservant = uiState.reservant
                        item {
                            ReservantDetailHeroCard(
                                reservant = reservant,
                                canManageReservants = uiState.canManageReservants,
                                onEditReservant = actions.onEditReservant,
                            )
                        }
                        item {
                            AuthCard(modifier = Modifier.fillMaxWidth()) {
                                TabRow(selectedTabIndex = uiState.activeTab.ordinal) {
                                    ReservantDetailTab.entries.forEach { tab ->
                                        Tab(
                                            selected = tab == uiState.activeTab,
                                            onClick = { actions.onSelectTab(tab) },
                                            text = { Text(tab.label) },
                                        )
                                    }
                                }
                            }
                        }
                        when (uiState.activeTab) {
                            ReservantDetailTab.Infos -> item { ReservantInfosTab(reservant) }
                            ReservantDetailTab.Contacts -> item {
                                ReservantContactsTab(uiState = uiState, actions = actions)
                            }
                            ReservantDetailTab.Jeux -> item {
                                ReservantGamesTab(uiState = uiState, actions = actions)
                            }
                        }
                    }

                    else -> item {
                        ReservantsEmptyCard(
                            title = "Réservant introuvable",
                            body = "Le réservant demandé n'est plus disponible.",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservantDetailHeroCard(
    reservant: ReservantDetail,
    canManageReservants: Boolean,
    onEditReservant: (Int) -> Unit,
) {
    AuthCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color(0xFFF2F7FF),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = reservant.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF18233A),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailChip(reservantTypeLabel(reservant.type))
                reservant.email.takeIf { it.isNotBlank() }?.let { DetailChip(it) }
            }
            if (canManageReservants) {
                PrimaryAuthButton(
                    text = "Modifier",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEditReservant(reservant.id) },
                )
            }
        }
    }
}

@Composable
private fun ReservantInfosTab(reservant: ReservantDetail) {
    AuthCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DetailField("Nom", reservant.name)
            DetailField("Email", reservant.email)
            DetailField("Téléphone", reservant.phoneNumber.orEmpty())
            DetailField("Adresse", reservant.address.orEmpty())
            DetailField("Siret", reservant.siret.orEmpty())
            DetailField("Notes", reservant.notes.orEmpty())
        }
    }
}

@Composable
private fun ReservantContactsTab(
    uiState: ReservantDetailUiState,
    actions: ReservantDetailActions,
) {
    AuthCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Contacts",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF255EC8),
                )
                if (uiState.canManageReservants) {
                    OutlinedButton(onClick = actions.onToggleContactForm) {
                        Text(if (uiState.isContactFormExpanded) "Fermer" else "Ajouter")
                    }
                }
            }

            if (uiState.contactsErrorMessage != null) {
                AuthFeedbackBanner(
                    message = uiState.contactsErrorMessage,
                    tone = AuthFeedbackTone.Error,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(onClick = actions.onDismissContactsErrorMessage) {
                    Text("Fermer le message")
                }
            }

            if (uiState.isContactFormExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FestivalTextField(
                        value = uiState.contactForm.name,
                        onValueChange = actions.onContactNameChanged,
                        label = "Nom",
                        isError = uiState.contactForm.nameError != null,
                        supportingText = uiState.contactForm.nameError,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FestivalTextField(
                        value = uiState.contactForm.email,
                        onValueChange = actions.onContactEmailChanged,
                        label = "Email",
                        isError = uiState.contactForm.emailError != null,
                        supportingText = uiState.contactForm.emailError,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FestivalTextField(
                        value = uiState.contactForm.phoneNumber,
                        onValueChange = actions.onContactPhoneNumberChanged,
                        label = "Téléphone",
                        isError = uiState.contactForm.phoneNumberError != null,
                        supportingText = uiState.contactForm.phoneNumberError,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FestivalTextField(
                        value = uiState.contactForm.jobTitle,
                        onValueChange = actions.onContactJobTitleChanged,
                        label = "Fonction / Type",
                        isError = uiState.contactForm.jobTitleError != null,
                        supportingText = uiState.contactForm.jobTitleError,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ReservantsDropdownSelector(
                        label = "Priorité",
                        selectedLabel = priorityLabel(uiState.contactForm.priority),
                        options = listOf(
                            "0 - Normal" to 0,
                            "1 - Prioritaire" to 1,
                        ),
                        onValueSelected = actions.onContactPrioritySelected,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PrimaryAuthButton(
                        text = if (uiState.isSavingContact) "Ajout…" else "Ajouter le contact",
                        enabled = !uiState.isSavingContact,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = actions.onSaveContact,
                    )
                }
            }

            when {
                uiState.isLoadingContacts -> CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                uiState.contacts.isEmpty() -> Text(
                    text = "Aucun contact enregistré pour le moment.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF5D6981),
                )
                else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.contacts.forEach { contact ->
                        ContactRow(contact)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(contact: ReservantContact) {
    AuthCard(modifier = Modifier.fillMaxWidth(), containerColor = Color(0xFFFAFCFF)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF18233A),
            )
            Text(contact.jobTitle, color = Color(0xFF255EC8))
            Text(contact.email, color = Color(0xFF5D6981))
            Text(contact.phoneNumber, color = Color(0xFF5D6981))
            DetailChip(priorityLabel(contact.priority))
        }
    }
}

@Composable
private fun ReservantGamesTab(
    uiState: ReservantDetailUiState,
    actions: ReservantDetailActions,
) {
    val linkedEditorId = uiState.linkedEditorId

    AuthCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Jeux",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF255EC8),
                )
                if (uiState.canManageReservants && linkedEditorId != null && uiState.reservant != null) {
                    OutlinedButton(
                        onClick = { actions.onCreateLinkedGame(uiState.reservant.id, linkedEditorId) },
                    ) {
                        Text("Nouveau jeu")
                    }
                }
            }

            if (uiState.gamesErrorMessage != null) {
                AuthFeedbackBanner(
                    message = uiState.gamesErrorMessage,
                    tone = AuthFeedbackTone.Error,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(onClick = actions.onDismissGamesErrorMessage) {
                    Text("Fermer le message")
                }
            }

            when {
                linkedEditorId == null -> Text(
                    text = "Aucun éditeur lié. Associez un éditeur pour afficher son catalogue de jeux.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF5D6981),
                )
                uiState.isLoadingGames -> CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                uiState.games.isEmpty() -> Text(
                    text = "Aucun jeu trouvé pour cet éditeur.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF5D6981),
                )
                else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.games.forEach { game ->
                        GameRow(game = game, onOpenGameDetails = actions.onOpenGameDetails)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameRow(
    game: GameListItem,
    onOpenGameDetails: (Int) -> Unit,
) {
    AuthCard(modifier = Modifier.fillMaxWidth(), containerColor = Color(0xFFFAFCFF)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEAF0FB)),
                    contentAlignment = Alignment.Center,
                ) {
                    val imageModel = game.imageUrl?.let(::toAbsoluteBackendUrl)
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = game.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.SportsEsports,
                            contentDescription = null,
                            tint = Color(0xFF6F96DD),
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF18233A),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailChip(game.type)
                DetailChip("${game.minAge}+")
                game.playersLabel()?.let { DetailChip(it) }
            }
            OutlinedButton(onClick = { onOpenGameDetails(game.id) }) {
                Text("Voir le jeu")
            }
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF6F96DD),
        )
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF18233A),
        )
    }
}

@Composable
private fun DetailChip(label: String) {
    AuthCard(containerColor = Color(0xFFEAF0FB)) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFF255EC8),
        )
    }
}

private fun priorityLabel(priority: Int): String {
    return if (priority == 1) {
        "1 - Prioritaire"
    } else {
        "0 - Normal"
    }
}
