// Role : Gérer les routes des jeux.
import { Router } from 'express'
import type { Pool, PoolClient } from 'pg'
import pool from '../db/database.js'
import { requireRole } from '../middleware/require-role.js'

type MechanismResponse = { id: number; name: string; description: string | null }

type GameRecord = {
  id: number
  title: string
  type: string
  editor_id: number | null
  editor_name: string | null
  min_age: number
  authors: string
  min_players: number | null
  max_players: number | null
  prototype: boolean
  duration_minutes: number | null
  theme: string | null
  description: string | null
  image_url: string | null
  rules_video_url: string | null
  mechanisms: MechanismResponse[]
}

type GamePayload = Partial<Omit<GameRecord, 'id' | 'editor_name' | 'mechanisms'>> & {
  mechanismIds?: number[]
}

type GameListResponse = {
  items: GameRecord[]
  pagination: {
    page: number
    limit: number
    total: number
    totalPages: number
    sortBy: string
    sortOrder: 'asc' | 'desc'
  }
}

const BACKOFFICE_ROLES = ['admin', 'super-organizer', 'organizer']
const DEFAULT_PAGE = 1
const DEFAULT_LIMIT = 20
const MAX_LIMIT = 100
const backofficeOnly = requireRole(BACKOFFICE_ROLES)

const router = Router()

// Role : Convertir une valeur en entier nullable.
// Preconditions : value est un nombre ou une chaine convertible.
// Postconditions : Retourne un entier, null ou undefined.
function toNullableInt(value: unknown): number | null | undefined {
  if (value === undefined) return undefined
  if (value === null) return null
  if (typeof value === 'number') return Number.isFinite(value) ? value : undefined
  if (typeof value === 'string' && value.trim().length > 0) {
    const num = Number(value.trim())
    return Number.isFinite(num) ? num : undefined
  }
  return undefined
}

// Role : Nettoyer une chaine et retourner null si vide.
// Preconditions : value peut etre une chaine, null ou undefined.
// Postconditions : Retourne une chaine nettoyee, null ou undefined.
function toNullableString(value: unknown): string | null | undefined {
  if (value === undefined) return undefined
  if (value === null) return null
  if (typeof value === 'string') {
    const trimmed = value.trim()
    return trimmed.length > 0 ? trimmed : null
  }
  return undefined
}

// Role : Convertir une valeur en booleen.
// Preconditions : value peut etre string/number/boolean.
// Postconditions : Retourne true/false ou undefined si non interpretable.
function toBoolean(value: unknown): boolean | undefined {
  if (value === undefined) return undefined
  if (typeof value === 'boolean') return value
  if (typeof value === 'number') return value !== 0
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase()
    if (['true', '1', 'oui', 'yes'].includes(normalized)) return true
    if (['false', '0', 'non', 'no'].includes(normalized)) return false
  }
  return undefined
}

// Role : Extraire une liste d'identifiants de mecanismes.
// Preconditions : value doit etre un tableau de valeurs numeriques.
// Postconditions : Retourne une liste d'IDs uniques ou undefined.
function parseMechanismIds(value: unknown): number[] | undefined {
  if (value === undefined) return undefined
  if (!Array.isArray(value)) return undefined
  const ids = value
    .map((item) => Number(item))
    .filter((num) => Number.isFinite(num))
    .map((num) => Math.trunc(num))
  return Array.from(new Set(ids))
}

type ErrorResponseBody = { error: string; details?: string[] }

// Role : Normaliser le champ details pour les réponses d'erreur.
// Preconditions : details peut être string, tableau ou undefined.
// Postconditions : Retourne un tableau de détails propre ou undefined.
function normalizeErrorDetails(details?: string | string[] | null): string[] | undefined {
  if (details == null) return undefined
  const normalized = (Array.isArray(details) ? details : [details])
    .map((detail) => detail.trim())
    .filter((detail) => detail.length > 0)
  return normalized.length > 0 ? normalized : undefined
}

function buildErrorBody(error: string, details?: string | string[] | null): ErrorResponseBody {
  const normalizedDetails = normalizeErrorDetails(details)
  return normalizedDetails ? { error, details: normalizedDetails } : { error }
}

