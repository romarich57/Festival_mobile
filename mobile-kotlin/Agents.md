# Agents.md

## Objectif

Ce document décrit l’état réel de l’application Android Kotlin/Jetpack Compose du projet `festivalapp` après la migration d’arborescence et l’ajout du flux mobile de réinitialisation de mot de passe.

Il sert de référence pour :
- l’organisation des packages ;
- la séparation des responsabilités ;
- les flux auth supportés ;
- les endpoints backend consommés par le mobile ;
- les deep links reconnus par l’application.

## Architecture actuelle

L’application reste une app Android mono-activité autour de [MainActivity.kt](/Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/mobile-kotlin/app/src/main/java/com/projetmobile/mobile/MainActivity.kt) et de [FestivalApp.kt](/Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/mobile-kotlin/app/src/main/java/com/projetmobile/mobile/ui/screens/app/FestivalApp.kt).

La composition générale est :

```text
com/projetmobile/mobile/
  data/
    dao/
    database/
    entity/
      auth/
      festival/
    mapper/
      auth/
      festival/
    remote/
      auth/
      festival/
    repository/
      auth/
      festival/
  ui/
    components/
    screens/
      app/
      auth/
        login/
        register/
        emailverification/
        forgotpassword/
        resetpassword/
      profile/
      festival/
    theme/
    utils/
      navigation/
      session/
      validation/
  workers/
  AppContainer.kt
  MainActivity.kt
```

## Règles de structure

- `data/entity/*` contient les modèles utilisés côté application, pas les DTO réseau.
- `data/remote/*` contient les interfaces Retrofit et DTOs liés à l’API Node/Express.
- `data/mapper/*` contient les conversions DTO -> entités.
- `data/repository/*` contient les interfaces et implémentations consommées par les ViewModels.
- `data/database/*` contient la persistance locale Android comme `AuthPreferenceStore` et `PersistentCookieJar`.
- `ui/screens/*` regroupe un écran avec son `UiState` et son `ViewModel`.
- `ui/components/*` contient les briques Compose réutilisables.
- `ui/utils/navigation/*` contient les destinations et conventions de navigation.
- `ui/utils/validation/*` contient la validation de formulaires.
- `ui/utils/session/*` contient la restauration et la propagation de session côté UI.
- `workers/` existe comme placeholder d’arborescence même si aucun worker n’est encore branché.

## Fichiers pivots

- [AppContainer.kt](/Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/mobile-kotlin/app/src/main/java/com/projetmobile/mobile/AppContainer.kt)
  centralise Retrofit, Moshi, cookie jar et repositories.
- [AuthApiService.kt](/Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/mobile-kotlin/app/src/main/java/com/projetmobile/mobile/data/remote/auth/AuthApiService.kt)
  définit les endpoints auth consommés par l’app.
- [AuthRepository.kt](/Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/mobile-kotlin/app/src/main/java/com/projetmobile/mobile/data/repository/auth/AuthRepository.kt)
  expose le contrat auth pour les ViewModels.
- [AuthRepositoryImpl.kt](/Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/mobile-kotlin/app/src/main/java/com/projetmobile/mobile/data/repository/auth/AuthRepositoryImpl.kt)
  gère les appels réseau auth et la persistance légère liée à l’auth.
- [AppNavKey.kt](/Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/mobile-kotlin/app/src/main/java/com/projetmobile/mobile/ui/utils/navigation/AppNavKey.kt)
  centralise les clés typées Nav3, les tabs top-level, le chrome global et le parsing des deep links.
- [AuthFormValidator.kt](/Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/mobile-kotlin/app/src/main/java/com/projetmobile/mobile/ui/utils/validation/AuthFormValidator.kt)
  centralise la validation locale des formulaires auth.

## Flux auth mobile

### 1. Connexion

- écran : `ui/screens/auth/login`
- endpoint : `POST /api/auth/login`
- persistance UI : mémorise le dernier identifiant saisi pour préremplir la connexion

