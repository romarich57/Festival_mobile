# Task Plan: Cartographie backend/mobile auth, users, games, reservant

## Goal
Établir, à partir du code source, la cartographie des routes backend liées à `auth`, `profile/users`, `games` et `reservant`, identifier celles réellement consommées par le mobile Android, expliquer le modèle d’auth/session, et relier chaque flux aux services Android concernés.

## Current Phase
Phase 1

## Phases
### Phase 1: Requirements & Discovery
- [x] Understand user intent
- [x] Identify constraints and requirements
- [ ] Document findings in findings.md
- **Status:** in_progress

### Phase 2: Backend Route Inventory
- [ ] Inspect Express route registration and route files
- [ ] Extract auth/session behavior from middleware/config
- [ ] Document endpoint surface for requested domains
- **Status:** pending

### Phase 3: Android Usage Mapping
- [ ] Locate Android networking/services/repositories
- [ ] Identify backend endpoints actually called by mobile
- [ ] Map Android services to backend routes
- **Status:** pending

### Phase 4: Cross-check & Verification
- [ ] Reconcile backend surface with Android usage
- [ ] Note unused or mobile-only discrepancies
- [ ] Verify with tests or route references where relevant
- **Status:** pending

### Phase 5: Delivery
- [ ] Produce concise synthesis in French
- [ ] Include precise file references
- [ ] Call out uncertainties explicitly
- **Status:** pending

## Key Questions
1. Quelles routes sont exposées côté backend pour `auth`, `users/profile`, `games` et `reservant` ?
2. Quels endpoints sont réellement appelés depuis l’application Android, et par quels services/repositories ?
3. Quel est le modèle d’authentification/session effectif côté mobile: cookies HTTP-only, refresh token, JWT d’accès, stockage local ?

## Decisions Made
| Decision | Rationale |
|----------|-----------|
| Fonder l’analyse uniquement sur le code source | Requête de cartographie précise, sans hypothèse externe |
| Croiser backend et Android avant de conclure | Permet de distinguer surface exposée et usage réel |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
|       | 1       |            |

## Notes
- Référencer les fichiers avec lignes précises dans la réponse finale.
- Signaler explicitement les zones non utilisées par le mobile si elles existent.
