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
    
    // Récupération de l'instance d'AppContainer (le conteneur de dépendances) depuis FestivalApplication.
    // L'initialisation est "lazy" (paresseuse), c'est-à-dire qu'elle n'est créée qu'au moment de son premier appel.
    private val appContainer by lazy {
        (application as FestivalApplication).appContainer
    }
    
    // Flux partagé asynchrone utilisé pour émettre les destinations vers lesquelles l'application
    // doit naviguer automatiquement (notamment dans le cas de Deep Links).
    private val incomingDestinations = MutableSharedFlow<AppNavKey>(extraBufferCapacity = 1)

    /**: Initialise le cycle de vue Composé, active le mode "edge-to-edge", et écoute
     * les `Deep Links` éventuels.
     * Précondition : Le système lance ou réactive cette activité.
     * Postcondition : Le composant racine UI (FestivalApp) est affiché et le cycle de mise à jour Jetpack est définitat éventuellement sauvegardé de l'activité (null lors d'un nouveau lancement).
     * @return Unit. Ne renvoie rien.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Exécute la logique de base du framework Android.
        super.onCreate(savedInstanceState)
        
        // Active le mode d'affichage "edge-to-edge" (le contenu va jusqu'en dessous des barres de navigation et de statut).
        enableEdgeToEdge()
        
        // Définit le contenu de l'interface utilisateur de l'activité en utilisant Jetpack Compose.
        setContent {
            // Application du thème global (couleurs, polices, formes d'interface).
            FestivalMobileTheme {
                // Appel du composant racine de l'application (Navigation et Scaffold global).
                FestivalApp(
                    appContainer = appContainer,
                    incomingDestinations = incomingDestinations,
                )
            }
        }
        
        // Tentative de parsing du lien (Deep Link) avec lequel l'application a pu être lancée.
        emitDeepLink(intent)
    }

    /**
     * Rôle de la fonction :
     * Gère : Met à jour les intentions avec de possibles nouveaux Deep Links arrivant
     * sur l'application lorsqu'elle est déjà instanciée (arrière-plan).
     * Précondition : Un `Intent` entrant avec un URI pertinent cible cette activité.
     * Postcondition : Parse ce nouvel `Intent` et le transmet via l'émission de destinatio
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Met à jour l'Intent courant pour de futures références possibles.
        setIntent(intent)
        // Parse et déclenche la navigation si le nouvel Intent contient un Deep Link valide.
        emitDeepLink(intent)
    }

    /**
     * Rôle de la fonction :
     * Parse l'URI contenue dans l'Intent (s'il y en a un) et l'émet dans le flux `incomingDestinations`
     * afin de déclencher un changement d'écran via le système de navigation de l'application.
     * 
     * @para: Analyse la route et paramètres d'un URI pour rediriger vers un écran spécifique
     * de l'application via le SharedFlow `incomingDestinations`.
     * Précondition : Reçoit un Intent (contenant possiblement null ou un format d'URL).
     * Postcondition : Émet l'objet `AppNavKey` correspondant ou ignore silencieusementspondance n'est trouvée, la fonction s'arrête ici via `return`.
        val destination = parseAppDeepLink(intent?.data) ?: return
        
        // Lancement d'une coroutine attachée au cycle de vie de l'activité (lifecycleScope)
        // pour émettre la destination vers le flux.
        lifecycleScope.launch {
            incomingDestinations.emit(destination)
        }
    }
}