### 2. Inscription

- écran : `ui/screens/auth/register`
- endpoint : `POST /api/auth/register`
- effet attendu : un email de vérification est envoyé et l’email en attente est mémorisé localement

### 3. Vérification email en attente

- écran : `ui/screens/auth/emailverification/PendingVerificationScreen`
- endpoint : `POST /api/auth/resend-verification`
- rôle : permettre le renvoi d’un email de vérification

### 4. Résultat de vérification email

- écran : `ui/screens/auth/emailverification/VerificationResultScreen`
- deep link supporté : `festivalapp://auth/verification?status={status}`
- le backend mobile-first ouvre l’app via `GET /api/auth/verify-email?token=...`

### 5. Mot de passe oublié

- écran : `ui/screens/auth/forgotpassword`
- endpoint : `POST /api/auth/password/forgot`
- comportement :
  - l’utilisateur saisit son email ;
  - le backend répond toujours avec un message générique pour éviter l’énumération des comptes ;
  - après succès, l’email est mémorisé comme dernier identifiant de connexion.

### 6. Réinitialisation de mot de passe

- écran : `ui/screens/auth/resetpassword`
- endpoint : `POST /api/auth/password/reset`
- deep link supporté : `festivalapp://auth/reset-password?token={token}`
- entrée mobile-first :
  - l’email reçu pointe vers `GET /api/auth/reset-password?token=...`
  - cette route backend tente d’ouvrir l’app via deep link et fournit une page HTML fallback
- décision produit :
  - le token n’est pas prévalidé à l’ouverture de l’écran ;
  - la validation réelle du token se fait au `submit` du nouveau mot de passe ;
  - si le token est absent, l’écran mobile affiche un état d’erreur dédié avec retour à la connexion.

### 7. Profil

- écran : `ui/screens/profile`
- visible uniquement quand une session est active
- rôle actuel :
  - afficher les informations de session de base ;
  - permettre la déconnexion
- endpoint : `POST /api/auth/logout`

## Endpoints backend auth utiles au mobile

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/resend-verification`
- `GET /api/auth/verify-email?token=...`
- `POST /api/auth/password/forgot`
- `POST /api/auth/password/reset`
- `POST /api/auth/logout`
- `GET /api/auth/reset-password?token=...`
- `GET /api/auth/whoami`

## Deep links supportés

- `festivalapp://auth/verification?status={status}`
- `festivalapp://auth/reset-password?token={token}`

Le manifest Android accepte actuellement les chemins custom scheme :
- `/verification`
- `/reset-password`

via [AndroidManifest.xml](/Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/mobile-kotlin/app/src/main/AndroidManifest.xml).

## Validation locale des formulaires auth

[AuthFormValidator.kt](/Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/mobile-kotlin/app/src/main/java/com/projetmobile/mobile/ui/utils/validation/AuthFormValidator.kt) couvre :

- login ;
- register ;
- resend verification ;
- forgot password ;
- reset password avec confirmation.

Règles actuelles côté app :
- email requis et format simple valide pour forgot/reset where needed ;
- mot de passe minimum 8 caractères ;
- confirmation obligatoire et identique au mot de passe pour le reset.

## Notes de conception

- La couche `domain/` n’existe plus dans l’état cible mobile.
- Les repositories auth/festival vivent directement sous `data/repository/*`.
- L’application utilise une navigation basse uniquement pour les destinations top-level :
  - en session absente : `Festivals`, `Connexion`, `Inscription`
  - en session active non-admin : `Festivals`, `Réservants`, `Jeux`, `Profil`
  - en session active admin : `Festivals`, `Réservants`, `Jeux`, `Profil`, `Admin`
- Les écrans `forgot password`, `reset password`, `pending verification` et `verification result` ne sont pas des onglets.
- Les sections `Réservants`, `Jeux` et `Admin` sont pour l’instant des placeholders Navigation 3 qui affichent uniquement `Section en cours d'implémentation`.
- Le flux de reset est volontairement mobile-first ; aucun frontend web dédié n’est supposé dans ce projet.

