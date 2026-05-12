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

## Android Kotlin Setup
 
### 1) Démarrer le backend
 
Depuis la racine `Projet_mobile` :
 
```bash
docker compose -f docker-compose.dev.yml up -d db
cd backend
cp .env.example .env
npm install
npm run dev
```
 
Vérification API :
 
```bash
curl http://localhost:4000/api/health
```
 
### 2) Configurer l'app Android
 
Depuis la racine `Projet_mobile/mobile-kotlin` :
 
```bash
cp local.properties.example local.properties
```
 
Ajuste `API_BASE_URL_DEBUG` si nécessaire :
 
- Émulateur Android : `http://10.0.2.2:4000/api/`
### 3) Build Android
 
```bash
cd mobile-kotlin
./gradlew assembleDebug
```
 
APK debug attendu :
```
app/build/outputs/apk/debug/app-debug.apk
```

---
*Projet universitaire (IG4 - Semestre 8)*
