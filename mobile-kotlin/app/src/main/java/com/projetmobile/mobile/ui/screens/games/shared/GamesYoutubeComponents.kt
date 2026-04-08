/**
 * Rôle : Gère l'aperçu, la lecture et les fallbacks YouTube pour les écrans de jeux.
 * Ce fichier réunit la miniature de prévisualisation, la boîte de dialogue de lecture, la WebView et les helpers de lancement externe.
 * Précondition : Les URL vidéo fournies doivent provenir d'une source contrôlée ou d'une saisie utilisateur déjà validée.
 * Postcondition : L'UI peut afficher une vidéo intégrée ou offrir des solutions de repli lisibles.
 */
package com.projetmobile.mobile.ui.screens.games

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

private const val YoutubePlayerBridgeName = "AndroidYoutubePlayer"
private const val YoutubePlayerTimeoutMillis = 8_000L
private const val YoutubePlayerDefaultErrorMessage = "Impossible de lire cette vidéo dans l'application."

/**
 * Rôle : Affiche un aperçu de vidéo YouTube pour les règles d'un jeu.
 * Précondition : `rulesVideoUrl` doit être un lien potentiellement supporté par `youtubeVideoReference`.
 * Postcondition : L'utilisateur voit soit un état vide, soit une miniature cliquable, soit un message d'erreur clair.
 */
@Composable
internal fun GamesRulesVideoPreview(
    rulesVideoUrl: String,
    modifier: Modifier = Modifier,
    title: String = "Vidéo des règles",
    emptyMessage: String = "Aucune vidéo renseignée.",
    invalidMessage: String = "Lien YouTube invalide ou non supporté.",
    onPlayVideo: ((YoutubeVideoReference) -> Unit)? = null,
) {
    val videoReference = remember(rulesVideoUrl) { youtubeVideoReference(rulesVideoUrl) }
    val isBlank = rulesVideoUrl.isBlank()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
            color = Color(0xFF5D6981),
        )
        when {
            isBlank -> {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF5D6981),
                    modifier = Modifier.testTag("games-rules-video-empty"),
                )
            }

            videoReference == null -> {
                Text(
                    text = invalidMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFB42318),
                    modifier = Modifier.testTag("games-rules-video-invalid"),
                )
            }

            else -> {
                // Le clic n'est actif que lorsque l'appelant veut ouvrir la lecture complète.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            color = Color(0xFFEAF0FB),
                            shape = RoundedCornerShape(24.dp),
                        )
                        .let { currentModifier ->
                            if (onPlayVideo == null) {
                                currentModifier
                            } else {
                                currentModifier.clickable { onPlayVideo(videoReference) }
                            }
                        }
                        .testTag("games-rules-video-preview"),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = videoReference.thumbnailUrl,
                        contentDescription = "Aperçu vidéo",
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                color = Color(0xCCFF0000),
                                shape = RoundedCornerShape(999.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(44.dp),
                        )
                    }
                }
                if (onPlayVideo != null) {
                    OutlinedButton(
                        onClick = { onPlayVideo(videoReference) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("games-rules-video-play-button"),
                    ) {
                        Text("Lire la vidéo")
                    }
                }
            }
        }
    }
}

/**
 * Rôle : Affiche la boîte de dialogue de lecture YouTube avec WebView et états de chargement.
 * Précondition : `videoReference` doit déjà contenir un identifiant YouTube valide.
 * Postcondition : La vidéo démarre dans une vue plein écran ou affiche un message de repli si la lecture échoue.
 */
