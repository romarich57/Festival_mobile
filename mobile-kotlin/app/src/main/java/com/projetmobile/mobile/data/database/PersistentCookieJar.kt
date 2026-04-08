/**
 * Rôle du fichier :
 * Implémentation complète et sécurisée d'un gestionnaire de Cookies ("CookieJar") pour le client HTTP OkHttp.
 * Ce fichier permet d'intercepter les cookies renvoyés par l'API backend (comme le jwt_token)
 * et de les réinjecter automatiquement dans les appels suivants, créant ainsi des sessions HTTP persistantes.
 * Ces cookies sont sauvegardés localement de manière chiffrée afin de prévenir les failles de sécurité.
 */
package com.projetmobile.mobile.data.database

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.json.JSONArray
import org.json.JSONObject

/**
 * Rôle : Implémentation de la politique de persistance pour la librairie OkHttp (via CookieJar).
 * Agit comme une façade déléguant les traitements au magasin chiffré.
 * 
 * Précondition : Un contexte Android valide (Context) permettant d'instancier un espace chiffré.
 * Postcondition : Gère l'enregistrement et le chargement automatiques des variables de session (ex: JWT) dans les requêtes/réponses réseau.
 */
class PersistentCookieJar(context: Context) : CookieJar {
    // Instanciation de l'engine de sécurité interne sur les SharedPreferences.
    private val cookieStore = SecureCookieStore(context.applicationContext)

    /**
     * Rôle : Intercepter les cookies renvoyés par une réponse serveur (via 'Set-Cookie') et les passer
     * à l'espace de stockage persistant et chiffré.
     * 
     * Précondition : Réception d'une réponse OkHttp incluant des en-têtes contenant des cookies.
     * Postcondition : Les cookies sont délégués au `cookieStore` pour un enregistrement sécurisé et pérenne.
     */
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore.saveCookies(cookies)
    }

    /**
     * Rôle : Injecter les cookies correspondants aux critères de l'hôte (URL) dans chaque requête OkHttp
     * sortante pour maintenir la "session" (accès authentifié protégé par Auth Token).
     * 
     * Précondition : Le client web lance une requête HTTP sur l'URL ciblée.
     * Postcondition : Retourne la liste des cookies valides, ajoutée automatiquement au Header "Cookie".
     */
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore.loadCookies(url)
    }
}

/**
 * Rôle : Opérer la sérialisation, la désérialisation, ainsi que le cryptage / décryptage dynamique
 * via la librairie EncryptedSharedPreferences (AndroidX Security).
 * 
 * Précondition : Fournir le contexte adéquat pour créer le fichier.
 * Postcondition : Stocke un magasin sécurisé au format SharedPreferences où le contenu n'est pas lisible en clair.
 */
private class SecureCookieStore(context: Context) {
    // Un simple Object() qui opèrera comme Mutex pour sécuriser les blocs Thread-Safe (évite lectures corruptives)
    private val lock = Any()
    // Toujours s'assurer d'utiliser context.applicationContext pour éviter les Memory-Leaks.
    private val applicationContext = context.applicationContext
    
    // Reste null jusqu'au premier appel, s'instancie uniquement via getSharedPreferences()
    private var sharedPreferences: SharedPreferences? = null

    /**
     * Rôle : Sauvegarder en mémoire cryptée les cookies obtenus d'une réponse.
     * Gère tout le processus de la mise à jour (écraser un vieux cookie avec sa nouvelle version),
     * et de nettoyage (enlever ceux marqués comme expirés/persistent expiré).
     * 
     * Précondition : Liste de nouveaux cookies.
     * Postcondition : Écrit sur le disque les informations validées sans doublon et expurge les cookies en fin de vie de la base.
     */
    fun saveCookies(cookies: List<Cookie>) {
        // Vérouille l'accès pour empêcher qu'un autre processus modifie les données en simultané.
        synchronized(lock) {
            // Lecture des données existantes transformées en dictionnaire associatif par identifiant.
            // .toMutableMap() est indispensable car nous allons y ajouter ou retirer des éléments.
            val mergedCookies = readCookies()
                .associateBy(::cookieIdentifier)
                .toMutableMap()

            // Itération sur les nouveaux arrivants
            cookies.forEach { cookie ->
                // Si le cookie n'est pas de "Sesssion" (donc persistent) MAIS que sa date est expirée, on le supprime.
                if (cookie.persistent && cookie.expiresAt < System.currentTimeMillis()) {
                    mergedCookies.remove(cookieIdentifier(cookie))
                } else {
                    // Sinon, on le stocke ou on l'écrase dans sa case définie.
                    mergedCookies[cookieIdentifier(cookie)] = cookie
                }
            }

            // Repassage du Dico/Map en Liste simple, et écriture via notre fonction serialisatrice
            writeCookies(mergedCookies.values.toList())
        }
    }

