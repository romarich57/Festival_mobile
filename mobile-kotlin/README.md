# Mobile Kotlin (Android)

Application Android Kotlin connectée au backend du projet situé dans `../backend`.

Ce README explique comment lancer l'application dans les cas suivants :

- backend avec PostgreSQL Docker
- backend avec PostgreSQL local, sans Docker
- lancement sur émulateur Android
- lancement sur téléphone réel en USB
- lancement sur téléphone réel en Wi-Fi

## Vue d'ensemble

Au démarrage, l'application ouvre l'onglet `Festivals` et tente immédiatement de charger `GET /api/festivals` depuis le backend.

La base URL Android est injectée dans `BuildConfig.API_BASE_URL` depuis `local.properties`.

En build `debug`, l'ordre de priorité est le suivant :

1. `API_BASE_URL_DEVICE`
2. `API_BASE_URL_DEBUG`
3. fallback par défaut : `http://10.0.2.2:4000/api/`

Conséquence importante :

- si `API_BASE_URL_DEVICE` est défini, il sera utilisé aussi en debug
- un émulateur peut donc casser si une ancienne IP de téléphone reste dans `local.properties`

## Prérequis

### Backend

- Node.js + npm
- un accès PostgreSQL
- Docker Desktop seulement si tu veux utiliser la base du `docker-compose.dev.yml`

### Android

- Android Studio
- Android SDK installé
- JDK 17
- au moins un AVD configuré si tu veux tester sur émulateur
- `adb` disponible si tu veux tester sur téléphone

## Arborescence utile

- `../backend` : API Node/TypeScript
- `./local.properties.example` : exemple de configuration Android locale
- `./app/build.gradle.kts` : injection de `BuildConfig.API_BASE_URL`
- `./app/src/debug/AndroidManifest.xml` : autorise HTTP en debug

## Important : ouvrir le bon projet dans Android Studio

Ouvre le dossier `mobile-kotlin/` dans Android Studio.

N'ouvre pas la racine `Projet_mobile/` pour développer l'app Android, car le repo contient aussi un Gradle racine distinct.

## Etape 1 : configurer et lancer le backend

### Cas A : backend avec PostgreSQL Docker

Depuis la racine du repo :

```bash
cd /Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile
docker compose -f docker-compose.dev.yml up -d db
```

Ce conteneur expose PostgreSQL sur `localhost:5433`.

Puis lance le backend :

```bash
cd backend
cp .env.example .env
npm install
npm run dev
```

Vérifie que l'API répond :

```bash
curl http://localhost:4000/api/health
```

### Cas B : backend sans Docker

Tu peux lancer le backend sans Docker si tu as déjà un PostgreSQL local ou distant.

Dans ce cas :

1. copie quand même le fichier d'environnement
2. modifie `DATABASE_URL` dans `backend/.env`
3. lance ensuite seulement le backend

Exemple :

```bash
cd /Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/backend
cp .env.example .env
```

Puis adapte la ligne :

```env
DATABASE_URL=postgresql://secureapp:secureapp_dev_password@localhost:5432/secureapp
```

Ensuite :

```bash
npm install
npm run dev
curl http://localhost:4000/api/health
```

### Ce que fait vraiment `npm run dev`

`npm run dev` :

- lance le backend Node/TypeScript
- n'installe pas Docker
- ne démarre pas PostgreSQL Docker

Donc si ta `DATABASE_URL` pointe vers `localhost:5433`, il faut avoir démarré Docker avant.

## Etape 2 : configurer Android

Depuis `mobile-kotlin/` :

```bash
cd /Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/mobile-kotlin
cp local.properties.example local.properties
```

Le fichier `local.properties` ne doit pas être versionné.

## Etape 3 : choisir le bon cas de lancement

### Cas 1 : lancement sur émulateur Android

Utilise cette configuration :

```properties
sdk.dir=/Users/ton_user/Library/Android/sdk
API_BASE_URL_DEBUG=http://10.0.2.2:4000/api/
API_BASE_URL_RELEASE=https://api.example.com/api/
```

Recommandation :

- supprime complètement `API_BASE_URL_DEVICE`, ou
- mets-la à la même valeur que `API_BASE_URL_DEBUG`

Pourquoi :

- `10.0.2.2` est l'adresse spéciale de l'hôte vue depuis l'émulateur Android
- si `API_BASE_URL_DEVICE` contient une vieille IP, elle prendra la priorité

Compile :

```bash
./gradlew assembleDebug
```

Puis lance l'app depuis Android Studio sur ton AVD.

### Cas 2 : lancement sur téléphone réel en USB

C'est le cas le plus fiable sur téléphone.

#### 1. Préparer le téléphone

- active les options développeur
- active le débogage USB
- branche le téléphone en USB

#### 2. Vérifier `adb`

```bash
adb devices -l
```

Le téléphone doit apparaître avec l'état `device`.

S'il apparaît en `offline`, il faut :

- débrancher/rebrancher
- accepter la clé RSA sur le téléphone
- redémarrer `adb` si nécessaire

#### 3. Créer le tunnel USB

```bash
adb reverse tcp:4000 tcp:4000
```

#### 4. Configurer `local.properties`

```properties
sdk.dir=/Users/ton_user/Library/Android/sdk
API_BASE_URL_DEVICE=http://127.0.0.1:4000/api/
API_BASE_URL_RELEASE=https://api.example.com/api/
```

Ensuite rebuild et lance l'app sur le téléphone.

