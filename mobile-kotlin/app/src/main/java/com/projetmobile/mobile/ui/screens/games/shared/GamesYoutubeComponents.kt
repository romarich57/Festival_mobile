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
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?,
                    ) {
                        if (request?.isForMainFrame == true) {
                            onPlayerError(
                                error?.description?.toString().orEmpty().ifBlank {
                                    YoutubePlayerDefaultErrorMessage
                                },
                            )
                        }
                    }

                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?,
                    ) {
                        if (request?.isForMainFrame == true) {
                            onPlayerError("Erreur HTTP ${errorResponse?.statusCode ?: 0}")
                        }
                    }
                }
                setBackgroundColor(android.graphics.Color.BLACK)
            }
        },
        update = { webView ->
            if (webView.tag != videoReference.videoId) {
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

internal fun openVideoExternally(
    context: Context,
    videoReference: YoutubeVideoReference,
) {
    openYoutubeExternally(context, videoReference)
}

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

private fun openInBrowser(
    context: Context,
    url: String,
) {
    startExternalActivity(
        context = context,
        intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)),
    )
}

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

private sealed interface GamesYoutubePlayerState {
    data object Loading : GamesYoutubePlayerState

    data object Ready : GamesYoutubePlayerState

    data class Error(val message: String) : GamesYoutubePlayerState
}

private class GamesYoutubePlayerBridge(
    private val onPlayerReady: () -> Unit,
    private val onPlayerError: (String) -> Unit,
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onPlayerReady() {
        mainHandler.post(onPlayerReady)
    }

    @JavascriptInterface
    fun onPlayerError(reason: String?) {
        mainHandler.post {
            onPlayerError(reason.orEmpty())
        }
    }
}