@Composable
internal fun GamesYoutubePlayerDialog(
    videoReference: YoutubeVideoReference,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var playerState by remember(videoReference.videoId) {
        mutableStateOf<GamesYoutubePlayerState>(GamesYoutubePlayerState.Loading)
    }
    val onPlayerReadyState by rememberUpdatedState {
        playerState = GamesYoutubePlayerState.Ready
    }
    val onPlayerErrorState by rememberUpdatedState { reason: String ->
        playerState = GamesYoutubePlayerState.Error(resolveYoutubePlayerErrorMessage(reason))
    }

    LaunchedEffect(videoReference.videoId) {
        // Si le lecteur n'émet aucun signal de prêt, on bascule vers un état d'erreur avec des repli externes.
        delay(YoutubePlayerTimeoutMillis)
        if (playerState is GamesYoutubePlayerState.Loading) {
            playerState = GamesYoutubePlayerState.Error(
                "Le lecteur intégré n'a pas réussi à démarrer.",
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("games-youtube-player-dialog"),
            color = Color.Black,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Fermer la vidéo",
                            tint = Color.White,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    GamesYoutubeWebView(
                        videoReference = videoReference,
                        modifier = Modifier.fillMaxSize(),
                        onPlayerReady = onPlayerReadyState,
                        onPlayerError = onPlayerErrorState,
                    )

                    // Le lecteur garde un overlay tant qu'il n'a pas signalé un état exploitable.
                    when (val state = playerState) {
                        GamesYoutubePlayerState.Loading -> GamesYoutubeLoadingOverlay()
                        GamesYoutubePlayerState.Ready -> Unit
                        is GamesYoutubePlayerState.Error -> GamesYoutubeErrorOverlay(
                            message = state.message,
                            onOpenYoutube = {
                                openYoutubeExternally(
                                    context = context,
                                    videoReference = videoReference,
                                )
                            },
                            onOpenBrowser = {
                                openInBrowser(
                                    context = context,
                                    url = videoReference.watchUrl,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Rôle : Monte la WebView qui héberge le lecteur YouTube embarqué.
 * Précondition : `videoReference` doit être valide et les callbacks doivent être prêts à recevoir les événements du lecteur.
 * Postcondition : La page HTML du lecteur est chargée ou rechargée quand la vidéo change.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun GamesYoutubeWebView(
    videoReference: YoutubeVideoReference,
    modifier: Modifier = Modifier,
    onPlayerReady: () -> Unit,
    onPlayerError: (String) -> Unit,
) {
    val bridge = remember(onPlayerReady, onPlayerError) {
        GamesYoutubePlayerBridge(
            onPlayerReady = onPlayerReady,
            onPlayerError = onPlayerError,
        )
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                addJavascriptInterface(bridge, YoutubePlayerBridgeName)
                webViewClient = object : WebViewClient() {
                    /**
                     * Rôle : Exécute l'action on received erreur du module les jeux partagé.
                     *
                     * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
                     *
                     * Postcondition : Le résultat reflète l'opération demandée.
                     */
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?,
                    ) {
                        if (request?.isForMainFrame == true) {
                            // On ignore les erreurs de sous-ressources pour ne remonter que l'état réel du player.
                            onPlayerError(
                                error?.description?.toString().orEmpty().ifBlank {
                                    YoutubePlayerDefaultErrorMessage
                                },
                            )
                        }
                    }

                    /**
                     * Rôle : Exécute l'action on received http erreur du module les jeux partagé.
                     *
                     * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
                     *
                     * Postcondition : Le résultat reflète l'opération demandée.
                     */
                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?,
                    ) {
                        if (request?.isForMainFrame == true) {
                            // Les erreurs HTTP du document principal doivent être remontées comme un échec de lecture.
                            onPlayerError("Erreur HTTP ${errorResponse?.statusCode ?: 0}")
                        }
                    }
                }
                setBackgroundColor(android.graphics.Color.BLACK)
            }
        },
        update = { webView ->
            if (webView.tag != videoReference.videoId) {
                // On recharge le lecteur uniquement quand l'identifiant vidéo a changé.
                webView.tag = videoReference.videoId
                webView.loadDataWithBaseURL(
                    "https://www.youtube.com",
                    buildYoutubePlayerHtml(videoReference.videoId),
                    "text/html",
                    "utf-8",
                    null,
                )
            }
        },
    )
}

/**
 * Rôle : Affiche l'overlay de chargement du lecteur YouTube pendant l'initialisation.
 * Précondition : Le lecteur doit être dans l'état `Loading`.
 * Postcondition : L'utilisateur voit une progression et un texte d'attente superposés à la vidéo.
 */
@Composable
private fun GamesYoutubeLoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(color = Color.White)
            Text(
                text = "Chargement de la vidéo…",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/**
 * Rôle : Affiche l'overlay d'erreur avec des solutions de repli pour la lecture YouTube.
 * Précondition : Un message d'erreur doit déjà être disponible pour expliquer l'échec de lecture.
 * Postcondition : L'utilisateur peut ouvrir YouTube ou le navigateur pour continuer la lecture.
 */
@Composable
private fun GamesYoutubeErrorOverlay(
    message: String,
    onOpenYoutube: () -> Unit,
    onOpenBrowser: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD9000000))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = Color(0xFF18233A),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.widthIn(max = 520.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Lecture impossible",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFD0D8E8),
                )
                OutlinedButton(
                    onClick = onOpenYoutube,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Ouvrir sur YouTube")
                }
                OutlinedButton(
                    onClick = onOpenBrowser,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Ouvrir dans le navigateur")
                }
            }
        }
    }
}

