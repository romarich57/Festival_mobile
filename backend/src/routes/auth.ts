// Role : Gérer les routes d'authentification et de session.
import crypto from 'node:crypto'
import { Router } from 'express'
import type { Request, Response } from 'express'
import bcrypt from 'bcryptjs'
import jwt from 'jsonwebtoken'
import pool from '../db/database.js'
import {
  verifyToken,
  createAccessToken,
  createRefreshToken,
} from '../middleware/token-management.js'
import {
  JWT_SECRET,
  MOBILE_DEEP_LINK_BASE,
  MOBILE_PASSWORD_RESET_DEEP_LINK_BASE,
} from '../config/env.js'
import type { TokenPayload } from '../types/token-payload.js'
import { sendVerificationEmail, sendPasswordResetEmail } from '../services/email.js'

type DbUser = {
  id: number
  login: string
  password_hash: string
  role: string
  first_name: string
  last_name: string
  email: string
  phone: string | null
  avatar_url: string | null
  email_verified: boolean
  email_verification_token?: string | null
  email_verification_expires_at?: Date | null
  password_reset_token?: string | null
  password_reset_expires_at?: Date | null
  created_at: Date
}

type SafeUser = {
  id: number
  login: string
  role: string
  firstName: string
  lastName: string
  email: string
  phone: string | null
  avatarUrl: string | null
  emailVerified: boolean
  createdAt: Date
}

type VerificationFlowStatus = 'success' | 'expired' | 'invalid' | 'error'

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const PASSWORD_RESET_EXPIRATION_MS = 60 * 60 * 1000
const COOKIE_BASE_OPTIONS = {
  httpOnly: true,
  secure: true,
  sameSite: 'strict' as const,
}
const ACCESS_COOKIE_OPTIONS = {
  ...COOKIE_BASE_OPTIONS,
  maxAge: 15 * 60 * 1000,
}
const REFRESH_COOKIE_OPTIONS = {
  ...COOKIE_BASE_OPTIONS,
  maxAge: 7 * 24 * 60 * 60 * 1000,
}
const router = Router()

// Role : Construire un utilisateur sans champs sensibles.
// Preconditions : row provient de la table users.
// Postconditions : Retourne un SafeUser sans mot de passe ni tokens.
function toSafeUser(row: DbUser): SafeUser {
  return {
    id: row.id,
    login: row.login,
    role: row.role,
    firstName: row.first_name,
    lastName: row.last_name,
    email: row.email,
    phone: row.phone,
    avatarUrl: row.avatar_url,
    emailVerified: row.email_verified,
    createdAt: row.created_at,
  }
}

// Role : Nettoyer une valeur texte.
// Preconditions : value peut etre de tout type.
// Postconditions : Retourne une chaine nettoyee ou vide.
function sanitize(value: unknown): string {
  return typeof value === 'string' ? value.trim() : ''
}

// Role : Hasher un token de verification email.
// Preconditions : token est une chaine non vide.
// Postconditions : Retourne le hash SHA-256.
function hashVerificationToken(token: string): string {
  return crypto.createHash('sha256').update(token).digest('hex')
}

// Role : Hasher un identifiant de refresh token.
// Preconditions : tokenId est une chaine non vide.
// Postconditions : Retourne le hash SHA-256.
function hashRefreshTokenId(tokenId: string): string {
  return crypto.createHash('sha256').update(tokenId).digest('hex')
}

// Role : Construire le deep link mobile de retour vers l'application.
// Preconditions : status fait partie des statuts supportes.
// Postconditions : Retourne une URL deeplink avec le statut.
export function buildVerificationDeepLink(status: VerificationFlowStatus): string {
  const normalizedBase = MOBILE_DEEP_LINK_BASE.replace(/\/$/, '')

  try {
    const deepLink = new URL(normalizedBase)
    deepLink.searchParams.set('status', status)
    return deepLink.toString()
  } catch {
    const separator = normalizedBase.includes('?') ? '&' : '?'
    return `${normalizedBase}${separator}status=${encodeURIComponent(status)}`
  }
}

