// Role : Tester les routes /users.
import test, { mock } from 'node:test'
import assert from 'node:assert/strict'
import nodemailer from 'nodemailer'
import pool from '../../db/database.js'
import usersRouter from '../../routes/users.js'
import {
  createTestUser,
  generateTestEmail,
  generateTestLogin,
  setupTests,
  teardownTests,
} from '../test-helpers.js'

type Handler = (req: any, res: any, next: () => void) => Promise<void> | void

const sendMailMock = mock.fn(async () => ({ messageId: 'users-test-message-id' }))

mock.method(nodemailer, 'createTransport', () => {
  return {
    sendMail: sendMailMock,
  } as any
})

// Role : Recuperer le handler pour une route et une methode.
// Preconditions : path et method sont definis.
// Postconditions : Retourne le handler ou undefined.
function getRouteHandler(
  path: string,
  method: 'get' | 'post' | 'put' | 'delete',
): Handler | undefined {
  const layer: any = usersRouter.stack.find(
    (item: any) => item.route?.path === path && item.route?.methods?.[method],
  )
  return layer?.route?.stack?.[layer.route.stack.length - 1]?.handle
}

test.before(async () => {
  await setupTests()
})

test.after(async () => {
  await teardownTests()
})

test.beforeEach(() => {
  sendMailMock.mock.resetCalls()
})

test('POST /users - should create user with default role', async () => {
  const login = generateTestLogin()
  const email = generateTestEmail()
  const handler = getRouteHandler('/', 'post')
  assert.ok(handler)

  const mockReq = {
    body: {
      login,
      password: 'SecurePass123',
      firstName: 'Test',
      lastName: 'User',
      email,
      phone: '',
      avatarUrl: '',
    },
  }

  const mockRes: any = {
    statusCode: 200,
    jsonData: null,
    status(code: number) {
      this.statusCode = code
      return this
    },
    json(data: any) {
      this.jsonData = data
      return this
    },
  }

  await handler?.(mockReq as any, mockRes as any, () => {})

  assert.strictEqual(mockRes.statusCode, 201)

  const { rows } = await pool.query('SELECT role FROM users WHERE login = $1', [login])
  assert.strictEqual(rows[0].role, 'benevole')
})

test('PUT /users/:id - should update user role and status', async () => {
  const user = await createTestUser({ emailVerified: false })
  const handler = getRouteHandler('/:id', 'put')
  assert.ok(handler)

  const mockReq = {
    params: { id: String(user.id) },
    body: {
      role: 'benevole',
      emailVerified: true,
      firstName: 'Updated',
      lastName: 'User',
      email: user.email,
    },
  }

  const mockRes: any = {
    statusCode: 200,
    jsonData: null,
    status(code: number) {
      this.statusCode = code
      return this
    },
    json(data: any) {
      this.jsonData = data
      return this
    },
  }

  await handler?.(mockReq as any, mockRes as any, () => {})

  assert.strictEqual(mockRes.statusCode, 200)
  const { rows } = await pool.query(
    'SELECT role, email_verified, first_name FROM users WHERE id = $1',
    [user.id],
  )
  assert.strictEqual(rows[0].role, 'benevole')
  assert.strictEqual(rows[0].email_verified, true)
  assert.strictEqual(rows[0].first_name, 'Updated')
})

test('PUT /users/me - should update current user profile', async () => {
  const user = await createTestUser()
  const handler = getRouteHandler('/me', 'put')
  assert.ok(handler)

  const mockReq = {
    user: { id: user.id },
    body: {
      firstName: 'Profile',
      lastName: 'Updated',
      email: user.email,
    },
  }

  const mockRes: any = {
    statusCode: 200,
    jsonData: null,
    status(code: number) {
      this.statusCode = code
      return this
    },
    json(data: any) {
      this.jsonData = data
      return this
    },
  }

  await handler?.(mockReq as any, mockRes as any, () => {})

  assert.strictEqual(mockRes.statusCode, 200)
  const { rows } = await pool.query('SELECT first_name, last_name FROM users WHERE id = $1', [
    user.id,
  ])
  assert.strictEqual(rows[0].first_name, 'Profile')
  assert.strictEqual(rows[0].last_name, 'Updated')
})