// Role : Normaliser les erreurs Postgres vers des reponses HTTP propres.
// Preconditions : err est une erreur issue d'une requete pg.
// Postconditions : Retourne une reponse HTTP ou null si non geree.
type DatabaseErrorResponse = { status: number; body: ErrorResponseBody }

function mapDatabaseError(err: any): DatabaseErrorResponse | null {
  const code = err?.code as string | undefined
  const constraint = err?.constraint as string | undefined
  const detail = err?.detail as string | undefined
  const withDetails = (error: string, details?: string | string[]): DatabaseErrorResponse => ({
    status: 400,
    body: buildErrorBody(error, details),
  })

  if (code === '23505') {
    if (constraint === 'games_title_key') {
      return { status: 409, body: buildErrorBody('Titre déjà utilisé') }
    }
    return { status: 409, body: buildErrorBody('Conflit de duplication', detail) }
  }
  if (code === '23503') {
    if (constraint === 'games_editor_id_fkey') {
      return { status: 400, body: buildErrorBody("Éditeur inexistant") }
    }
    return { status: 400, body: buildErrorBody('Référence inexistante', detail) }
  }
  if (code === '23502') {
    return withDetails('Champ requis manquant', detail)
  }
  if (code === '23514') {
    return withDetails('Violation de contrainte', detail)
  }
  if (code === '22P02') {
    return withDetails('Format invalide', detail)
  }

  return null
}

// Role : Valider et normaliser le payload de jeu.
// Preconditions : body est l'objet req.body.
// Postconditions : Retourne les donnees nettoyees et la liste d'erreurs.
function parseGameBody(body: any, requireBasics: boolean): { data: GamePayload; errors: string[] } {
  const errors: string[] = []
  const data: GamePayload = {}

  const title = toNullableString(body?.title)
  const type = toNullableString(body?.type)
  const authors = toNullableString(body?.authors)
  const theme = toNullableString(body?.theme)
  const description = toNullableString(body?.description)
  const imageUrl = toNullableString(body?.image_url)
  const rulesVideoUrl = toNullableString(body?.rules_video_url)
  const editorId = toNullableInt(body?.editor_id)
  const minAge = toNullableInt(body?.min_age)
  const minPlayers = toNullableInt(body?.min_players)
  const maxPlayers = toNullableInt(body?.max_players)
  const durationMinutes = toNullableInt(body?.duration_minutes)
  const prototype = toBoolean(body?.prototype)
  const mechanismIds = parseMechanismIds(body?.mechanismIds)

  if (title !== undefined) data.title = title ?? ''
  if (type !== undefined) data.type = type ?? ''
  if (authors !== undefined) data.authors = authors ?? ''
  if (theme !== undefined) data.theme = theme
  if (description !== undefined) data.description = description
  if (imageUrl !== undefined) data.image_url = imageUrl
  if (rulesVideoUrl !== undefined) data.rules_video_url = rulesVideoUrl
  if (editorId !== undefined) data.editor_id = editorId
  if (minAge !== undefined) data.min_age = minAge ?? 0
  if (minPlayers !== undefined) data.min_players = minPlayers
  if (maxPlayers !== undefined) data.max_players = maxPlayers
  if (durationMinutes !== undefined) data.duration_minutes = durationMinutes
  if (prototype !== undefined) data.prototype = prototype
  if (mechanismIds !== undefined) data.mechanismIds = mechanismIds

  if (requireBasics) {
    if (!title) errors.push('title est requis')
    if (!type) errors.push('type est requis')
    if (minAge === undefined || minAge === null) errors.push('min_age est requis')
    if (!authors) errors.push('authors est requis')
    if (editorId === undefined || editorId === null) errors.push('editor_id est requis')
  }

  if (minAge !== undefined && (minAge === null || minAge < 0)) {
    errors.push('min_age doit être positif')
  }
  if (minPlayers !== undefined && minPlayers !== null && minPlayers < 1) {
    errors.push('min_players doit être supérieur ou égal à 1')
  }
  if (maxPlayers !== undefined && maxPlayers !== null && maxPlayers < 1) {
    errors.push('max_players doit être supérieur ou égal à 1')
  }
  if (
    minPlayers !== undefined &&
    maxPlayers !== undefined &&
    minPlayers !== null &&
    maxPlayers !== null &&
    minPlayers > maxPlayers
  ) {
    errors.push('min_players ne peut pas être supérieur à max_players')
  }
  if (durationMinutes !== undefined && durationMinutes !== null && durationMinutes < 0) {
    errors.push('duration_minutes doit être positif')
  }

  return { data, errors }
}

