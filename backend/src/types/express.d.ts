import type { TokenPayload } from './token-payload.js'

// Role : Etendre le type Express.Request avec les champs auth.
declare global {
  namespace Express {
    // On ajoute user au type Request d'Express
    interface Request {
      cookies?: Record<string, string>
      user?: TokenPayload // Peut etre defini par verifyToken
    }
  }
}

export {}


//C'est un fichier de configuration TypeScript qui permet à ton éditeur de code (VS Code) 
// et au compilateur de comprendre que l'objet req transporte maintenant les infos de l'utilisateur connecté, 
// et de te proposer l'autocomplétion (req.user.email, req.user.role, etc.) sans erreurs.
