# Android Kotlin setup (projet généré)

## 1) Démarrer le backend

Depuis la racine `Projet_mobile`:

```bash
docker compose -f docker-compose.dev.yml up -d db
cd backend
cp .env.example .env
npm install
npm run dev
```

Note: ce setup utilise PostgreSQL Docker exposé en `localhost:5433` (pour éviter les conflits avec un PostgreSQL local sur `5432`).

Vérification API:

```bash
curl http://localhost:4000/api/health
```

## 2) Configurer l'app Android

Depuis la racine `Projet_mobile/mobile-kotlin`:

```bash
cp local.properties.example local.properties
```

Ajuste `API_BASE_URL_DEBUG` si nécessaire:

- Émulateur Android: `http://10.0.2.2:4000/api/`
- Téléphone réel: `http://<IP_LOCALE_DE_TA_MACHINE>:4000/api/`

## 3) Build Android

```bash
cd mobile-kotlin
./gradlew assembleDebug
```

APK debug attendu:

`app/build/outputs/apk/debug/app-debug.apk`

## 4) Ce qui est déjà prêt

- `BuildConfig.API_BASE_URL` injecté via `app/build.gradle.kts`
- Appel Retrofit vers `GET /health`
- Écran de test dans `MainActivity` avec état loading/success/error
- HTTP autorisé en debug (`app/src/debug/AndroidManifest.xml`)

## 5) CORS

CORS concerne surtout les frontends web. Une app Android native Kotlin n'est normalement pas bloquée par CORS.