## Attendus Navigation 3

- Le shell racine ne doit plus utiliser `NavController`, `NavHost` ni des routes string comme source de vérité de navigation.
- La navigation doit reposer sur des clés typées sérialisables déclarées dans [AppNavKey.kt](/Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/mobile-kotlin/app/src/main/java/com/projetmobile/mobile/ui/utils/navigation/AppNavKey.kt) via `sealed interface AppNavKey : NavKey`.
- Les piles top-level obligatoires sont `Festivals`, `Reservants`, `Games`, `Profile`, `Admin`, `Login` et `Register`.
- `Login` et `Register` sont réservées au mode public ; `Admin` n’est visible qu’avec le rôle `admin`.
- L’onglet actif doit être conservé au niveau racine, indépendamment de la pile actuellement affichée.
- Le rendu de la pile active doit passer par `NavDisplay`.
- Le shell doit préserver l’état Compose et les `ViewModel` d’écran via les décorateurs publics de saveable state et d’intégration ViewModel Navigation 3.
- Les écrans auth secondaires doivent rester rattachés à leur pile d’affinité :
- `ForgotPassword`, `ResetPassword` et `VerificationResult` appartiennent à la pile `Login`.
- `PendingVerification` appartient à la pile `Register`.
- Le `Scaffold` global doit porter :
- une `CenterAlignedTopAppBar` dont le titre dérive de la clé active ;
- un bouton retour visible seulement hors racine de pile ;
- une `NavigationBar` dépendante de l’état de session.
- La navigation basse doit rester :
- session absente : `Festivals`, `Connexion`, `Inscription` ;
- session active non-admin : `Festivals`, `Réservants`, `Jeux`, `Profil` ;
- session active admin : `Festivals`, `Réservants`, `Jeux`, `Profil`, `Admin`.
- Un logout réussi depuis `Profil` doit ramener explicitement sur `Connexion` et ne pas conserver de pile privée active.
- Un login réussi doit ramener sur `Festivals` et normaliser la pile `Login` à sa racine.
- Un logout réussi doit aussi réinitialiser les piles privées `Festivals`, `Reservants`, `Games`, `Profile` et `Admin`.
- Un register réussi doit ouvrir `PendingVerification(email)` dans la pile `Register`.
- Les deep links existants doivent être conservés sans mode hybride `navigation-compose` :
- `festivalapp://auth/verification?status={status}` ;
- `festivalapp://auth/reset-password?token={token}`.
- Le parsing des `Intent` ou `Uri` entrants doit être traduit en `AppNavKey` dès l’entrée de l’application, y compris sur `onNewIntent`.
- Pour `VerificationResult(status)`, la top bar doit conserver les libellés dynamiques :
- `Email confirmé` ;
- `Lien expiré` ;
- `Lien invalide` ;
- `Erreur de vérification`.
- Les écrans suivants ne doivent plus rendre de titre visuel local en tête de contenu afin d’éviter un doublon avec la top bar globale :
- `FestivalScreen`
- `ProfileScreen`
- `LoginScreen`
- `RegisterScreen`
- `ForgotPasswordScreen`
- `ResetPasswordScreen`
- `PendingVerificationScreen`
- `VerificationResultScreen`
- La suppression du titre local ne doit pas modifier les messages d’aide, les champs, les CTA, les états loading/erreur/succès ni la logique métier.
- `Festivals` et `Profil` conservent leur contenu fonctionnel actuel.
- `Réservants`, `Jeux` et `Admin` sont des onglets top-level Navigation 3 mais leur contenu courant est un placeholder unique sans CTA.
- Aucun `libs.versions.toml` ne doit être introduit dans ce chantier.




Autre docuementations : 

## Objectif du document

