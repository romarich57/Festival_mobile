// Role : Tester les routes /api/games.
import test from 'node:test'
import assert from 'node:assert/strict'
import pool from '../../db/database.js'
import gamesRouter from '../../routes/games.js'
import {
  createTestFestival,
  createTestReservant,
  deleteTestGameFixtures,
  setupTests,
  teardownTests,
} from '../test-helpers.js'

type MockResponse = {
  statusCode: number
  jsonData: any
  headersSent?: boolean
  status: (code: number) => MockResponse
  json: (data: any) => MockResponse
}

type Handler = (req: any, res: MockResponse, next: () => void) => Promise<void> | void

let testCounter = 0

function nextPrefix(): string {
  testCounter += 1
  return `games_test_${Date.now()}_${testCounter}`
}

function createMockResponse(): MockResponse {
  return {
    statusCode: 200,
    jsonData: null,
    headersSent: false,
    status(code: number) {
      this.statusCode = code
      return this
    },
    json(data: any) {
      this.jsonData = data
      this.headersSent = true
      return this
    },
  }
}

function getRouteStack(path: string, method: 'get' | 'post' | 'put' | 'patch' | 'delete'): Handler[] {
  const layer: any = gamesRouter.stack.find(
    (item: any) => item.route?.path === path && item.route?.methods?.[method],
  )
  return (layer?.route?.stack ?? []).map((entry: any) => entry.handle as Handler)
}

async function runRoute(path: string, method: 'get' | 'post' | 'put' | 'patch' | 'delete', req: any) {
  const handlers = getRouteStack(path, method)
  assert.ok(handlers.length > 0, `Route ${method.toUpperCase()} ${path} introuvable`)
  const res = createMockResponse()

  const invokeAt = async (index: number): Promise<void> => {
    const handler = handlers[index]
    if (!handler || res.headersSent) {
      return
    }

    let nextCalled = false
    await handler(req, res, () => {
      nextCalled = true
    })

    if (nextCalled && !res.headersSent) {
      await invokeAt(index + 1)
    }
  }

  await invokeAt(0)
  return res
}

async function createEditor(prefix: string) {
  const { rows } = await pool.query(
    `
      INSERT INTO editor (name, email, website, description)
      VALUES ($1, $2, $3, $4)
      RETURNING id, name
    `,
    [
      `${prefix}_editor`,
      `${prefix}@editor.test.com`,
      'https://example.com',
      'Test editor',
    ],
  )
  return rows[0] as { id: number; name: string }
}

async function createMechanism(prefix: string, suffix: string) {
  const { rows } = await pool.query(
    `
      INSERT INTO mechanism (name, description)
      VALUES ($1, $2)
      RETURNING id, name
    `,
    [`${prefix}_${suffix}`, 'Test mechanism'],
  )
  return rows[0] as { id: number; name: string }
}

async function createGameRecord(input: {
  prefix: string
  title: string
  type: string
  editorId: number
  minAge?: number
}) {
  const { rows } = await pool.query(
    `
      INSERT INTO games (title, type, editor_id, min_age, authors, min_players, max_players)
      VALUES ($1, $2, $3, $4, $5, $6, $7)
      RETURNING id, title, type
    `,
    [
      input.title,
      input.type,
      input.editorId,
      input.minAge ?? 10,
      `${input.prefix} author`,
      1,
      4,
    ],
  )
  return rows[0] as { id: number; title: string; type: string }
}

async function cleanupGameFixtures(prefix: string) {
  await pool.query(
    `
      DELETE FROM game_mechanism
      WHERE game_id IN (SELECT id FROM games WHERE title LIKE $1)
    `,
    [`${prefix}%`],
  )
  await pool.query(
    `
      DELETE FROM jeux_alloues
      WHERE game_id IN (SELECT id FROM games WHERE title LIKE $1)
    `,
    [`${prefix}%`],
  )
  await pool.query(
    `
      DELETE FROM reservation
      WHERE reservant_id IN (
        SELECT id FROM reservant WHERE email LIKE $1
      )
    `,
    [`${prefix}%@reservant.test.com`],
  )
  await pool.query('DELETE FROM games WHERE title LIKE $1', [`${prefix}%`])
  await pool.query('DELETE FROM mechanism WHERE name LIKE $1', [`${prefix}%`])
  await pool.query('DELETE FROM editor WHERE email LIKE $1', [`${prefix}%@editor.test.com`])
  await pool.query('DELETE FROM reservant WHERE email LIKE $1', [`${prefix}%@reservant.test.com`])
  await pool.query('DELETE FROM festival WHERE name LIKE $1', [`${prefix}%`])
}

test.before(async () => {
  await setupTests()
})

