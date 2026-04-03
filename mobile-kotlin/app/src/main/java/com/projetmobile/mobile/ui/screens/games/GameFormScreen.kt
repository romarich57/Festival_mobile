package com.projetmobile.mobile.ui.screens.games

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone

@Composable
internal fun GameFormScreen(
    uiState: GameFormUiState,
    actions: GameFormActions,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        val selection = uri?.let { loadGameImageSelection(context, it) }
            ?: return@rememberLauncherForActivityResult
        actions.onLocalImageSelected(selection)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("game-form-root"),
    ) {
        val showHorizontalActions = maxWidth >= 520.dp

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.errorMessage != null) {
                    item {
                        AuthFeedbackBanner(
                            message = uiState.errorMessage,
                            tone = AuthFeedbackTone.Error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("game-form-error-banner"),
                        )
                    }
                    item {
                        OutlinedButton(onClick = actions.onDismissErrorMessage) {
                            Text("Fermer le message")
                        }
                    }
                }

                if (uiState.errorMessage == null && uiState.lookupErrorMessage != null) {
                    item {
                        AuthFeedbackBanner(
                            message = uiState.lookupErrorMessage,
                            tone = AuthFeedbackTone.Error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("game-form-lookup-error-banner"),
                        )
                    }
                }

                item {
                    GameFormHeroCard(
                        mode = uiState.mode,
                        gameTitle = uiState.fields.title,
                        onBackToList = actions.onBackToList,
                    )
                }

                item {
                    GameFormContentCard(
                        uiState = uiState,
                        actions = actions,
                        showHorizontalActions = showHorizontalActions,
                        onPickLocalImage = { imagePickerLauncher.launch(arrayOf("image/*")) },
                    )
                }
            }
        }
    }
}