Ce document compile les informations, règles, notes, spécifications, références et bonnes pratiques transmises pour cadrer l’organisation, l’architecture et le développement de l’application Android Kotlin/Jetpack Compose autour du projet **festivalapp**.

Il sert de base de travail structurée pour :
- l’organisation des dossiers ;
- les choix d’architecture ;
- les bonnes pratiques Kotlin et Android ;
- la mise en place de la navigation, de la couche réseau et de la persistance locale ;
- le respect des contraintes pédagogiques et techniques du projet.

---

## Références transmises

### Liens fournis
- https://medium.com/@sanjaykushwaha_58217/jwt-authentication-in-android-a-step-by-step-guide-d0dd768cb21a
- https://medium.com/yellowme/adding-ssl-certificates-into-your-android-app-with-retrofit-1a6ea9bd3b27
- https://medium.com/yellowme/adding-ssl-certificates-into-your-android-app-with-retrofit-1a6ea9bd3b27
- https://developer.android.com/codelabs/basic-android-kotlin-compose-datastore?hl=fr#4

### Documents fournis
- Lesson 1: Kotlin basics
- Lesson 2: Functions
- Lesson 3: Classes and objects
- Retrofit
- Navigation 3 step-by-step

---

## Exigences globales du projet Android

Développeur averti que vous êtes, votre application devra répondre aux bonnes pratiques suivantes :

### 1. Design et expérience utilisateur
- Respecter les règles de design **Material Design** de Google.
- Concevoir une interface claire, cohérente et moderne avec Jetpack Compose.
- Prévoir une UX cohérente en ligne et hors ligne.
- Assurer une navigation propre et lisible entre les écrans.
- Mutualiser les composants UI lorsque cela est pertinent.

### 2. Cycle de vie Android
L’application doit prendre en compte les évènements du cycle de vie, notamment :
- changements de configuration ;
- recréation d’activité ;
- destruction du process par l’OS ;
- restauration d’état ;
- persistance des données nécessaires à la continuité de l’expérience utilisateur.

### 3. Architecture
L’application doit être :
- **mono-activité** ;
- construite selon une **architecture à 3 couches** :
  - **UI**
  - **Domain** (optionnelle mais recommandée)
  - **Data**
- conforme au principe d’**indépendance des couches** ;
- conforme à **UDF (Unique Data Flow / flux de données unidirectionnel)** ;
- conforme aux principes **SOLID**, en particulier :
  - **D — Dependency Inversion Principle**

### 4. Offline first
L’application doit être pensée **offline first**.

Cela signifie :
- proposer une expérience utilisateur utile même sans connexion ;
- définir clairement ce qui est consultable, modifiable ou conservable hors ligne ;
- synchroniser les données locales et distantes lorsqu’une connexion est disponible ;
- éviter de faire dépendre toute l’interface du réseau en temps réel.

### 5. Organisation du travail recommandée
Il est recommandé de travailler de la manière suivante :
1. Recenser les écrans.
2. Identifier les composants mutualisables.
3. Recenser les données d’état.
4. Réaliser les ViewModels.
5. Concevoir la logique de navigation et les éléments d’interface associés.
6. Travailler simultanément sur la couche UI et la couche Data.
7. Travailler simultanément sur les couches de données locales et distantes (API).
8. Répartir le travail entre les membres de l’équipe tout en restant compétent sur l’ensemble de l’application.
9. Utiliser la **revue de code** comme outil de montée en compétence collective et de contrôle qualité.

---

## Contraintes d’architecture détaillées

### Mono-activité
L’application doit être conçue autour d’une seule activité hôte, avec une navigation interne gérée par Compose et son système de navigation.

### Architecture en 3 couches

#### Couche UI
Responsabilités :
- affichage des données ;
- collecte des interactions utilisateur ;
- rendu des états d’interface ;
- navigation ;
- délégation de la logique métier au ViewModel.

Ne doit pas :
- contenir de logique réseau directe ;
- manipuler directement des DTO de l’API ;
- contenir de logique métier complexe.