// Role : Construire le deep link mobile vers l'ecran de reinitialisation du mot de passe.
// Preconditions : token est une chaine non vide.
// Postconditions : Retourne une URL deeplink avec le token de reset.
export function buildPasswordResetDeepLink(token: string): string {
  const normalizedBase = MOBILE_PASSWORD_RESET_DEEP_LINK_BASE.replace(/\/$/, '')

  try {
    const deepLink = new URL(normalizedBase)
    deepLink.searchParams.set('token', token)
    return deepLink.toString()
  } catch {
    const separator = normalizedBase.includes('?') ? '&' : '?'
    return `${normalizedBase}${separator}token=${encodeURIComponent(token)}`
  }
}

// Role : Echaper une chaine avant injection HTML.
// Preconditions : value est une chaine.
// Postconditions : Retourne une version HTML-safe.
function escapeHtml(value: string): string {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

// Role : Fournir le contenu HTML de fallback lors de la verification email.
// Preconditions : status et deepLink sont valides.
// Postconditions : Retourne une page HTML minimale qui tente d'ouvrir l'app.
export function renderVerificationFallbackHtml({
  status,
  deepLink,
}: {
  status: VerificationFlowStatus
  deepLink: string
}): string {
  const safeDeepLink = escapeHtml(deepLink)
  const contentByStatus: Record<
    VerificationFlowStatus,
    { title: string; message: string }
  > = {
    success: {
      title: 'Email vérifié',
      message:
        'Votre adresse email a bien été confirmée. Nous essayons d’ouvrir l’application mobile.',
    },
    expired: {
      title: 'Lien expiré',
      message:
        'Le lien de vérification a expiré. Revenez dans l’application pour renvoyer un nouvel email.',
    },
    invalid: {
      title: 'Lien invalide',
      message:
        'Ce lien de vérification est invalide. Revenez dans l’application pour demander un nouvel email.',
    },
    error: {
      title: 'Vérification indisponible',
      message:
        'Une erreur est survenue pendant la vérification. Réessayez depuis l’application dans quelques instants.',
    },
  }
  const content = contentByStatus[status]
  const safeTitle = escapeHtml(content.title)
  const safeMessage = escapeHtml(content.message)

  return `<!DOCTYPE html>
<html lang="fr">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${safeTitle}</title>
    <style>
      body {
        margin: 0;
        min-height: 100vh;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
        background: linear-gradient(180deg, #f4f6fd 0%, #e8eef9 100%);
        color: #18233a;
        display: grid;
        place-items: center;
      }
      main {
        width: min(92vw, 460px);
        background: #ffffff;
        border-radius: 24px;
        box-shadow: 0 18px 50px rgba(24, 35, 58, 0.12);
        padding: 32px 24px;
      }
      h1 {
        margin: 0 0 16px;
        font-size: 2rem;
      }
      p {
        margin: 0 0 16px;
        line-height: 1.6;
      }
      a {
        display: inline-block;
        margin-top: 12px;
        padding: 14px 18px;
        border-radius: 14px;
        background: #6f96dd;
        color: #ffffff;
        text-decoration: none;
        font-weight: 600;
      }
      small {
        display: block;
        margin-top: 16px;
        color: #57657f;
      }
    </style>
  </head>
  <body>
    <main>
      <h1>${safeTitle}</h1>
      <p>${safeMessage}</p>
      <a href="${safeDeepLink}">Ouvrir l'application</a>
      <small>Si l’application ne s’ouvre pas automatiquement, utilisez le bouton ci-dessus.</small>
    </main>
    <script>
      window.setTimeout(function () {
        window.location.replace(${JSON.stringify(deepLink)});
      }, 120);
    </script>
  </body>
</html>`
}

// Role : Fournir le contenu HTML de fallback pour ouvrir l'ecran mobile de reset password.
// Preconditions : token est valide.
// Postconditions : Retourne une page HTML minimale qui tente d'ouvrir l'app.
export function renderPasswordResetFallbackHtml(token: string): string {
  const deepLink = buildPasswordResetDeepLink(token)
  const safeDeepLink = escapeHtml(deepLink)

  return `<!DOCTYPE html>
<html lang="fr">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Réinitialisation du mot de passe</title>
    <style>
      body {
        margin: 0;
        min-height: 100vh;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
        background: linear-gradient(180deg, #f4f6fd 0%, #e8eef9 100%);
        color: #18233a;
        display: grid;
        place-items: center;
      }
      main {
        width: min(92vw, 460px);
        background: #ffffff;
        border-radius: 24px;
        box-shadow: 0 18px 50px rgba(24, 35, 58, 0.12);
        padding: 32px 24px;
      }
      h1 {
        margin: 0 0 16px;
        font-size: 2rem;
      }
      p {
        margin: 0 0 16px;
        line-height: 1.6;
      }
      a {
        display: inline-block;
        margin-top: 12px;
        padding: 14px 18px;
        border-radius: 14px;
        background: #6f96dd;
        color: #ffffff;
        text-decoration: none;
        font-weight: 600;
      }
      small {
        display: block;
        margin-top: 16px;
        color: #57657f;
      }
    </style>
  </head>
  <body>
    <main>
      <h1>Réinitialiser votre mot de passe</h1>
      <p>Nous essayons d’ouvrir l’application mobile pour finaliser la réinitialisation du mot de passe.</p>
      <a href="${safeDeepLink}">Ouvrir l'application</a>
      <small>Si l’application ne s’ouvre pas automatiquement, utilisez le bouton ci-dessus.</small>
    </main>
    <script>
      window.setTimeout(function () {
        window.location.replace(${JSON.stringify(deepLink)});
      }, 120);
    </script>
  </body>
</html>`
}

// Role : Determiner si la reponse doit etre renvoyee sous forme HTML.
// Preconditions : req est une requete Express.
// Postconditions : Retourne true pour un navigateur/email client HTML.
function prefersHtmlResponse(req: Request): boolean {
  if (typeof req.accepts === 'function') {
    const preferredType = req.accepts(['html', 'json'])
    if (preferredType === 'html') {
      return true
    }
    if (preferredType === 'json') {
      return false
    }
  }

  const acceptHeader =
    typeof req.get === 'function' ? req.get('accept') ?? '' : ''
  return acceptHeader.includes('text/html') || acceptHeader.includes('application/xhtml+xml')
}

// Role : Renvoyer le resultat de verification vers l'application mobile ou une page fallback.
// Preconditions : status et httpStatus sont valides.
// Postconditions : Termine la reponse HTTP.
function respondWithVerificationResult(
  req: Request,
  res: Response,
  status: VerificationFlowStatus,
  httpStatus: number,
) {
  const deepLink = buildVerificationDeepLink(status)
  res.setHeader('Cache-Control', 'no-store')

  if (prefersHtmlResponse(req)) {
    return res
      .status(httpStatus)
      .type('html')
      .send(renderVerificationFallbackHtml({ status, deepLink }))
  }

  return res.redirect(deepLink)
}

type JwtDecoded = { exp?: number }

// Role : Extraire la date d'expiration d'un JWT.
// Preconditions : token est un JWT valide.
// Postconditions : Retourne une date ou null si exp manquant.
function getJwtExpiration(token: string): Date | null {
  const decoded = jwt.decode(token) as JwtDecoded | null
  if (!decoded?.exp) {
    return null
  }
  return new Date(decoded.exp * 1000)
}

// Role : Authentifier un utilisateur et poser les cookies JWT.
// Preconditions : identifier et password sont fournis.
// Postconditions : Retourne l'utilisateur et definit les cookies si succes.
router.post('/login', async (req, res) => {
  const identifier = sanitize(req.body?.identifier)
  const password = sanitize(req.body?.password)

  if (!identifier || !password) {
    return res.status(400).json({ error: 'Identifiants manquants' })
  }

  try {
    const { rows } = await pool.query<DbUser>(
      `
      SELECT
        id,
        login,
        password_hash,
        role,
        first_name,
        last_name,
        email,
        phone,
        avatar_url,
        email_verified,
        created_at
      FROM users
      WHERE login = $1 OR LOWER(email) = LOWER($2)
      LIMIT 1
    `,
      [identifier, identifier],
    )
    const user = rows[0]
    if (!user) {
      return res.status(401).json({ error: 'Identifiants invalides' })
    }

    const match = await bcrypt.compare(password, user.password_hash)
    if (!match) {
      return res.status(401).json({ error: 'Identifiants invalides' })
    }

    if (!user.email_verified) {
      return res.status(401).json({ error: 'Identifiants invalides' })
    }

    const payload: TokenPayload = {
      id: user.id,
      login: user.login,
      role: user.role,
    }
    const accessToken = createAccessToken(payload)
    const refreshTokenId = crypto.randomUUID()
    const refreshToken = createRefreshToken({ ...payload, jti: refreshTokenId })
    const refreshExpiresAt = getJwtExpiration(refreshToken)
    if (!refreshExpiresAt) {
      return res.status(500).json({ error: 'Erreur serveur' })
    }

    await pool.query(
      `
      INSERT INTO refresh_tokens (user_id, jti_hash, expires_at)
      VALUES ($1, $2, $3)
    `,
      [user.id, hashRefreshTokenId(refreshTokenId), refreshExpiresAt],
    )

    res.cookie('access_token', accessToken, ACCESS_COOKIE_OPTIONS)
    res.cookie('refresh_token', refreshToken, REFRESH_COOKIE_OPTIONS)

    res.json({
      message: 'Authentification réussie',
      user: toSafeUser(user),
    })
  } catch (error) {
    console.error('Erreur lors du login', error)
    res.status(500).json({ error: 'Erreur serveur' })
  }
})

// Role : Creer un compte utilisateur et envoyer l'email de verification.
// Preconditions : Les champs requis sont fournis et valides.
// Postconditions : Retourne un message de creation ou une erreur.
router.post('/register', async (req, res) => {
  const payload = {
    login: sanitize(req.body?.login),
    firstName: sanitize(req.body?.firstName),
    lastName: sanitize(req.body?.lastName),
    email: sanitize(req.body?.email).toLowerCase(),
    password: typeof req.body?.password === 'string' ? req.body.password : '',
    phone: sanitize(req.body?.phone),
    avatarUrl: sanitize(req.body?.avatarUrl),
  }

  if (
    !payload.login ||
    !payload.firstName ||
    !payload.lastName ||
    !payload.email ||
    payload.password.trim().length === 0
  ) {
    return res.status(400).json({ error: 'Champs obligatoires manquants' })
  }

  if (!EMAIL_REGEX.test(payload.email)) {
    return res.status(400).json({ error: 'Email invalide' })
  }

  try {
    const { rows: existing } = await pool.query<{ login: string; email: string | null }>(
      `
      SELECT login, email
      FROM users
      WHERE login = $1 OR LOWER(email) = LOWER($2)
      LIMIT 1
    `,
      [payload.login, payload.email],
    )

    const duplicate = existing[0]
    if (duplicate) {
      if (duplicate.login === payload.login) {
        return res.status(409).json({ error: 'Login déjà utilisé' })
      }
      const existingEmail = duplicate.email?.toLowerCase()
      if (existingEmail && existingEmail === payload.email) {
        return res.status(409).json({ error: 'Email déjà utilisé' })
      }
    }

    const passwordHash = await bcrypt.hash(payload.password, 10)
    const token = crypto.randomBytes(32).toString('hex')
    const tokenHash = hashVerificationToken(token)
    const expiresAt = new Date(Date.now() + 24 * 60 * 60 * 1000)

    const client = await pool.connect()
    try {
      await client.query('BEGIN')
      await client.query(
        `
        INSERT INTO users (
          login,
          password_hash,
          role,
          first_name,
          last_name,
          email,
          phone,
          avatar_url,
          email_verified,
          email_verification_token,
          email_verification_expires_at
        ) VALUES (
          $1, $2, 'benevole', $3, $4, $5, $6, $7, FALSE, $8, $9
        )
      `,
        [
          payload.login,
          passwordHash,
          payload.firstName,
          payload.lastName,
          payload.email,
          payload.phone || null,
          payload.avatarUrl || null,
          tokenHash,
          expiresAt,
        ],
      )

      await sendVerificationEmail(payload.email, token)
      await client.query('COMMIT')
    } catch (error: any) {
      await client.query('ROLLBACK')
      if (error?.code === '23505') {
        return res.status(409).json({ error: 'Login ou email déjà utilisé' })
      }
      console.error('Erreur lors de la création du compte', error)
      return res.status(500).json({ error: 'Erreur lors de la création du compte' })
    } finally {
      client.release()
    }

    res.status(201).json({
      message: 'Compte créé. Veuillez vérifier votre email pour activer votre compte.',
    })
  } catch (error) {
    console.error('Erreur validation inscription', error)
    res.status(500).json({ error: 'Erreur serveur' })
  }
})

// Role : Renvoyer un email de verification.
// Preconditions : email est fourni et valide.
// Postconditions : Retourne un message de traitement.
router.post('/resend-verification', async (req, res) => {
  const email = sanitize(req.body?.email).toLowerCase()
  if (!email || !EMAIL_REGEX.test(email)) {
    return res.status(400).json({ error: 'Email invalide' })
  }

  try {
    const { rows } = await pool.query<{ id: number; email_verified: boolean }>(
      `
      SELECT id, email_verified
      FROM users
      WHERE LOWER(email) = LOWER($1)
      LIMIT 1
    `,
      [email],
    )

    const user = rows[0]
    if (!user) {
      return res.json({
        message:
          'Si un compte non vérifié existe pour cet email, un lien vient d’être renvoyé.',
      })
    }

    if (user.email_verified) {
      return res.json({
        message: 'Ce compte est déjà vérifié. Vous pouvez vous connecter.',
      })
    }

    const token = crypto.randomBytes(32).toString('hex')
    const tokenHash = hashVerificationToken(token)
    const expiresAt = new Date(Date.now() + 24 * 60 * 60 * 1000)

    await pool.query(
      `
      UPDATE users
      SET email_verification_token = $1,
          email_verification_expires_at = $2
      WHERE id = $3
    `,
      [tokenHash, expiresAt, user.id],
    )

    await sendVerificationEmail(email, token)

    res.json({
      message: 'Un nouvel email de vérification vient d’être envoyé.',
    })
  } catch (error) {
    console.error('Erreur renvoi email vérification', error)
    res.status(500).json({ error: 'Erreur serveur' })
  }
})

// Role : Verifier un email via un token.
// Preconditions : token est fourni.
// Postconditions : Active le compte et retourne l'utilisateur.
router.get('/verify-email', async (req, res) => {
  const token = sanitize(req.query?.token)
  if (!token) {
    return respondWithVerificationResult(req, res, 'invalid', 400)
  }

  const tokenHash = hashVerificationToken(token)

  try {
    const { rows: matches } = await pool.query<{
      id: number
      email_verification_expires_at: Date | null
    }>(
      `
      SELECT id, email_verification_expires_at
      FROM users
      WHERE email_verification_token = $1
      LIMIT 1
    `,
      [tokenHash],
    )

    const tokenMatch = matches[0]
    if (!tokenMatch) {
      return respondWithVerificationResult(req, res, 'invalid', 400)
    }

    if (
      !tokenMatch.email_verification_expires_at ||
      tokenMatch.email_verification_expires_at.getTime() <= Date.now()
    ) {
      return respondWithVerificationResult(req, res, 'expired', 400)
    }

    const { rows } = await pool.query<DbUser>(
      `
      UPDATE users
      SET email_verified = TRUE,
          email_verification_token = NULL,
          email_verification_expires_at = NULL
      WHERE id = $2
        AND email_verification_token = $1
        AND email_verification_expires_at > NOW()
      RETURNING
        id,
        login,
        password_hash,
        role,
        first_name,
        last_name,
        email,
        phone,
        avatar_url,
        email_verified,
        created_at
    `,
      [tokenHash, tokenMatch.id],
    )

    const user = rows[0]
    if (!user) {
      return respondWithVerificationResult(req, res, 'invalid', 400)
    }

    return respondWithVerificationResult(req, res, 'success', 200)
  } catch (error) {
    console.error('Erreur vérification email', error)
    return respondWithVerificationResult(req, res, 'error', 500)
  }
})

// Role : Rediriger un lien email de reset password vers l'application mobile.
// Preconditions : token est fourni dans la query string.
// Postconditions : Redirige vers le deeplink mobile ou renvoie une page HTML fallback.
router.get('/reset-password', async (req, res) => {
  const token = sanitize(req.query?.token)
  if (!token) {
    return res.status(400).json({ error: 'Token manquant' })
  }

  const deepLink = buildPasswordResetDeepLink(token)
  res.setHeader('Cache-Control', 'no-store')

  if (prefersHtmlResponse(req)) {
    return res.status(200).type('html').send(renderPasswordResetFallbackHtml(token))
  }

  return res.redirect(deepLink)
})

// Role : Demander une reinitialisation de mot de passe.
// Preconditions : email est fourni et valide.
// Postconditions : Enregistre un token et envoie un email.
router.post('/password/forgot', async (req, res) => {
  const email = sanitize(req.body?.email).toLowerCase()
  if (!email || !EMAIL_REGEX.test(email)) {
    return res.status(400).json({ error: 'Email invalide' })
  }

  try {
    const { rows } = await pool.query<{ id: number }>(
      `
      SELECT id
      FROM users
      WHERE LOWER(email) = LOWER($1)
      LIMIT 1
    `,
      [email],
    )

    const user = rows[0]
    if (!user) {
      return res.json({
        message:
          'Si un compte existe pour cet email, un lien de réinitialisation a été envoyé.',
      })
    }

    const token = crypto.randomBytes(32).toString('hex')
    const tokenHash = hashVerificationToken(token)
    const expiresAt = new Date(Date.now() + PASSWORD_RESET_EXPIRATION_MS)

    await pool.query(
      `
      UPDATE users
      SET password_reset_token = $1,
          password_reset_expires_at = $2
      WHERE id = $3
    `,
      [tokenHash, expiresAt, user.id],
    )


    await sendPasswordResetEmail(email, token)

    res.json({
      message:
        'Si un compte existe pour cet email, un lien de réinitialisation vient d’être envoyé.',
    })
  } catch (error) {
    console.error('Erreur demande réinitialisation', error)
    res.status(500).json({ error: 'Erreur serveur' })
  }
})

// Role : Reinitialiser le mot de passe a partir d'un token.
// Preconditions : token et nouveau mot de passe sont fournis.
// Postconditions : Met a jour le mot de passe et invalide les refresh tokens.
router.post('/password/reset', async (req, res) => {
  const token = sanitize(req.body?.token)
  const password =
    typeof req.body?.password === 'string' ? req.body.password.trim() : ''

  if (!token || !password) {
    return res.status(400).json({ error: 'Token et mot de passe requis' })
  }

  if (password.length < 8) {
    return res.status(400).json({ error: 'Mot de passe trop court (8 caractères min.)' })
  }

  const tokenHash = hashVerificationToken(token)

  try {
    const passwordHash = await bcrypt.hash(password, 10)
    const client = await pool.connect()
    try {
      await client.query('BEGIN')
      const { rows } = await client.query<DbUser>(
        `
        UPDATE users
        SET password_hash = $1,
            password_reset_token = NULL,
            password_reset_expires_at = NULL
        WHERE password_reset_token = $2
          AND password_reset_expires_at > NOW()
        RETURNING
          id,
          login,
          password_hash,
          role,
          first_name,
          last_name,
          email,
          phone,
          avatar_url,
          email_verified,
          created_at
      `,
        [passwordHash, tokenHash],
      )

      const user = rows[0]
      if (!user) {
        await client.query('ROLLBACK')
        return res.status(400).json({ error: 'Token invalide ou expiré' })
      }

      await client.query(
        'UPDATE refresh_tokens SET revoked_at = NOW() WHERE user_id = $1 AND revoked_at IS NULL',
        [user.id],
      )

      await client.query('COMMIT')
      res.json({ message: 'Mot de passe mis à jour. Vous pouvez vous connecter.' })
    } catch (error) {
      await client.query('ROLLBACK')
      throw error
    } finally {
      client.release()
    }
  } catch (error) {
    console.error('Erreur réinitialisation mot de passe', error)
    res.status(500).json({ error: 'Erreur serveur' })
  }
})

// Role : Deconnecter l'utilisateur courant.
// Preconditions : Aucune.
// Postconditions : Revoque le refresh token si present et nettoie les cookies.
router.post('/logout', async (req, res) => {
  const refresh = req.cookies?.refresh_token
  if (refresh) {
    try {
      const decoded = jwt.verify(refresh, JWT_SECRET) as TokenPayload
      if (decoded?.jti) {
        await pool.query(
          'UPDATE refresh_tokens SET revoked_at = NOW() WHERE jti_hash = $1',
          [hashRefreshTokenId(decoded.jti)],
        )
      }
    } catch (error) {
      console.warn('Refresh token invalide lors du logout', error)
    }
  }

  res.clearCookie('access_token', COOKIE_BASE_OPTIONS)
  res.clearCookie('refresh_token', COOKIE_BASE_OPTIONS)
  res.json({ message: 'Déconnexion réussie' })
})

// Role : Renouveler les tokens via le refresh token.
// Preconditions : refresh_token est present et valide.
// Postconditions : Retourne un nouveau access token et refresh token.
router.post('/refresh', async (req, res) => {
  const refresh = req.cookies?.refresh_token
  if (!refresh) {
    return res.status(401).json({ error: 'Refresh token manquant' })
  }

  let decoded: TokenPayload
  try {
    decoded = jwt.verify(refresh, JWT_SECRET) as TokenPayload
  } catch {
    return res.status(403).json({ error: 'Refresh token invalide ou expiré' })
  }

  if (!decoded?.jti) {
    return res.status(403).json({ error: 'Refresh token invalide ou expiré' })
  }

  try {
    const { rows } = await pool.query<{
      id: number
      user_id: number
      expires_at: Date
      revoked_at: Date | null
    }>(
      `
      SELECT id, user_id, expires_at, revoked_at
      FROM refresh_tokens
      WHERE jti_hash = $1
    `,
      [hashRefreshTokenId(decoded.jti)],
    )

    const stored = rows[0]
    if (!stored) {
      await pool.query(
        'UPDATE refresh_tokens SET revoked_at = NOW() WHERE user_id = $1 AND revoked_at IS NULL',
        [decoded.id],
      )
      return res.status(403).json({ error: 'Refresh token invalide ou expiré' })
    }

    const storedExpiresAt = new Date(stored.expires_at)
    if (
      stored.revoked_at ||
      stored.user_id !== decoded.id ||
      Number.isNaN(storedExpiresAt.getTime()) ||
      storedExpiresAt <= new Date()
    ) {
      await pool.query(
        'UPDATE refresh_tokens SET revoked_at = NOW() WHERE user_id = $1 AND revoked_at IS NULL',
        [decoded.id],
      )
      return res.status(403).json({ error: 'Refresh token invalide ou expiré' })
    }

    const newRefreshId = crypto.randomUUID()
    const newRefreshToken = createRefreshToken({
      id: decoded.id,
      login: decoded.login,
      role: decoded.role,
      jti: newRefreshId,
    })
    const refreshExpiresAt = getJwtExpiration(newRefreshToken)
    if (!refreshExpiresAt) {
      return res.status(500).json({ error: 'Erreur serveur' })
    }

    const client = await pool.connect()
    try {
      await client.query('BEGIN')
      await client.query('UPDATE refresh_tokens SET revoked_at = NOW() WHERE id = $1', [
        stored.id,
      ])
      await client.query(
        `
        INSERT INTO refresh_tokens (user_id, jti_hash, expires_at)
        VALUES ($1, $2, $3)
      `,
        [decoded.id, hashRefreshTokenId(newRefreshId), refreshExpiresAt],
      )
      await client.query('COMMIT')
    } catch (error) {
      await client.query('ROLLBACK')
      throw error
    } finally {
      client.release()
    }

    const newAccess = createAccessToken({
      id: decoded.id,
      login: decoded.login,
      role: decoded.role,
    })
    res.cookie('access_token', newAccess, ACCESS_COOKIE_OPTIONS)
    res.cookie('refresh_token', newRefreshToken, REFRESH_COOKIE_OPTIONS)
    res.json({ message: 'Token renouvelé' })
  } catch (error) {
    console.error('Erreur lors du refresh token', error)
    res.status(500).json({ error: 'Erreur serveur' })
  }
})

// Role : Retourner l'utilisateur courant.
// Preconditions : L'utilisateur est authentifie.
// Postconditions : Retourne les informations de l'utilisateur ou une erreur.
router.get('/whoami', verifyToken, async (req, res) => {
  if (!req.user) {
    return res.status(401).json({ error: 'Utilisateur non authentifié' })
  }

  try {
    const { rows } = await pool.query<DbUser>(
      `
      SELECT
        id,
        login,
        password_hash,
        role,
        first_name,
        last_name,
        email,
        phone,
        avatar_url,
        email_verified,
        created_at
      FROM users
      WHERE id = $1
      LIMIT 1
    `,
      [req.user.id],
    )

    const user = rows[0]
    if (!user) {
      return res.status(404).json({ error: 'Utilisateur introuvable' })
    }

    res.json({ user: toSafeUser(user) })
  } catch (error) {
    console.error('Erreur whoami', error)
    res.status(500).json({ error: 'Erreur serveur' })
  }
})

export default router
