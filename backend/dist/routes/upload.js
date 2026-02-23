// Role : Gerer les routes d'upload de fichiers.
import { Router } from 'express';
import multer from 'multer';
import path from 'node:path';
import fs from 'node:fs';
import { fileURLToPath } from 'node:url';
import { verifyToken } from '../middleware/token-management.js';
import { requireRole } from '../middleware/require-role.js';
const router = Router();
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
// Dossier d'upload des avatars
const uploadsDir = path.resolve(__dirname, '../../uploads/avatars');
// Dossier d'upload des images de jeux
const gamesImagesDir = path.resolve(__dirname, '../../uploads/games');
// Creer les dossiers s'ils n'existent pas
if (!fs.existsSync(uploadsDir)) {
    fs.mkdirSync(uploadsDir, { recursive: true });
}
if (!fs.existsSync(gamesImagesDir)) {
    fs.mkdirSync(gamesImagesDir, { recursive: true });
}
// Configuration de Multer pour les avatars
const avatarStorage = multer.diskStorage({
    destination(_req, _file, cb) {
        cb(null, uploadsDir);
    },
    filename(_req, file, cb) {
        const uniqueSuffix = `${Date.now()}-${Math.round(Math.random() * 1e9)}`;
        const ext = path.extname(file.originalname).toLowerCase();
        cb(null, `avatar-${uniqueSuffix}${ext}`);
    },
});
// Configuration de Multer pour les images de jeux
const gameImageStorage = multer.diskStorage({
    destination(_req, _file, cb) {
        cb(null, gamesImagesDir);
    },
    filename(_req, file, cb) {
        const uniqueSuffix = `${Date.now()}-${Math.round(Math.random() * 1e9)}`;
        const ext = path.extname(file.originalname).toLowerCase();
        cb(null, `game-${uniqueSuffix}${ext}`);
    },
});
// Role : Filtrer les types de fichiers autorises.
// Preconditions : file est fourni par Multer.
// Postconditions : Accepte ou refuse le fichier selon le MIME.
const fileFilter = (_req, file, cb) => {
    const allowedMimes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    if (allowedMimes.includes(file.mimetype)) {
        cb(null, true);
    }
    else {
        cb(new Error('Type de fichier non autorisé. Seules les images sont acceptées.'));
    }
};
const uploadAvatar = multer({
    storage: avatarStorage,
    fileFilter,
    limits: {
        fileSize: 2 * 1024 * 1024, // Max 2 Mo
    },
});
const uploadGameImage = multer({
    storage: gameImageStorage,
    fileFilter,
    limits: {
        fileSize: 2 * 1024 * 1024, // Max 2 Mo
    },
});
// Role : Uploader un avatar utilisateur.
// Preconditions : Utilisateur authentifie, fichier image valide.
// Postconditions : Retourne l'URL de l'avatar stocke.
router.post('/avatar', verifyToken, uploadAvatar.single('avatar'), (req, res) => {
    const file = req.file;
    if (!file) {
        return res.status(400).json({ error: 'Aucun fichier reçu' });
    }
    const avatarUrl = `/uploads/avatars/${file.filename}`;
    res.json({ url: avatarUrl, message: 'Avatar uploadé avec succès' });
});
// Role : Uploader une image de jeu.
// Preconditions : Utilisateur authentifie avec role backoffice, fichier image valide.
// Postconditions : Retourne l'URL de l'image stockee.
router.post('/game-image', verifyToken, requireRole(['admin', 'super-organizer', 'organizer']), uploadGameImage.single('image'), (req, res) => {
    const file = req.file;
    if (!file) {
        return res.status(400).json({ error: 'Aucun fichier reçu' });
    }
    const imageUrl = `/uploads/games/${file.filename}`;
    res.json({ url: imageUrl, message: 'Image de jeu uploadée avec succès' });
});
// Role : Uniformiser la gestion des erreurs Multer.
// Preconditions : err est une erreur Multer ou generique.
// Postconditions : Retourne une reponse JSON adaptee.
router.use((err, _req, res, next) => {
    if (err instanceof multer.MulterError) {
        if (err.code === 'LIMIT_FILE_SIZE') {
            return res.status(400).json({ error: 'Fichier trop volumineux (max 2MB)' });
        }
        return res.status(400).json({ error: err.message });
    }
    if (err) {
        return res.status(400).json({ error: err.message });
    }
    next();
});
export default router;
//# sourceMappingURL=upload.js.map