test.beforeEach(async () => {
  await deleteTestGameFixtures()
})

test.afterEach(async () => {
  await deleteTestGameFixtures()
})

test.after(async () => {
  await teardownTests()
})

test('GET / returns paginated games for benevole', async () => {
  const prefix = nextPrefix()
  const editor = await createEditor(prefix)
  await createGameRecord({
    prefix,
    title: `${prefix}_Bravo`,
    type: `${prefix}_type`,
    editorId: editor.id,
    minAge: 12,
  })
  await createGameRecord({
    prefix,
    title: `${prefix}_Alpha`,
    type: `${prefix}_type`,
    editorId: editor.id,
    minAge: 8,
  })
  await createGameRecord({
    prefix,
    title: `${prefix}_Charlie`,
    type: `${prefix}_type`,
    editorId: editor.id,
    minAge: 10,
  })

  const firstPageRes = await runRoute('/', 'get', {
    query: {
      title: prefix,
      page: '1',
      limit: '2',
      sortBy: 'title',
      sortOrder: 'asc',
    },
    user: { role: 'benevole' },
  })

  assert.strictEqual(firstPageRes.statusCode, 200)
  assert.deepStrictEqual(firstPageRes.jsonData.pagination, {
    page: 1,
    limit: 2,
    total: 3,
    totalPages: 2,
    sortBy: 'title',
    sortOrder: 'asc',
  })
  assert.deepStrictEqual(
    firstPageRes.jsonData.items.map((item: any) => item.title),
    [`${prefix}_Alpha`, `${prefix}_Bravo`],
  )

  const secondPageRes = await runRoute('/', 'get', {
    query: {
      title: prefix,
      page: '2',
      limit: '2',
      sortBy: 'title',
      sortOrder: 'asc',
    },
    user: { role: 'benevole' },
  })

  assert.strictEqual(secondPageRes.statusCode, 200)
  assert.strictEqual(secondPageRes.jsonData.pagination.page, 2)
  assert.deepStrictEqual(secondPageRes.jsonData.items.map((item: any) => item.title), [
    `${prefix}_Charlie`,
  ])

  await cleanupGameFixtures(prefix)
})

test('GET / rejects invalid sort values', async () => {
  const res = await runRoute('/', 'get', {
    query: { sortBy: 'duration' },
    user: { role: 'benevole' },
  })

  assert.strictEqual(res.statusCode, 400)
  assert.strictEqual(res.jsonData.error, 'sort invalide')
})

test('GET /types returns distinct types including test fixtures', async () => {
  const prefix = nextPrefix()
  const editor = await createEditor(prefix)
  await createGameRecord({
    prefix,
    title: `${prefix}_Type_A`,
    type: `${prefix}_strategy`,
    editorId: editor.id,
  })
  await createGameRecord({
    prefix,
    title: `${prefix}_Type_B`,
    type: `${prefix}_party`,
    editorId: editor.id,
  })
  await createGameRecord({
    prefix,
    title: `${prefix}_Type_C`,
    type: `${prefix}_party`,
    editorId: editor.id,
  })

  const res = await runRoute('/types', 'get', {
    query: {},
    user: { role: 'benevole' },
  })

  assert.strictEqual(res.statusCode, 200)
  const matchingValues = res.jsonData.filter((value: string) => value.startsWith(prefix))
  assert.deepStrictEqual(matchingValues, [`${prefix}_party`, `${prefix}_strategy`])

  await cleanupGameFixtures(prefix)
})

test('POST / forbids benevole role', async () => {
  const res = await runRoute('/', 'post', {
    user: { role: 'benevole' },
    body: {
      title: 'ignored',
      type: 'ignored',
      editor_id: 1,
      min_age: 8,
      authors: 'ignored',
    },
  })

  assert.strictEqual(res.statusCode, 403)
  assert.strictEqual(res.jsonData.error, 'Acces interdit')
})

test('POST / allows organizer to create a game with mechanisms', async () => {
  const prefix = nextPrefix()
  const editor = await createEditor(prefix)
  const mechanismA = await createMechanism(prefix, 'bluff')
  const mechanismB = await createMechanism(prefix, 'draft')

  const res = await runRoute('/', 'post', {
    user: { role: 'organizer' },
    body: {
      title: `${prefix}_created`,
      type: `${prefix}_expert`,
      editor_id: editor.id,
      min_age: 14,
      authors: `${prefix} authors`,
      mechanismIds: [mechanismA.id, mechanismB.id],
    },
  })

  assert.strictEqual(res.statusCode, 201)
  assert.strictEqual(res.jsonData.title, `${prefix}_created`)
  assert.strictEqual(res.jsonData.editor_id, editor.id)
  assert.deepStrictEqual(
    res.jsonData.mechanisms.map((item: any) => item.id).sort((a: number, b: number) => a - b),
    [mechanismA.id, mechanismB.id],
  )

  await cleanupGameFixtures(prefix)
})