/**
 * Rôle : Construit la page HTML injectée dans la WebView pour charger le lecteur YouTube.
 * Précondition : `videoId` doit être un identifiant YouTube valide et non vide.
 * Postcondition : Retourne un document HTML autonome prêt à être affiché par la WebView.
 */
private fun buildYoutubePlayerHtml(videoId: String): String {
    return """
        <!DOCTYPE html>
        <html lang="fr">
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0" />
                <style>
                    html, body, #player {
                        margin: 0;
                        width: 100%;
                        height: 100%;
                        background: #000;
                        overflow: hidden;
                    }
                </style>
                <script src="https://www.youtube.com/iframe_api"></script>
            </head>
            <body>
                <div id="player"></div>
                <script>
                    let player = null;
                    let settled = false;

                    function notifyReady() {
                        if (!settled && window.$YoutubePlayerBridgeName) {
                            settled = true;
                            window.$YoutubePlayerBridgeName.onPlayerReady();
                        }
                    }

                    function notifyError(reason) {
                        if (!settled && window.$YoutubePlayerBridgeName) {
                            settled = true;
                            window.$YoutubePlayerBridgeName.onPlayerError(String(reason || 'unknown'));
                        }
                    }

                    function onYouTubeIframeAPIReady() {
                        try {
                            player = new YT.Player('player', {
                                width: '100%',
                                height: '100%',
                                videoId: '$videoId',
                                playerVars: {
                                    autoplay: 1,
                                    playsinline: 1,
                                    rel: 0
                                },
                                events: {
                                    onReady: function(event) {
                                        notifyReady();
                                        event.target.playVideo();
                                    },
                                    onError: function(event) {
                                        notifyError('youtube_error:' + event.data);
                                    }
                                }
                            });
                        } catch (error) {
                            notifyError(error && error.message ? error.message : 'player_init_failed');
                        }
                    }

                    window.addEventListener('error', function(event) {
                        notifyError(event && event.message ? event.message : 'window_error');
                    });
                </script>
            </body>
        </html>
    """.trimIndent()
}

/**
 * Rôle : Transforme un code d'erreur technique du lecteur en message humain.
 * Précondition : `reason` doit contenir le texte brut transmis par le lecteur ou la couche WebView.
 * Postcondition : Retourne un message de repli compréhensible par l'utilisateur.
 */
private fun resolveYoutubePlayerErrorMessage(reason: String): String {
    return when {
        reason.contains("youtube_error:101") || reason.contains("youtube_error:150") -> {
            "La vidéo refuse l'intégration dans l'application. Ouvrez-la sur YouTube."
        }

        reason.contains("youtube_error:100") -> {
            "Cette vidéo n'est plus disponible."
        }

        reason.contains("youtube_error:2") || reason.contains("youtube_error:5") -> {
            YoutubePlayerDefaultErrorMessage
        }

        reason.contains("ERR_INTERNET_DISCONNECTED", ignoreCase = true) -> {
            "Connexion internet indisponible."
        }

        reason.contains("timeout", ignoreCase = true) -> {
            "Le lecteur a mis trop de temps à répondre."
        }

        else -> YoutubePlayerDefaultErrorMessage
    }
}

