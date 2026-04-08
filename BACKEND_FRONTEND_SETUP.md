# Setup de travail: backend <-> frontend mobile Kotlin

## 1) Démarrer PostgreSQL

```bash
docker compose -f docker-compose.dev.yml up -d db
```

## 2) Configurer et lancer le backend

```bash
cd backend
cp .env.example .env
npm install
npm run dev
```

Test API:

```bash
curl http://localhost:4000/api/health
```

## 3) Configurer et builder le frontend Kotlin

```bash
cd mobile-kotlin
cp local.properties.example local.properties
./gradlew assembleDebug
```

APK debug:

`mobile-kotlin/app/build/outputs/apk/debug/app-debug.apk`

APK publique à distribuer:

```bash
cd mobile-kotlin
./gradlew assemblePublic
```

`mobile-kotlin/app/build/outputs/apk/public/app-public.apk`

## 4) URL backend côté Android

- Émulateur Android: `http://10.0.2.2:4000/api/`
- Téléphone réel: `http://<IP_LOCALE_DE_TA_MACHINE>:4000/api/`

## 5) Important sur CORS

Une app Android native Kotlin n'est généralement pas bloquée par CORS.
