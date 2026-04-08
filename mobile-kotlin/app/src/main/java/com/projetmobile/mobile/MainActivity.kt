/**
 * Rôle : Activité principale (MainActivity) de l'application Android servant de conteneur UI unique.
 * Elle monte l'interface graphique via Jetpack Compose, intercepte les Deep Links
 * et applique le thème global de l'application au composant racine.
 * Précondition : Le contexte Android charge et lance l'activité lors de l'exécution de l'application.
 * Postcondition : `FestivalApp` s'affiche correctement avec le thème et le gestionnaire de navigation.
 */
package com.projetmobile.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.projetmobile.mobile.ui.screens.app.FestivalApp
import com.projetmobile.mobile.ui.theme.FestivalMobileTheme
import com.projetmobile.mobile.ui.utils.navigation.AppNavKey
import com.projetmobile.mobile.ui.utils.navigation.parseAppDeepLink
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * Rôle : Classe d'ancrage visuel de l'application (Single-Activity).
 * Hérite de `ComponentActivity` pour fournir le contexte Android et gérer le cycle de vie Composables.
 * Précondition : Doit être la classe Activity de démarrage référencée dans le manifeste de l'application Android.
 * Postcondition : Assure l'injection d'`AppContainer` et la propagation des `incomingDestinations`.
 */
class MainActivity : ComponentActivity() {

    // Le conteneur de dépendances est récupéré depuis l'Application uniquement au premier accès.
    private val appContainer by lazy {
        (application as FestivalApplication).appContainer
    }

    // Flux partagé utilisé pour déclencher une navigation automatique à partir d'un deep link.
    private val incomingDestinations = MutableSharedFlow<AppNavKey>(extraBufferCapacity = 1)

    /**
     * Rôle : Initialise l'activité, monte l'arbre Compose et traite un deep link éventuel au lancement.
     *
     * Précondition : Le système Android crée ou recrée l'activité.
     *
     * Postcondition : L'UI racine est affichée avec le thème global et la navigation peut recevoir une destination externe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Le contenu s'étend sous les barres système pour garder une mise en page cohérente avec Compose.
        enableEdgeToEdge()

        setContent {
            FestivalMobileTheme {
                FestivalApp(
                    appContainer = appContainer,
                    incomingDestinations = incomingDestinations,
                )
            }
        }

        // Si l'activité a été ouverte par un lien profond, on le propage au routeur.
        emitDeepLink(intent)
    }

    /**
     * Rôle : Met à jour l'activité lorsqu'un nouvel intent arrive, notamment depuis un deep link à chaud.
     *
     * Précondition : L'activité est déjà instanciée et reçoit un nouvel `Intent`.
     *
     * Postcondition : L'intent courant est remplacé et analysé pour une navigation éventuelle.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // L'intent courant est conservé pour les traitements ultérieurs.
        setIntent(intent)

        emitDeepLink(intent)
    }

    /**
     * Rôle : Analyse l'URI d'un intent et émet la destination interne correspondante si elle existe.
     *
     * Précondition : L'intent peut contenir une URI vide, absente ou valide.
     *
     * Postcondition : Une destination valide est envoyée dans `incomingDestinations`, sinon rien n'est émis.
     */
    private fun emitDeepLink(intent: Intent?) {
        val destination = parseAppDeepLink(intent?.data) ?: return

        // L'émission reste liée au cycle de vie de l'activité.
        lifecycleScope.launch {
            incomingDestinations.emit(destination)
        }
    }
}
