# Findings & Decisions

## Requirements
- Analyser `backend/src` pour les domaines `auth`, `profile/users`, `games` et `reservant`.
- Cartographier les endpoints réellement utilisés par le mobile Android.
- Expliquer le modèle d’auth/session.
- Relier les endpoints backend aux services Android correspondants.
- Fournir des références de fichiers précises.

## Research Findings
- Le backend expose déjà les fichiers de routes ciblés: `backend/src/routes/auth.ts`, `backend/src/routes/users.ts`, `backend/src/routes/games.ts`, `backend/src/routes/reservant.ts`.
- Le projet Android semble vivre sous `mobile-kotlin/`; les appels réseau devront être cherchés là.
- Les points de montage globaux sont définis dans `backend/src/server.ts` avec les préfixes réels: `/api/auth`, `/api/users`, `/api/games`, `/api/reservant`.
- Les routes `users`, `games` et `reservant` sont protégées par `verifyToken` au niveau `server.ts`; `reservant` ajoute aussi `requireRole(BACKOFFICE_ROLES)` sur tout le routeur.
- Le modèle d’auth backend repose sur deux cookies HTTP-only signés JWT: `access_token` court (15 min) et `refresh_token` long (7 jours), avec rotation et révocation en base via `refresh_tokens`.
- Le mobile Android n’envoie pas de Bearer token: il persiste les cookies via `PersistentCookieJar` et tente automatiquement `POST auth/refresh` via `AuthRefreshInterceptor` après un `401/403`.
- Les endpoints Retrofit déclarés côté mobile couvrent `auth`, `users/me`, `games`, `reservant`, plus quelques endpoints adjacents `editors`, `mechanisms`, `upload/avatar`, `upload/game-image`.
- Les routes backend `auth/verify-email` et `auth/reset-password` ne sont pas appelées par Retrofit; elles servent de pont email/browser vers les deep links `festivalapp://auth/verification` et `festivalapp://auth/reset-password`.

## Technical Decisions
| Decision | Rationale |
|----------|-----------|
| Commencer par l’enregistrement global des routes dans `server.ts` | Permet d’obtenir les préfixes réels avant d’inspecter chaque route |
| Rechercher les usages Android par URL/segments de routes plutôt que par simple nom de fichier | Plus fiable pour distinguer endpoints réellement appelés |
| Séparer “déclaré dans Retrofit” de “utilisé par UI/repository” | Évite de surestimer les endpoints réellement actifs dans le mobile |

## Issues Encountered
| Issue | Resolution |
|-------|------------|
| `rg --files` sur plusieurs dossiers n’a listé que le backend au premier passage | Faire une inspection dédiée du module Android |
| Certains endpoints backend existent sans équivalent Android direct | Les documenter comme “non utilisés par le mobile” plutôt que les exclure silencieusement |

## Resources
- `backend/src/server.ts`
- `backend/src/routes/auth.ts`
- `backend/src/routes/users.ts`
- `backend/src/routes/games.ts`
- `backend/src/routes/reservant.ts`
- `mobile-kotlin/`

## Visual/Browser Findings
- Aucun.