    /**
     * Rôle : Détecter et purger les cookies périmés dans le stockage local avant de
     * retourner uniquement ceux compatibles avec le domaine visé.
     * 
     * Précondition : Fournir l'URL sur laquelle la requête OkHttp sort.
     * Postcondition : Renvoie un Array de `Cookie` valides. De plus, opère une réécriture immédiate si des éléments ont été balayés à cette lecture.
     */
    fun loadCookies(url: HttpUrl): List<Cookie> {
        synchronized(lock) {
            val currentTime = System.currentTimeMillis()
            // On collecte tout le tiroir persistant chiffré de cookies
            val allCookies = readCookies()
            
            // La variable `activeCookies` garde :
            // - les Cookies non-persistant (cookie de session actif tant que l'app n'est pas kill).
            // - OU les Cookies persistants SI leur date d'expiration est encore dans le futur.
            val activeCookies = allCookies.filter { cookie ->
                !cookie.persistent || cookie.expiresAt >= currentTime
            }

            // Si j'ai observé un écart (signifiant l'expiration de certains objets), je résauvegarde
            if (activeCookies.size != allCookies.size) {
                writeCookies(activeCookies)
            }

            // Ne retourne au final QUE les cookies qui ont le droit d'être mis sur *cette* HttpUrl (url.matches)
            return activeCookies.filter { cookie -> cookie.matches(url) }
        }
    }

