# backend/Dockerfile

# Étape 1 : Builder (Compilation du TypeScript)
FROM node:20-alpine AS builder

WORKDIR /app

# Copie des fichiers de dépendances
COPY package*.json ./
# Installation de TOUTES les dépendances (y compris devDependencies pour TypeScript)
RUN npm ci

# Copier le reste du code
COPY . .

# Compiler TypeScript vers JavaScript dans le dossier dist/
RUN npm run build

# Étape 2 : Production (Image légère)
FROM node:20-alpine AS runner

WORKDIR /app

# Copie uniquement des fichiers nécessaires
COPY package*.json ./
# Installer QUE les dépendances de production
RUN npm ci --omit=dev

# Récupérer les fichiers compilés depuis le builder
COPY --from=builder /app/dist ./dist

# Variables d'environnement par défaut
ENV NODE_ENV=production
ENV PORT=4000

EXPOSE 4000

# Commande de lancement en production
CMD ["npm", "run", "start"]