// Role : Construire les filtres SQL a partir de la query.
// Preconditions : query contient les parametres de filtre.
// Postconditions : Retourne la clause WHERE et les params.
function buildFilters(query: any): { where: string; params: any[] } {
  const filters: string[] = []
  const params: any[] = []

  if (query.title) {
    params.push(`%${query.title.toString().toLowerCase()}%`)
    filters.push(`LOWER(g.title) LIKE $${params.length}`)
  }
  if (query.type) {
    params.push(query.type.toString())
    filters.push(`g.type = $${params.length}`)
  }
  if (query.editor_id !== undefined && query.editor_id !== null && query.editor_id !== '') {
    params.push(Number(query.editor_id))
    filters.push(`g.editor_id = $${params.length}`)
  }
  if (query.min_age !== undefined && query.min_age !== null && query.min_age !== '') {
    params.push(Number(query.min_age))
    filters.push(`g.min_age >= $${params.length}`)
  }

  const where = filters.length > 0 ? `WHERE ${filters.join(' AND ')}` : ''
  return { where, params }
}

// Role : Convertir une pagination de query en entier borne.
// Preconditions : value peut être null/undefined/string.
// Postconditions : Retourne un entier valide ou undefined.
function toPositiveInt(value: unknown): number | undefined {
  const parsed = toNullableInt(value)
  if (parsed === undefined || parsed === null) return undefined
  if (parsed < 1) return undefined
  return parsed
}

const SORT_FIELD_MAP: Record<string, string> = {
  title: 'LOWER(g.title)',
  min_age: 'g.min_age',
  editor_name: "LOWER(COALESCE(e.name, ''))",
}

// Role : Normaliser le tri demandé.
// Preconditions : La query peut utiliser sortBy/sortOrder ou l'ancien paramètre sort.
// Postconditions : Retourne les métadonnées de tri et la clause ORDER BY sécurisée.
function parseSortOptions(query: any): {
  sortBy: string
  sortOrder: 'asc' | 'desc'
  orderBy: string
} | null {
  const sortByValue =
    typeof query.sortBy === 'string' ? query.sortBy.trim().toLowerCase() : ''
  const sortOrderValue =
    typeof query.sortOrder === 'string' ? query.sortOrder.trim().toLowerCase() : ''

  if (sortByValue) {
    const sortColumn = SORT_FIELD_MAP[sortByValue]
    if (!sortColumn) {
      return null
    }
    const sortOrder: 'asc' | 'desc' = sortOrderValue === 'desc' ? 'desc' : 'asc'
    return {
      sortBy: sortByValue,
      sortOrder,
      orderBy: `${sortColumn} ${sortOrder.toUpperCase()}${sortByValue === 'title' ? ', g.id ASC' : ', LOWER(g.title) ASC'}`,
    }
  }

  const legacySort = typeof query.sort === 'string' ? query.sort.trim().toLowerCase() : ''
  const legacyMap: Record<string, { sortBy: string; sortOrder: 'asc' | 'desc'; orderBy: string }> = {
    title_asc: { sortBy: 'title', sortOrder: 'asc', orderBy: 'LOWER(g.title) ASC, g.id ASC' },
    title_desc: { sortBy: 'title', sortOrder: 'desc', orderBy: 'LOWER(g.title) DESC, g.id DESC' },
    min_age_asc: { sortBy: 'min_age', sortOrder: 'asc', orderBy: 'g.min_age ASC, LOWER(g.title) ASC' },
    min_age_desc: { sortBy: 'min_age', sortOrder: 'desc', orderBy: 'g.min_age DESC, LOWER(g.title) ASC' },
    editor_asc: { sortBy: 'editor_name', sortOrder: 'asc', orderBy: "LOWER(COALESCE(e.name, '')) ASC, LOWER(g.title) ASC" },
    editor_desc: { sortBy: 'editor_name', sortOrder: 'desc', orderBy: "LOWER(COALESCE(e.name, '')) DESC, LOWER(g.title) ASC" },
  }

  if (!legacySort) {
    return legacyMap.title_asc ?? null
  }

  return legacyMap[legacySort] ?? null
}