    /**
     * Rôle : Extraire la chaine encodée (`getString`) du chiffreur Android puis parcourir le tableau JSON
     * pour reconstruire laborieusement des instances d'`okhttp3.Cookie`.
     * 
     * Précondition : Un fichier 'cookies_prefs' peut y être trouvé, abritant la clé dédiée.
     * Postcondition : En cas de succès : retourne les cookies enregistrés. Si un blocage Android KeyStore se produit, réinitialise le stockage transparent.
     */
    private fun readCookies(): List<Cookie> {
        return runCatching {
            // Lecture brute. Si rien, emptyList()
            val serializedCookies = getSharedPreferences().getString(COOKIE_STORE_KEY, null) ?: return emptyList()
            val jsonArray = JSONArray(serializedCookies)
            
            // Usage de buildList pour itérer sur le tableau Json et append des elements sur la volée
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val cookieJson = jsonArray.optJSONObject(index) ?: continue
                    deserializeCookie(cookieJson)?.let(::add) // S'il est valide (non-null), on l'ajoute.
                }
            }
        }.getOrElse { throwable ->
            // Le bloc crypté de SharedPreferences a eu un bug interne d'Android keystore ou Json parser.
            Log.w(TAG, "Cookie store corrompu, réinitialisation locale.", throwable)
            clearCorruptedStore()
            emptyList()
        }
    }

    /**
     * Rôle : Traduire une liste intégrale d'instances HTTP en objet `JSONArray` puis ordonner au pont
     * sécuritatif `EncryptedSharedPreferences` d'abriter cette signature (`putString`).
     * 
     * Précondition : La liste d'objets `Cookie` mise à jour.
     * Postcondition : La donnée de base de l'application contient la clé `COOKIE_STORE_KEY` fraîche.
     */
    private fun writeCookies(cookies: List<Cookie>) {
        val jsonArray = JSONArray()
        // Transforme individuellement chaque objet Cookie en JSON Object via la méthode helper
        cookies.forEach { cookie ->
            jsonArray.put(serializeCookie(cookie))
        }

        // Tente de sauvegarder la chaine JSON sur le fichier local. Catch de sécurité comme vue précédemment.
        runCatching {
            getSharedPreferences().edit()
                // .putString est intercepté par EncryptedSharedPreferences pour masquer (chiffrer) l'array.
                .putString(COOKIE_STORE_KEY, jsonArray.toString())
                .apply() // Operation asynchronement écrite.
        }.getOrElse { throwable ->
            Log.w(TAG, "Impossible d'écrire le cookie store, réinitialisation locale.", throwable)
            clearCorruptedStore()
        }
    }

    /**
     * Rôle : Opérer la rétro-conversion de la structure stricte `okhttp3.Cookie` vers son homologue
     * texte (json) neutre, sauvegardant domaines, chemins, valeurs sécurisées, etc.
     * 
     * Précondition : Un `Cookie` intercepté valide.
     * Postcondition : Un `JSONObject` sérialisé avec certitude.
     */
    private fun serializeCookie(cookie: Cookie): JSONObject {
        return JSONObject().apply {
            put("name", cookie.name)
            put("value", cookie.value)
            put("expiresAt", cookie.expiresAt) // Date de "péremption" pour la mise en nettoyage.
            put("domain", cookie.domain)
            put("path", cookie.path)
            put("secure", cookie.secure) // Si "true", requiert HTTPS.
            put("httpOnly", cookie.httpOnly) // Empèche le script frontend de lire le contenu s'il était sur un navigateur.
            put("hostOnly", cookie.hostOnly)
            put("persistent", cookie.persistent) // Mémorise s'il survit à la session temporaire.
        }
    }

    /**
     * Rôle de la fonction :
     * Lire un JSONObject extrait par le Storage, pour rebâtir avec le patter `Builder` un `okhttp3.Cookie`
     * injectable au niveau du header HTTP d'okhttp.
     * 
     * @param jsonObject La représentation brute extraite.
     * @return [Cookie]? L'objet reconstruit ou `null` si des valeurs obligatoires manquent ou qu'une erreur de build se produit.
     */
    private fun deserializeCookie(jsonObject: JSONObject): Cookie? {
        val domain = jsonObject.optString("domain")
        val name = jsonObject.optString("name")
        val value = jsonObject.optString("value")
        
        // Sanity Check : Les noms et domaines sont impératifs car c'est eux qui définissent où se hooker.
        if (domain.isBlank() || name.isBlank()) {
            return null
        }

        return runCatching {
            Cookie.Builder().apply {
                name(name)
                value(value)

                if (jsonObject.optBoolean("hostOnly")) {
                    hostOnlyDomain(domain)
                } else {
                    domain(domain)
                }

                // Donne le path root par défaut "/" si absent
                path(jsonObject.optString("path", "/"))

                if (jsonObject.optBoolean("persistent")) {
                    expiresAt(jsonObject.optLong("expiresAt", Long.MAX_VALUE))
                }

                // Ajoute des configurations HTTP-strictes ou HTTPS-strictes.
                if (jsonObject.optBoolean("secure")) {
                    secure()
                }
                if (jsonObject.optBoolean("httpOnly")) {
                    httpOnly()
                }
            }.build()
        }.getOrNull()
    }

    /**
     * Rôle de la fonction :
     * Construire un hachage identitaire / String unique en concaténant les infos clés d'un cookie.
     * Cela permet d'écraser un cookie par sa mise à jour dans un Dictionnaire [Map] (cf. `saveCookies()`), 
     * au lieu de le mettre en doublon.
     */
    private fun cookieIdentifier(cookie: Cookie): String {
        return "${cookie.name}|${cookie.domain}|${cookie.path}"
    }

    /**
     * Rôle de la fonction :
     * Récupère l'instance chiffrée ou la crée (si première fois). 
     * Assure un mécanisme de survie : si la clé de chiffrement (MasterKeys) de l'OS débloque 
     * ou qu'elle devient invalide, elle attrape l'erreur, détruit manuellement les précédents logs corrompus 
     * et essaie immédiatement de recréer une nouvelle base vierge fiable.
     * 
     * @return [SharedPreferences] La session configurée avec la solution Security.
     */
    private fun getSharedPreferences(): SharedPreferences {
        // Retourne directement mon instance sauvegardée le cas échéant.
        sharedPreferences?.let { return it }

        return runCatching {
            // Tente de créer la base encryptée...
            createEncryptedPreferences()
        }.recoverCatching {
            // .. Si ca échoue on gère la fuite : 
            Log.w(TAG, "Préférences chiffrées invalides, suppression du store local.", it)
            clearCorruptedStore() // Flush the xml file since we can't open it.
            createEncryptedPreferences() // Réessaye un nouveau init !
        }.getOrThrow().also { preferences ->
            // On retient pour un accès rapide.
            sharedPreferences = preferences
        }
    }

    /**
     * Rôle de la fonction :
     * Instancier un `EncryptedSharedPreferences` via le système Keystore Root Hardware-backed 
     * d'Android (AES256 SIV & GCM).
     */
    private fun createEncryptedPreferences(): SharedPreferences {
        // Va chercher un alias clé maître depuis le système Android. (Keystore).
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        
        // Formate notre fichier XML comme des SharedPreferences cryptées pour y placer des tokens sans risque.
        return EncryptedSharedPreferences.create(
            PREFERENCES_NAME,
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /**
     * Rôle de la fonction :
     * Détruire totalement le store en cas d'échec total (changement d'algorithme OS, master key effacée, etc.).
     */
    private fun clearCorruptedStore() {
        sharedPreferences = null
        applicationContext.deleteSharedPreferences(PREFERENCES_NAME)
    }

    private companion object {
        // Tag pour les filtrages Logcat Android
        private const val TAG = "SecureCookieStore"
        // Le nom du fichier XML de préférences généré par l'OS
        private const val PREFERENCES_NAME = "festival_mobile_cookies"
        // La clé qui détiendra notre array JSON global sérialisée.
        private const val COOKIE_STORE_KEY = "cookies_json"
    }
}