#### Couche Domain
Responsabilités :
- modèles métier propres ;
- règles métier ;
- contrats de repository ;
- éventuels use cases si nécessaire.

Caractéristiques :
- indépendante de l’UI ;
- indépendante de la couche réseau ;
- indépendante des détails techniques d’infrastructure.

#### Couche Data
Responsabilités :
- accès aux données distantes ;
- accès aux données locales ;
- mapping DTO → modèles métier ;
- implémentations concrètes des repositories ;
- gestion de la synchronisation.

### Indépendance des couches
Les couches doivent être découplées :
- la UI dépend d’abstractions du Domain ;
- le Domain ne dépend ni de l’UI ni des détails de Data ;
- Data implémente les contrats définis par Domain.

### UDF — Unique Data Flow
Le flux doit être unidirectionnel :
1. l’utilisateur déclenche une action ;
2. le ViewModel traite l’intention ;
3. le repository fournit ou modifie les données ;
4. le ViewModel met à jour l’état ;
5. l’UI observe l’état et se recompose.

### SOLID
Le projet doit respecter les principes SOLID, avec une attention particulière au **Dependency Inversion Principle** :
- les couches haut niveau ne doivent pas dépendre des implémentations concrètes ;
- les dépendances doivent cibler des interfaces ;
- l’injection de dépendances doit être privilégiée ;
- il faut éviter de créer directement les repositories dans les ViewModels.

---

## Organisation de l’arborescence

### Important
L’arborescence fournie sert **surtout de base d’organisation des dossiers**.

Les fichiers listés à l’intérieur :
- **ne sont pas définitifs** ;
- servent d’exemples d’organisation ;
- ne doivent pas être interprétés comme une liste figée ;
- peuvent évoluer selon les besoins fonctionnels et techniques du projet.

## Bonnes pratiques Kotlin à intégrer

Cette section synthétise les bonnes pratiques Kotlin utiles au projet à partir des ressources fournies.

### 1. Privilégier l’immutabilité
- Utiliser `val` par défaut.
- Réserver `var` aux cas réellement nécessaires.
- Considérer l’immutabilité comme la norme dans les modèles et états.

### 2. S’appuyer sur l’inférence de type avec discernement
- Laisser Kotlin inférer les types quand cela améliore la lisibilité.
- Déclarer explicitement les types lorsque cela clarifie l’intention publique d’une API.

### 3. Utiliser des fonctions courtes et ciblées
- Une fonction doit avoir un rôle clair.
- Préférer les fonctions d’expression simple lorsque cela améliore la lisibilité.
- Éviter les fonctions trop longues ou mélangeant plusieurs responsabilités.

### 4. Utiliser des arguments nommés et des paramètres par défaut
- Faciliter la lisibilité des appels de fonction.
- Réduire la surcharge inutile.
- Positionner les paramètres par défaut après les paramètres obligatoires lorsque cela a du sens.

### 5. Utiliser les data classes pour les objets de données
À privilégier pour :
- modèles d’état ;
- objets de transfert simples ;
- objets métier simples ;
- structures représentant un état ou une réponse.

### 6. Préférer des classes nommées à Pair/Triple dans le métier
- `Pair` et `Triple` existent, mais des data classes nommées sont généralement plus explicites.
- Le code métier gagne en lisibilité avec des noms de propriétés clairs.

### 7. Utiliser les interfaces pour les contrats
- Les repositories du Domain doivent être définis comme interfaces.
- Les implémentations concrètes restent en Data.
- Cela favorise le découplage, le test et l’inversion de dépendances.

### 8. Utiliser les extensions avec parcimonie et clarté
- Les fonctions d’extension sont utiles pour enrichir une API sans modifier la classe d’origine.
- Elles doivent être placées dans des fichiers bien nommés ou proches des types concernés.
- Elles ne doivent pas masquer une logique métier importante.