test('GET /users/me - should return current user profile', async () => {
  const user = await createTestUser({ emailVerified: false })
  const handler = getRouteHandler('/me', 'get')
  assert.ok(handler)

  const mockReq = {
    user: { id: user.id },
  }

  const mockRes: any = {
    statusCode: 200,
    jsonData: null,
    status(code: number) {
      this.statusCode = code
      return this
    },
    json(data: any) {
      this.jsonData = data
      return this
    },
  }

  await handler?.(mockReq as any, mockRes as any, () => {})

  assert.strictEqual(mockRes.statusCode, 200)
  assert.strictEqual(mockRes.jsonData.id, user.id)
  assert.strictEqual(mockRes.jsonData.email, user.email)
  assert.strictEqual(mockRes.jsonData.emailVerified, false)
})

test('PUT /users/me - should trigger a new verification when email changes', async () => {
  const user = await createTestUser({ emailVerified: true })
  const handler = getRouteHandler('/me', 'put')
  assert.ok(handler)
  const nextEmail = generateTestEmail()

  const mockReq = {
    user: { id: user.id },
    body: {
      email: nextEmail,
    },
  }

  const mockRes: any = {
    statusCode: 200,
    jsonData: null,
    status(code: number) {
      this.statusCode = code
      return this
    },
    json(data: any) {
      this.jsonData = data
      return this
    },
  }

  await handler?.(mockReq as any, mockRes as any, () => {})

  assert.strictEqual(mockRes.statusCode, 200)
  assert.strictEqual(
    mockRes.jsonData.message,
    'Profil mis à jour. Vérifiez votre nouvel email.',
  )
  assert.strictEqual(mockRes.jsonData.emailVerificationSent, true)
  assert.strictEqual(mockRes.jsonData.user.email, nextEmail)
  assert.strictEqual(mockRes.jsonData.user.emailVerified, false)
  assert.strictEqual(sendMailMock.mock.calls.length, 1)

  const { rows } = await pool.query(
    'SELECT email, email_verified, email_verification_token FROM users WHERE id = $1',
    [user.id],
  )
  assert.strictEqual(rows[0].email, nextEmail)
  assert.strictEqual(rows[0].email_verified, false)
  assert.ok(rows[0].email_verification_token)
})

test('PUT /users/me - should clear avatar when avatarUrl is null', async () => {
  const user = await createTestUser()
  await pool.query('UPDATE users SET avatar_url = $1 WHERE id = $2', ['/uploads/avatars/old.png', user.id])
  const handler = getRouteHandler('/me', 'put')
  assert.ok(handler)

  const mockReq = {
    user: { id: user.id },
    body: {
      avatarUrl: null,
    },
  }

  const mockRes: any = {
    statusCode: 200,
    jsonData: null,
    status(code: number) {
      this.statusCode = code
      return this
    },
    json(data: any) {
      this.jsonData = data
      return this
    },
  }

  await handler?.(mockReq as any, mockRes as any, () => {})

  assert.strictEqual(mockRes.statusCode, 200)
  assert.strictEqual(mockRes.jsonData.user.avatarUrl, null)

  const { rows } = await pool.query('SELECT avatar_url FROM users WHERE id = $1', [user.id])
  assert.strictEqual(rows[0].avatar_url, null)
})

test('DELETE /users/me - should delete current user', async () => {
  const user = await createTestUser()
  const handler = getRouteHandler('/me', 'delete')
  assert.ok(handler)

  const mockReq = {
    user: { id: user.id },
  }

  const mockRes: any = {
    statusCode: 200,
    jsonData: null,
    clearCookie() {
      return this
    },
    status(code: number) {
      this.statusCode = code
      return this
    },
    json(data: any) {
      this.jsonData = data
      return this
    },
  }

  await handler?.(mockReq as any, mockRes as any, () => {})

  assert.strictEqual(mockRes.statusCode, 200)
  const { rowCount } = await pool.query('SELECT 1 FROM users WHERE id = $1', [user.id])
  assert.strictEqual(rowCount, 0)
})