// Role : Construire la requete SQL de selection des jeux.
// Preconditions : where est une clause WHERE valide ou vide.
// Postconditions : Retourne la requete SQL complete.
function buildGameSelect(where: string): string {
  return `
    SELECT
      g.id, g.title, g.type, g.editor_id, e.name AS editor_name,
      g.min_age, g.authors, g.min_players, g.max_players, g.prototype, g.duration_minutes,
      g.theme, g.description, g.image_url, g.rules_video_url,
      COALESCE(
        json_agg(DISTINCT jsonb_build_object('id', m.id, 'name', m.name, 'description', m.description))
          FILTER (WHERE m.id IS NOT NULL),
        '[]'
      ) AS mechanisms
    FROM games g
    LEFT JOIN editor e ON e.id = g.editor_id
    LEFT JOIN game_mechanism gm ON gm.game_id = g.id
    LEFT JOIN mechanism m ON m.id = gm.mechanism_id
    ${where}
    GROUP BY
      g.id, g.title, g.type, g.editor_id, e.name, g.min_age, g.authors, g.min_players,
      g.max_players, g.prototype, g.duration_minutes, g.theme, g.description, g.image_url, g.rules_video_url
  `
}

// Role : Construire la réponse paginée des jeux.
// Preconditions : page et limit sont valides.
// Postconditions : Retourne le payload paginé attendu par le mobile.
function buildPagedGameResponse(
  items: GameRecord[],
  page: number,
  limit: number,
  total: number,
  sortBy: string,
  sortOrder: 'asc' | 'desc',
): GameListResponse {
  return {
    items,
    pagination: {
      page,
      limit,
      total,
      totalPages: total === 0 ? 0 : Math.ceil(total / limit),
      sortBy,
      sortOrder,
    },
  }
}

// Role : Recuperer un jeu par identifiant.
// Preconditions : gameId est valide.
// Postconditions : Retourne le jeu ou null s'il n'existe pas.
async function fetchGameById(
  gameId: number,
  client: Pool | PoolClient = pool,
): Promise<GameRecord | null> {
  const query = `${buildGameSelect('WHERE g.id = $1')} LIMIT 1`
  const { rows } = await client.query(query, [gameId])
  return rows.length > 0 ? (rows[0] as GameRecord) : null
}

// Role : Verifier l'existence d'un editeur.
// Preconditions : client est connecte et editorId est valide.
// Postconditions : Lance une erreur si l'editeur n'existe pas.
async function ensureEditorExists(client: PoolClient, editorId: number) {
  const { rows } = await client.query('SELECT id FROM editor WHERE id = $1', [editorId])
  if (rows.length === 0) {
    throw new Error('EDITOR_NOT_FOUND')
  }
}

// Role : Verifier l'existence des mecanismes.
// Preconditions : client est connecte et mechanismIds est une liste d'IDs.
// Postconditions : Lance une erreur si un mecanisme manque.
async function ensureMechanismsExist(client: PoolClient, mechanismIds: number[]) {
  if (mechanismIds.length === 0) return
  const { rows } = await client.query<{ id: number }>(
    'SELECT id FROM mechanism WHERE id = ANY($1::int[])',
    [mechanismIds],
  )
  const foundIds = new Set(rows.map((r) => r.id))
  const missing = mechanismIds.filter((id) => !foundIds.has(id))
  if (missing.length > 0) {
    const error = new Error(`MISSING_MECHANISMS:${missing.join(',')}`)
    throw error
  }
}

// Role : Remplacer les mecanismes d'un jeu.
// Preconditions : client est connecte et gameId est valide.
// Postconditions : Les liaisons game_mechanism sont remplacees.
async function replaceMechanisms(client: PoolClient, gameId: number, mechanismIds: number[]) {
  await client.query('DELETE FROM game_mechanism WHERE game_id = $1', [gameId])
  if (mechanismIds.length === 0) return

  const values = mechanismIds.map((id, index) => `($1, $${index + 2})`).join(',')
  await client.query(
    `INSERT INTO game_mechanism (game_id, mechanism_id) VALUES ${values} ON CONFLICT (game_id, mechanism_id) DO NOTHING`,
    [gameId, ...mechanismIds],
  )
}