### 9. Exploiter les objets singleton quand pertinent
- `object` peut être utile pour des fournisseurs uniques, constantes ou utilitaires.
- À utiliser avec discipline pour éviter un couplage excessif.

### 10. Companion object uniquement si cela a un sens métier ou technique
- Pour des factories, constantes ou comportements partagés.
- Éviter de tout centraliser artificiellement dans les companion objects.

### 11. Utiliser les packages pour structurer clairement le code
- Les packages servent à organiser les responsabilités.
- Les noms doivent être cohérents, explicites et stables.
- La structure de packages doit refléter l’architecture.

### 12. Contrôler la visibilité
Utiliser les modificateurs de visibilité pour ne pas surexposer l’implémentation :
- `public` si nécessaire ;
- `private` par défaut quand possible ;
- `protected` seulement si justifié ;
- limiter l’API exposée.

### 13. Regrouper les entités liées quand cela reste lisible
Kotlin n’impose pas une entité par fichier.
Cela permet :
- de regrouper des structures proches ;
- de garder les éléments liés ensemble.

Mais il faut :
- éviter les fichiers trop longs ;
- conserver une bonne découvrabilité.

### 14. Utiliser les fonctions d’ordre supérieur de manière lisible
- Lambdas, fonctions d’ordre supérieur et transformations de collections sont puissantes.
- Les employer pour clarifier, pas pour complexifier.
- Éviter les chaînes de transformations opaques dans les parties critiques.

### 15. Faire attention aux collections
- Différencier collections immuables et mutables.
- Préférer l’immutabilité pour l’état exposé.
- Choisir des structures adaptées au besoin réel.

### 16. Respecter la null safety
- Tirer parti du système de types nullable/non-nullable.
- Limiter au maximum les nulls dans les modèles métier.
- Gérer les cas absents explicitement plutôt que propager des valeurs nulles sans contrôle.

---

## Bonnes pratiques Android / Compose / Architecture à intégrer

### 1. ViewModel centré sur l’état
Le ViewModel doit :
- exposer un état d’UI clair ;
- transformer les actions utilisateur en changements d’état ;
- appeler les repositories via des abstractions ;
- survivre aux changements de configuration.

Le ViewModel ne doit pas :
- contenir du code UI Compose ;
- dépendre directement de l’implémentation concrète d’un repository si une interface existe ;
- embarquer de la logique de navigation lourde couplée à l’UI.

### 2. États d’UI explicites
Les états d’UI doivent couvrir au minimum :
- chargement ;
- succès ;
- erreur ;
- éventuel état vide ;
- éventuels sous-états métier spécifiques.

L’usage de classes scellées ou d’états bien structurés est recommandé.

### 3. UDF avec Compose
L’UI :
- observe un état ;
- affiche selon cet état ;
- envoie des événements vers le ViewModel ;
- ne modifie pas directement les données métier.

### 4. Composables petits et réutilisables
- Découper l’UI en composants simples.
- Mutualiser les cartes, listes, items, formulaires, barres d’actions, etc.
- Séparer les écrans des composants atomiques.

### 5. Navigation pensée comme un état
- Organiser proprement les destinations.
- Concevoir une logique de navigation lisible.
- Préserver l’état de navigation si nécessaire.
- Prévoir un back stack maîtrisé.

### 6. Scaffold, TopAppBar, BottomAppBar
L’interface peut s’appuyer sur :
- `Scaffold`
- `TopAppBar` / `CenterAlignedTopAppBar`
- `BottomAppBar`
- `NavigationBar`

Ces éléments doivent être cohérents avec Material Design.

### 7. Enum ou objets pour les destinations
Les destinations de navigation peuvent être décrites via :
- enum ;
- sealed class ;
- objets de destination ;
- routes sérialisables si nécessaire.

### 8. Préserver l’état important
L’application doit prendre en compte :
- restauration d’écran ;
- reprise après changement de configuration ;
- conservation de la pile de navigation si pertinente ;
- rechargement intelligent des données.