test('POST / returns duplicate title conflict with clean error body', async () => {
  const prefix = nextPrefix()
  const editor = await createEditor(prefix)
  await createGameRecord({
    prefix,
    title: `${prefix}_duplicate`,
    type: `${prefix}_family`,
    editorId: editor.id,
  })

  const res = await runRoute('/', 'post', {
    user: { role: 'organizer' },
    body: {
      title: `${prefix}_duplicate`,
      type: `${prefix}_family`,
      editor_id: editor.id,
      min_age: 8,
      authors: `${prefix} authors`,
    },
  })

  assert.strictEqual(res.statusCode, 409)
  assert.deepStrictEqual(res.jsonData, {
    error: 'Titre déjà utilisé',
  })

  await cleanupGameFixtures(prefix)
})

test('POST / returns normalized validation details', async () => {
  const prefix = nextPrefix()
  const editor = await createEditor(prefix)

  const res = await runRoute('/', 'post', {
    user: { role: 'organizer' },
    body: {
      title: `${prefix}_invalid`,
      type: `${prefix}_family`,
      editor_id: editor.id,
      min_age: 8,
      authors: `${prefix} authors`,
      min_players: 4,
      max_players: 2,
    },
  })

  assert.strictEqual(res.statusCode, 400)
  assert.strictEqual(res.jsonData.error, 'Payload invalide')
  assert.deepStrictEqual(res.jsonData.details, [
    'min_players ne peut pas être supérieur à max_players',
  ])

  await cleanupGameFixtures(prefix)
})

test('POST / returns clean editor error when editor does not exist', async () => {
  const prefix = nextPrefix()

  const res = await runRoute('/', 'post', {
    user: { role: 'organizer' },
    body: {
      title: `${prefix}_missing_editor`,
      type: `${prefix}_family`,
      editor_id: 999999,
      min_age: 10,
      authors: `${prefix} authors`,
    },
  })

  assert.strictEqual(res.statusCode, 400)
  assert.deepStrictEqual(res.jsonData, {
    error: 'Éditeur inexistant',
  })
})

test('POST / returns clean mechanism error with normalized details', async () => {
  const prefix = nextPrefix()
  const editor = await createEditor(prefix)

  const res = await runRoute('/', 'post', {
    user: { role: 'organizer' },
    body: {
      title: `${prefix}_missing_mechanism`,
      type: `${prefix}_family`,
      editor_id: editor.id,
      min_age: 10,
      authors: `${prefix} authors`,
      mechanismIds: [999999],
    },
  })

  assert.strictEqual(res.statusCode, 400)
  assert.strictEqual(res.jsonData.error, 'Mécanisme inexistant')
  assert.deepStrictEqual(res.jsonData.details, [
    'Mécanismes introuvables: 999999',
  ])

  await cleanupGameFixtures(prefix)
})

test('DELETE /:id returns conflict when game is allocated in a reservation', async () => {
  const prefix = nextPrefix()
  const editor = await createEditor(prefix)
  const game = await createGameRecord({
    prefix,
    title: `${prefix}_allocated`,
    type: `${prefix}_family`,
    editorId: editor.id,
  })
  const festival = await createTestFestival({ name: `${prefix}_festival` })
  const reservant = await createTestReservant({
    name: `${prefix}_reservant`,
    email: `${prefix}@reservant.test.com`,
  })
  const { rows: reservationRows } = await pool.query(
    `
      INSERT INTO reservation (
        reservant_id,
        festival_id,
        start_price,
        table_discount_offered,
        direct_discount,
        nb_prises,
        final_price
      )
      VALUES ($1, $2, $3, $4, $5, $6, $7)
      RETURNING id
    `,
    [reservant.id, festival.id, 10, 0, 0, 1, 10],
  )
  await pool.query(
    `
      INSERT INTO jeux_alloues (game_id, reservation_id, nb_tables_occupees, nb_exemplaires)
      VALUES ($1, $2, $3, $4)
    `,
    [game.id, reservationRows[0].id, 1, 1],
  )

  const res = await runRoute('/:id', 'delete', {
    user: { role: 'admin' },
    params: { id: String(game.id) },
  })

  assert.strictEqual(res.statusCode, 409)
  assert.strictEqual(
    res.jsonData.error,
    'Impossible de supprimer ce jeu car il est utilisé dans une réservation',
  )

  await cleanupGameFixtures(prefix)
})