// Role : Lister les jeux avec filtres.
// Preconditions : La base est accessible.
// Postconditions : Retourne la liste des jeux ou une erreur.
router.get('/', async (req, res) => {
  const page = toPositiveInt(req.query.page) ?? DEFAULT_PAGE
  const limit = Math.min(toPositiveInt(req.query.limit) ?? DEFAULT_LIMIT, MAX_LIMIT)
  const sortOptions = parseSortOptions(req.query)

  if (req.query.page !== undefined && toPositiveInt(req.query.page) == null) {
    return res.status(400).json({ error: 'page doit être un entier positif' })
  }
  if (req.query.limit !== undefined && toPositiveInt(req.query.limit) == null) {
    return res.status(400).json({ error: 'limit doit être un entier positif' })
  }
  if (sortOptions == null) {
    return res.status(400).json({ error: 'sort invalide' })
  }

  try {
    const { where, params } = buildFilters(req.query)
    const offset = (page - 1) * limit
    const countQuery = `SELECT COUNT(*)::int AS total FROM games g ${where}`
    const listQuery = `${buildGameSelect(where)} ORDER BY ${sortOptions.orderBy} LIMIT $${
      params.length + 1
    } OFFSET $${params.length + 2}`
    const [countResult, listResult] = await Promise.all([
      pool.query<{ total: number }>(countQuery, params),
      pool.query<GameRecord>(listQuery, [...params, limit, offset]),
    ])
    const total = countResult.rows[0]?.total ?? 0
    res.json(
      buildPagedGameResponse(
        listResult.rows,
        page,
        limit,
        total,
        sortOptions.sortBy,
        sortOptions.sortOrder,
      ),
    )
  } catch (err) {
    console.error('Erreur lors de la récupération des jeux', err)
    res.status(500).json({ error: 'Erreur serveur' })
  }
})

// Role : Lister les types de jeux disponibles.
// Preconditions : La base est accessible.
// Postconditions : Retourne les types distincts triés.
router.get('/types', async (_req, res) => {
  try {
    const { rows } = await pool.query<{ value: string }>(
      `
        SELECT DISTINCT TRIM(type) AS value
        FROM games
        WHERE type IS NOT NULL AND LENGTH(TRIM(type)) > 0
        ORDER BY value ASC
      `,
    )
    res.json(rows.map((row) => row.value))
  } catch (err) {
    console.error('Erreur lors de la récupération des types de jeux', err)
    res.status(500).json({ error: 'Erreur serveur' })
  }
})

// Role : Recuperer les mecanismes d'un jeu.
// Preconditions : id est valide.
// Postconditions : Retourne les mecanismes ou une erreur.
router.get('/:id/mechanisms', async (req, res) => {
  const gameId = Number(req.params.id)
  if (!Number.isFinite(gameId)) {
    return res.status(400).json({ error: 'Identifiant de jeu invalide' })
  }
  try {
    const game = await fetchGameById(gameId)
    if (!game) {
      return res.status(404).json({ error: 'Jeu introuvable' })
    }
    res.json(game.mechanisms)
  } catch (err) {
    console.error('Erreur lors de la récupération des mécanismes du jeu', err)
    res.status(500).json({ error: 'Erreur serveur' })
  }
})

// Role : Recuperer un jeu par ID.
// Preconditions : id est valide.
// Postconditions : Retourne le jeu ou une erreur.
router.get('/:id', async (req, res) => {
  const gameId = Number(req.params.id)
  if (!Number.isFinite(gameId)) {
    return res.status(400).json({ error: 'Identifiant de jeu invalide' })
  }
  try {
    const game = await fetchGameById(gameId)
    if (!game) {
      return res.status(404).json({ error: 'Jeu introuvable' })
    }
    res.json(game)
  } catch (err) {
    console.error('Erreur lors de la récupération du jeu', err)
    res.status(500).json({ error: 'Erreur serveur' })
  }
})