---

## Bonnes pratiques Data / Réseau / Persistance

### 1. Retrofit
La couche réseau peut s’appuyer sur Retrofit avec Kotlin Serialization.

Points importants :
- définir une interface API par domaine ;
- centraliser la configuration réseau ;
- utiliser une base URL claire ;
- définir le converter adapté ;
- gérer les erreurs réseau proprement ;
- ne pas exposer les DTO à la UI.

### 2. Kotlin Serialization
À privilégier pour :
- sérialiser/désérialiser les DTO ;
- garder une intégration idiomatique Kotlin ;
- éviter les incohérences de mapping.

### 3. Repository pattern
Le repository doit servir d’interface de médiation entre :
- données distantes ;
- données locales ;
- logique d’orchestration des données.

### 4. Offline first
Il faut penser :
- cache local ;
- lecture locale prioritaire si pertinent ;
- synchronisation ensuite avec le distant ;
- comportement défini en absence de réseau.

### 5. DataStore
Le lien fourni vers le codelab DataStore implique un intérêt pour :
- la persistance locale légère ;
- la sauvegarde de préférences ;
- la conservation robuste de petits états ou réglages.

### 6. Authentification JWT
Le lien fourni sur la JWT authentication en Android indique une orientation vers :
- gestion sécurisée des tokens ;
- intégration côté client Android ;
- stratégie de stockage adaptée ;
- prise en compte du renouvellement de session si nécessaire.

### 7. Certificats SSL avec Retrofit
Les liens fournis sur l’ajout de certificats SSL dans une app Android avec Retrofit indiquent un intérêt pour :
- le renforcement de la sécurité réseau ;
- la validation de certificats ;
- la sécurisation des communications avec l’API.

---

## Consignes de modélisation concrète pour le projet

### Ce qu’il faut recenser avant d’implémenter
- les écrans ;
- les composants communs ;
- les états d’UI ;
- les événements utilisateur ;
- les flux de navigation ;
- les données distantes ;
- les données locales ;
- les points de synchronisation offline/online.

### Ce qu’il faut faire tôt dans le projet
- établir les ViewModels ;
- définir les contrats de repository ;
- concevoir les modèles métier ;
- créer les DTO ;
- poser la navigation ;
- structurer les packages.

### Ce qu’il faut surveiller durant le développement
- absence de dépendances inversées incorrectes ;
- non-mélange des responsabilités UI / Data ;
- cohérence des noms ;
- réutilisation des composants ;
- qualité des mappings ;
- résilience aux pertes de connexion ;
- persistance des états critiques ;
- lisibilité du code.

---

## Ligne directrice de qualité

### Principes à suivre
- code lisible ;
- responsabilités bien séparées ;
- architecture stable ;
- composants réutilisables ;
- états explicites ;
- dépendances orientées vers des abstractions ;
- expérience hors ligne définie ;
- cohérence Material Design ;
- navigation propre ;
- prise en compte du cycle de vie Android.

### Pratiques d’équipe
- revue de code régulière ;
- compréhension croisée des différentes couches ;
- répartition du travail sans spécialisation opaque ;
- homogénéité des conventions de code ;
- vigilance sur les régressions architecturales.

---

## Conclusion opérationnelle

Le projet doit être développé comme une application Android moderne en Kotlin/Jetpack Compose, mono-activité, organisée en trois couches, respectant UDF, SOLID et l’indépendance des couches, avec une vraie stratégie offline first.

L’arborescence fournie constitue une **base d’organisation des dossiers**, non définitive, à adapter selon les besoins réels du projet.

Les bonnes pratiques Kotlin, Compose, ViewModel, Retrofit, Navigation et DataStore doivent guider les choix d’implémentation afin d’obtenir une base de code :
- propre ;
- maintenable ;
- testable ;
- cohérente ;
- sécurisée ;
- évolutive.