Dans ce mode :

- `127.0.0.1` côté téléphone pointe vers le tunnel `adb reverse`
- tu n'as pas besoin de connaître l'IP locale du PC

### Cas 3 : lancement sur téléphone réel en Wi-Fi

Utilise ce cas si tu ne veux pas passer par USB.

#### 1. Réseau

- le téléphone et le PC doivent être sur le même Wi-Fi
- le backend doit tourner sur le PC

#### 2. Trouver l'IP locale du PC

Exemple :

- `192.168.1.12`
- `192.168.0.25`

#### 3. Configurer `local.properties`

```properties
sdk.dir=/Users/ton_user/Library/Android/sdk
API_BASE_URL_DEVICE=http://192.168.1.12:4000/api/
API_BASE_URL_RELEASE=https://api.example.com/api/
```

#### 4. Rebuild et lancer

```bash
./gradlew assembleDebug
```

Puis lance l'app sur le téléphone.

#### 5. Conditions pour que ça marche

- le backend doit écouter sur `0.0.0.0`
- le firewall de la machine ne doit pas bloquer le port `4000`
- l'IP utilisée doit être la bonne

## Etape 4 : commandes de build utiles

### Build debug

```bash
cd /Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/mobile-kotlin
./gradlew assembleDebug
```

APK attendu :

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Installation directe si un device est connecté

```bash
./gradlew installDebug
```

## Etape 5 : vérifications rapides avant de lancer l'app

### Checklist backend

- `backend/.env` existe
- `DATABASE_URL` pointe vers une base accessible
- le backend tourne
- `curl http://localhost:4000/api/health` répond

### Checklist Android

- `mobile-kotlin/local.properties` existe
- la bonne variable d'URL est renseignée
- l'URL choisie correspond bien au cas de lancement
- l'app a été rebuild après modification de `local.properties`

## Tableau de correspondance rapide

| Cas | URL à utiliser |
| --- | --- |
| Emulateur Android | `http://10.0.2.2:4000/api/` |
| Téléphone USB avec `adb reverse` | `http://127.0.0.1:4000/api/` |
| Téléphone Wi-Fi | `http://<IP_LOCALE_DU_PC>:4000/api/` |

## Erreurs fréquentes

### `Failed to connect to /10.0.2.2:4000`

Cela signifie généralement :

- backend non démarré
- backend crashé
- port `4000` indisponible
- mauvaise config réseau côté Android

Sur émulateur :

- `10.0.2.2` est correct
- vérifie surtout que le backend tourne

Sur téléphone réel :

- `10.0.2.2` est faux
- il faut soit `127.0.0.1` avec `adb reverse`, soit l'IP locale du PC

### L'app continue d'utiliser une mauvaise IP

Cause la plus probable :

- `API_BASE_URL_DEVICE` est encore présent dans `local.properties`

Comme cette variable est prioritaire en debug, elle peut écraser `API_BASE_URL_DEBUG`.

### Le backend démarre mal sans Docker

Cause probable :

- `DATABASE_URL` pointe encore vers `localhost:5433`
- mais PostgreSQL Docker n'est pas lancé

Solution :

- soit lancer Docker
- soit modifier `DATABASE_URL` vers ton Postgres local

### Le téléphone apparaît `offline` dans `adb devices`

Ca signifie que la connexion ADB n'est pas valide.

Actions typiques :

- rebrancher le câble
- accepter la demande de confiance sur le téléphone
- relancer `adb`

Exemple :

```bash
adb kill-server
adb start-server
adb devices -l
```

## Configuration minimale recommandée par cas

### Emulateur

```properties
sdk.dir=/Users/ton_user/Library/Android/sdk
API_BASE_URL_DEBUG=http://10.0.2.2:4000/api/
API_BASE_URL_RELEASE=https://api.example.com/api/
```

### Téléphone USB

```properties
sdk.dir=/Users/ton_user/Library/Android/sdk
API_BASE_URL_DEVICE=http://127.0.0.1:4000/api/
API_BASE_URL_RELEASE=https://api.example.com/api/
```

### Téléphone Wi-Fi

```properties
sdk.dir=/Users/ton_user/Library/Android/sdk
API_BASE_URL_DEVICE=http://192.168.1.12:4000/api/
API_BASE_URL_RELEASE=https://api.example.com/api/
```

## Workflow recommandé

### Workflow recommandé pour l'émulateur

```bash
cd /Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile
docker compose -f docker-compose.dev.yml up -d db

cd backend
cp .env.example .env
npm install
npm run dev

curl http://localhost:4000/api/health

cd ../mobile-kotlin
cp local.properties.example local.properties
./gradlew assembleDebug
```

Puis :

- vérifier que `API_BASE_URL_DEVICE` ne gêne pas
- lancer l'AVD
- lancer l'app

### Workflow recommandé pour téléphone USB

```bash
cd /Users/romarich/Desktop/IG-Travail/IG4/semestre_8/Projet_mobile/backend
npm run dev

curl http://localhost:4000/api/health

adb devices -l
adb reverse tcp:4000 tcp:4000

cd ../mobile-kotlin
./gradlew installDebug
```

## Notes utiles

- l'app Android autorise HTTP en debug
- la route des festivals est publique, donc aucun login n'est nécessaire pour tester l'écran de démarrage
- si le backend répond sur `/api/health` mais que l'app échoue encore, le problème est presque toujours l'URL choisie dans `local.properties`