/**
 * Rôle : Ouvre la vidéo en utilisant d'abord les applications ou routes externes disponibles.
 * Précondition : `videoReference` doit référencer une vidéo YouTube valide.
 * Postcondition : L'utilisateur est redirigé vers YouTube ou, en dernier recours, vers le navigateur.
 */
internal fun openVideoExternally(
    context: Context,
    videoReference: YoutubeVideoReference,
) {
    openYoutubeExternally(context, videoReference)
}

/**
 * Rôle : Tente d'ouvrir la vidéo directement dans l'application YouTube.
 * Précondition : `videoReference` doit contenir un identifiant YouTube valide.
 * Postcondition : Si l'application YouTube n'est pas disponible, un repli navigateur est déclenché.
 */
private fun openYoutubeExternally(
    context: Context,
    videoReference: YoutubeVideoReference,
) {
    val youtubeIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("vnd.youtube:${videoReference.videoId}"),
    )
    if (!startExternalActivity(context, youtubeIntent)) {
        openInBrowser(context, videoReference.watchUrl)
    }
}

/**
 * Rôle : Ouvre une URL dans le navigateur externe de l'appareil.
 * Précondition : `url` doit être une URL navigable.
 * Postcondition : Le système tente de lancer une activité externe vers cette URL.
 */
private fun openInBrowser(
    context: Context,
    url: String,
) {
    startExternalActivity(
        context = context,
        intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)),
    )
}

/**
 * Rôle : Lance une activité externe de manière sûre et retourne si l'opération a réussi.
 * Précondition : Le contexte et l'intent doivent être prêts pour un démarrage d'activité.
 * Postcondition : Retourne `true` si l'activité a pu être ouverte, sinon `false`.
 */
private fun startExternalActivity(
    context: Context,
    intent: Intent,
): Boolean {
    return try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

/**
 * Rôle : Représente les états possibles du lecteur YouTube embarqué.
 * Précondition : L'état doit être manipulé uniquement par le composant de lecture.
 * Postcondition : L'UI peut basculer entre chargement, prêt et erreur avec un modèle clair.
 */
private sealed interface GamesYoutubePlayerState {
    /**
     * Rôle : Expose un singleton de support pour le module les jeux partagé.
     */
    data object Loading : GamesYoutubePlayerState

    /**
     * Rôle : Expose un singleton de support pour le module les jeux partagé.
     */
    data object Ready : GamesYoutubePlayerState

    /**
     * Rôle : Représente l'état d'erreur du lecteur avec le message à afficher à l'utilisateur.
     * Précondition : Un échec de lecture doit avoir été détecté.
     * Postcondition : L'UI peut afficher une erreur explicite et proposer des solutions de repli.
     */
    data class Error(val message: String) : GamesYoutubePlayerState
}

/**
 * Rôle : Sert de pont JavaScript vers Kotlin pour signaler l'état du lecteur YouTube.
 * Précondition : Les callbacks de disponibilité et d'erreur doivent être prêts à recevoir les événements.
 * Postcondition : Les événements JS sont renvoyés sur le thread principal de l'application.
 */
private class GamesYoutubePlayerBridge(
    private val onPlayerReady: () -> Unit,
    private val onPlayerError: (String) -> Unit,
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Rôle : Signale que le lecteur YouTube est prêt à être utilisé.
     * Précondition : L'événement doit provenir du bridge JavaScript du player.
     * Postcondition : Le callback Kotlin associé est exécuté sur le thread principal.
     */
    @JavascriptInterface
    fun onPlayerReady() {
        mainHandler.post(onPlayerReady)
    }

    /**
     * Rôle : Signale une erreur de lecture remontée par le lecteur YouTube.
     * Précondition : `reason` peut être nul ou vide, selon la source de l'erreur.
     * Postcondition : Le callback Kotlin reçoit une chaîne d'erreur normalisée sur le thread principal.
     */
    @JavascriptInterface
    fun onPlayerError(reason: String?) {
        mainHandler.post {
            onPlayerError(reason.orEmpty())
        }
    }
}