// Role : Creer un jeu.
// Preconditions : Le payload est valide.
// Postconditions : Retourne le jeu cree ou une erreur.
router.post('/', backofficeOnly, async (req, res) => {
  const { data, errors } = parseGameBody(req.body, true)
  if (errors.length > 0) {
    return res.status(400).json(buildErrorBody('Payload invalide', errors))
  }

  const mechanismIds = data.mechanismIds ?? []

  const client = await pool.connect()
  try {
    if (data.editor_id !== undefined && data.editor_id !== null) {
      await ensureEditorExists(client, data.editor_id)
    }
    await ensureMechanismsExist(client, mechanismIds)

    await client.query('BEGIN')
    const { rows } = await client.query(
      `
        INSERT INTO games (
          title, type, editor_id, min_age, authors,
          min_players, max_players, prototype, duration_minutes,
          theme, description, image_url, rules_video_url
        )
        VALUES ($1, $2, $3, $4, $5, $6, $7, COALESCE($8, false), $9, $10, $11, $12, $13)
        RETURNING id
      `,
      [
        data.title,
        data.type,
        data.editor_id,
        data.min_age,
        data.authors,
        data.min_players ?? null,
        data.max_players ?? null,
        data.prototype ?? false,
        data.duration_minutes ?? null,
        data.theme ?? null,
        data.description ?? null,
        data.image_url ?? null,
        data.rules_video_url ?? null,
      ],
    )
    const newGameId = rows[0].id as number

    if (mechanismIds) {
      await replaceMechanisms(client, newGameId, mechanismIds)
    }

    await client.query('COMMIT')
    const game = await fetchGameById(newGameId, client)
    res.status(201).json(game)
  } catch (err: any) {
    await client.query('ROLLBACK')
    if (err.message === 'EDITOR_NOT_FOUND') {
      return res.status(400).json(buildErrorBody("Éditeur inexistant"))
    }
    if (typeof err.message === 'string' && err.message.startsWith('MISSING_MECHANISMS')) {
      const missingIds = err.message.replace('MISSING_MECHANISMS:', '').trim()
      return res.status(400).json(
        buildErrorBody('Mécanisme inexistant', `Mécanismes introuvables: ${missingIds}`),
      )
    }
    const mappedError = mapDatabaseError(err)
    if (mappedError) {
      return res.status(mappedError.status).json(mappedError.body)
    }
    console.error('Erreur lors de la création du jeu', err)
    res.status(500).json({ error: 'Erreur serveur' })
  } finally {
    client.release()
  }
})

