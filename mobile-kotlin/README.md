# Mobile Kotlin (Android)

Projet Android Kotlin minimal connecté au backend `Projet_mobile/backend`.

## Commandes

```bash
# depuis Projet_mobile/mobile-kotlin
cp local.properties.example local.properties
./gradlew assembleDebug
```

## Structure

- `app/src/main/java/com/projetmobile/mobile/MainActivity.kt`: écran de test API
- `app/src/main/java/com/projetmobile/mobile/network/*`: Retrofit + modèles
- `app/src/main/java/com/projetmobile/mobile/ui/*`: repository + viewmodel

## URL API

La base URL est injectée dans `BuildConfig.API_BASE_URL` via:

- `API_BASE_URL_DEBUG`
- `API_BASE_URL_RELEASE`

Définies dans `local.properties` (voir `local.properties.example`).
