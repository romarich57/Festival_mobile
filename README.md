# Projet Mobile : Gestion de Festival de Jeux

Bienvenue sur le dépôt du Projet Mobile. Cette application est destinée à la gestion d'un festival de jeux, constituée d'une application mobile Android (Kotlin / Jetpack Compose) et d'un backend Node.js (TypeScript).

## Structure du Projet

Le dépôt est divisé en deux parties principales :

### 1. Application Mobile (`/mobile-kotlin`)
Application Android native développée en Kotlin utilisant Jetpack Compose.
- Modèle d'architecture **MVVM** et **UDF** (Unidirectional Data Flow) via StateFlows.
- Gestion d'état UI réactif et composants UI hautement modulaires.
- Écrans d'authentification, de gestion de profil, des jeux, des réservants et de validation.

Voir le fichier `mobile-kotlin/ANDROID_SETUP.md` pour démarrer le projet sous Android Studio.

### 2. Backend (`/backend`)
API REST développée en Node.js et TypeScript avec une base de données PostgreSQL.
- Authentification avec JWT et rôles (Admin, Éditeur, etc.).
- Gestion des festivals, réservations, stands et jeux alloués.
- Scripts d'initialisation de la base de données.

Voir les directives d'installation dans `BACKEND_FRONTEND_SETUP.md` et le dossier `backend/`.

## Déploiement

Le projet est configuré avec des conteneurs via `docker-compose.dev.yml` et `docker-compose.prod.yml` pour le déploiement du backend et de ses bases de données.

---
*Projet universitaire (IG4 - Semestre 8)*
