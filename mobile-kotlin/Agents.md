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
- en session active : `Festivals`, `Profil`
- Les écrans `forgot password`, `reset password`, `pending verification` et `verification result` ne sont pas des onglets.
- Le flux de reset est volontairement mobile-first ; aucun frontend web dédié n’est supposé dans ce projet.

## Attendus Navigation 3

- Le shell racine ne doit plus utiliser `NavController`, `NavHost` ni des routes string comme source de vérité de navigation.
- La navigation doit reposer sur des clés typées sérialisables déclarées dans [AppNavKey.kt](/Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/mobile-kotlin/app/src/main/java/com/projetmobile/mobile/ui/utils/navigation/AppNavKey.kt) via `sealed interface AppNavKey : NavKey`.
- Les quatre piles top-level obligatoires sont `Festivals`, `Login`, `Register` et `Profile`.
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
- session active : `Festivals`, `Profil`.
- Un logout réussi depuis `Profil` doit ramener explicitement sur `Connexion` et ne pas conserver de pile privée active.
- Un login réussi doit ramener sur `Festivals` et normaliser la pile `Login` à sa racine.
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
- Aucun `libs.versions.toml` ne doit être introduit dans ce chantier.