// Role : Mettre a jour un jeu.
// Preconditions : id est valide et le payload est coherent.
// Postconditions : Retourne le jeu mis a jour ou une erreur.
async function updateGame(req: any, res: any) {
  const gameId = Number(req.params.id)
  if (!Number.isFinite(gameId)) {
    return res.status(400).json({ error: 'Identifiant de jeu invalide' })
  }

  const { data, errors } = parseGameBody(req.body, false)
  if (errors.length > 0) {
    return res.status(400).json(buildErrorBody('Payload invalide', errors))
  }

  const client = await pool.connect()
  try {
    const { rows: currentRows } = await client.query(
      `SELECT id, title, type, editor_id, min_age, authors, min_players, max_players, prototype,
              duration_minutes, theme, description, image_url, rules_video_url
       FROM games WHERE id = $1`,
      [gameId],
    )
    if (currentRows.length === 0) {
      return res.status(404).json({ error: 'Jeu introuvable' })
    }
    const current = currentRows[0]

    const merged: GamePayload = {
      title: data.title ?? current.title,
      type: data.type ?? current.type,
      editor_id: data.editor_id ?? current.editor_id,
      min_age: data.min_age ?? current.min_age,
      authors: data.authors ?? current.authors,
      min_players: data.min_players ?? current.min_players,
      max_players: data.max_players ?? current.max_players,
      prototype: data.prototype ?? current.prototype,
      duration_minutes: data.duration_minutes ?? current.duration_minutes,
      theme: data.theme ?? current.theme,
      description: data.description ?? current.description,
      image_url: data.image_url ?? current.image_url,
      rules_video_url: data.rules_video_url ?? current.rules_video_url,
    }

    const mergedErrors: string[] = []
    if (!merged.title) mergedErrors.push('title est requis')
    if (!merged.type) mergedErrors.push('type est requis')
    if (merged.editor_id === null || merged.editor_id === undefined) {
      mergedErrors.push('editor_id est requis')
    }
    if (merged.min_age === null || merged.min_age === undefined) {
      mergedErrors.push('min_age est requis')
    }
    if (!merged.authors) mergedErrors.push('authors est requis')
    if (
      merged.min_players !== null &&
      merged.max_players !== null &&
      merged.min_players !== undefined &&
      merged.max_players !== undefined &&
      merged.min_players > merged.max_players
    ) {
      mergedErrors.push('min_players ne peut pas être supérieur à max_players')
    }
    if (mergedErrors.length > 0) {
      return res.status(400).json(buildErrorBody('Payload invalide', mergedErrors))
    }

    if (merged.editor_id !== null && merged.editor_id !== undefined) {
      await ensureEditorExists(client, merged.editor_id)
    }

    const mechanismIds =
      data.mechanismIds !== undefined ? data.mechanismIds : undefined

    if (mechanismIds !== undefined) {
      await ensureMechanismsExist(client, mechanismIds)
    }

    await client.query('BEGIN')
    await client.query(
      `
        UPDATE games
        SET title = $1,
            type = $2,
            editor_id = $3,
            min_age = $4,
            authors = $5,
            min_players = $6,
            max_players = $7,
            prototype = COALESCE($8, false),
            duration_minutes = $9,
            theme = $10,
            description = $11,
            image_url = $12,
            rules_video_url = $13
        WHERE id = $14
      `,
      [
        merged.title,
        merged.type,
        merged.editor_id,
        merged.min_age,
        merged.authors,
        merged.min_players ?? null,
        merged.max_players ?? null,
        merged.prototype ?? false,
        merged.duration_minutes ?? null,
        merged.theme ?? null,
        merged.description ?? null,
        merged.image_url ?? null,
        merged.rules_video_url ?? null,
        gameId,
      ],
    )

    if (mechanismIds !== undefined) {
      await replaceMechanisms(client, gameId, mechanismIds)
    }

    await client.query('COMMIT')
    const game = await fetchGameById(gameId, client)
    res.json(game)
  } catch (err: any) {
    await client.query('ROLLBACK')
    if (err.message === 'EDITOR_NOT_FOUND') {
      return res.status(400).json(buildErrorBody("Éditeur inexistant"))
    }
    if (typeof err.message === 'string' && err.message.startsWith('MISSING_MECHANISMS')) {
      const missingIds = err.message.replace('MISSING_MECHANISMS:', '').trim()
      return res.status(400).json(
        buildErrorBody('Mécanisme inexistant', `Mécanismes introuvables: ${missingIds}`),
      )
    }
    const mappedError = mapDatabaseError(err)
    if (mappedError) {
      return res.status(mappedError.status).json(mappedError.body)
    }
    console.error('Erreur lors de la mise à jour du jeu', err)
    res.status(500).json({ error: 'Erreur serveur' })
  } finally {
    client.release()
  }
}

// Role : Mettre a jour un jeu (PUT).
// Preconditions : id est valide.
// Postconditions : Delegue a updateGame.
router.put('/:id', backofficeOnly, updateGame)

// Role : Mettre a jour un jeu (PATCH).
// Preconditions : id est valide.
// Postconditions : Delegue a updateGame.
router.patch('/:id', backofficeOnly, updateGame)

// Role : Supprimer un jeu.
// Preconditions : id est valide.
// Postconditions : Retourne un message de suppression ou une erreur.
router.delete('/:id', backofficeOnly, async (req, res) => {
  const gameId = Number(req.params.id)
  if (!Number.isFinite(gameId)) {
    return res.status(400).json({ error: 'Identifiant de jeu invalide' })
  }

  try {
    const { rowCount } = await pool.query('DELETE FROM games WHERE id = $1', [gameId])
    if (rowCount === 0) {
      return res.status(404).json({ error: 'Jeu introuvable' })
    }
    res.json({ message: 'Jeu supprimé' })
  } catch (err: any) {
    if (err.code === '23503') {
      return res.status(409).json({
        error: 'Impossible de supprimer ce jeu car il est utilisé dans une réservation',
      })
    }
    console.error('Erreur lors de la suppression du jeu', err)
    res.status(500).json({ error: 'Erreur serveur' })
  }
})

export default router
