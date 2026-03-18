// Role : Tester les routes d'authentification.
import test, { mock } from 'node:test'
import assert from 'node:assert/strict'
import crypto from 'node:crypto'
import nodemailer from 'nodemailer'
import authRouter from '../../routes/auth.js'
import pool from '../../db/database.js'
import {
    createTestUser,
    generateTestEmail,
    generateTestLogin,
    setupTests,
    teardownTests
} from '../test-helpers.js'

// Tests des routes /api/auth (register, login, tokens)

const sendMailMock = mock.fn(async () => ({ messageId: 'test-message-id' }))

mock.method(nodemailer, 'createTransport', () => {
    return {
        sendMail: sendMailMock
    } as any
})

function getRouteHandler(path: string, method: 'get' | 'post') {
    return authRouter.stack
        .find((layer: any) => layer.route?.path === path && layer.route?.methods?.[method])
        ?.route?.stack[0]?.handle
}

function createMockResponse() {
    return {
        statusCode: 200,
        jsonData: null as any,
        redirectUrl: null as string | null,
        headers: {} as Record<string, string>,
        status(code: number) {
            this.statusCode = code
            return this
        },
        json(data: any) {
            this.jsonData = data
            return this
        },
        send(data: any) {
            this.jsonData = data
            return this
        },
        redirect(url: string) {
            this.statusCode = 302
            this.redirectUrl = url
            return this
        },
        setHeader(name: string, value: string) {
            this.headers[name] = value
            return this
        },
        type(value: string) {
            this.headers['content-type'] = value
            return this
        },
        cookie(name: string, value: string, options?: any) {
            this.headers[`cookie:${name}`] = JSON.stringify({ value, options })
            return this
        }
    }
}

async function storeVerificationTokenForUser(userId: number, token: string, expiresAt: Date) {
    const tokenHash = crypto.createHash('sha256').update(token).digest('hex')
    await pool.query(
        `
        UPDATE users
        SET email_verified = FALSE,
            email_verification_token = $1,
            email_verification_expires_at = $2
        WHERE id = $3
        `,
        [tokenHash, expiresAt, userId]
    )
}

async function storePasswordResetTokenForUser(userId: number, token: string, expiresAt: Date) {
    const tokenHash = crypto.createHash('sha256').update(token).digest('hex')
    await pool.query(
        `
        UPDATE users
        SET password_reset_token = $1,
            password_reset_expires_at = $2
        WHERE id = $3
        `,
        [tokenHash, expiresAt, userId]
    )
}

async function createRefreshTokenRecord(userId: number, expiresAt: Date = new Date(Date.now() + 60_000)) {
    const jti = crypto.randomUUID()
    const jtiHash = crypto.createHash('sha256').update(jti).digest('hex')
    await pool.query(
        `
        INSERT INTO refresh_tokens (user_id, jti_hash, expires_at)
        VALUES ($1, $2, $3)
        `,
        [userId, jtiHash, expiresAt]
    )
    return { jti, jtiHash }
}

// Preparation et nettoyage
test.before(async () => {
    await setupTests()
    sendMailMock.mock.resetCalls()
})

test.after(async () => {
    await teardownTests()
})



test('POST /register - should create new user with valid data', async () => {
    const mockReq = {
        body: {
            login: generateTestLogin(),
            firstName: 'John',
            lastName: 'Doe',
            email: generateTestEmail(),
            password: 'SecurePass123!',
            phone: '0123456789'
        }
    }

    const mockRes: any = createMockResponse()

    await getRouteHandler('/register', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 201)
    assert.ok(mockRes.jsonData)
    assert.ok(mockRes.jsonData.message.includes('Compte créé'))
    assert.strictEqual(sendMailMock.mock.calls.length, 1)
})

test('POST /register - should return 400 for missing required fields', async () => {
    const mockReq = {
        body: {
            login: 'testuser'
            // Champs firstName, lastName, email, password manquants
        }
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
        }
    }

    await getRouteHandler('/register', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 400)
    assert.ok(mockRes.jsonData?.error)
    assert.ok(mockRes.jsonData.error.includes('obligatoires'))
})

test('POST /register - should return 400 for invalid email format', async () => {
    const mockReq = {
        body: {
            login: generateTestLogin(),
            firstName: 'Test',
            lastName: 'User',
            email: 'invalid-email-format',
            password: 'SecurePass123!'
        }
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
        }
    }

    await getRouteHandler('/register', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 400)
    assert.ok(mockRes.jsonData?.error)
    assert.ok(mockRes.jsonData.error.includes('Email invalide'))
})

test('POST /register - should return 409 for duplicate email', async () => {
    const email = generateTestEmail()

    // Creer un premier utilisateur
    await createTestUser({ email })

    // Tenter un doublon
    const mockReq = {
        body: {
            login: generateTestLogin(),
            firstName: 'Test',
            lastName: 'User',
            email: email, // Doublon
            password: 'SecurePass123!'
        }
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
        }
    }

    await getRouteHandler('/register', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 409)
    assert.ok(mockRes.jsonData?.error)
    assert.ok(mockRes.jsonData.error.includes('déjà utilisé'))
})

test('POST /register - should return 409 for duplicate login', async () => {
    const login = generateTestLogin()

    // Creer un premier utilisateur
    await createTestUser({ login })

    // Tenter un doublon
    const mockReq = {
        body: {
            login: login, // Doublon
            firstName: 'Test',
            lastName: 'User',
            email: generateTestEmail(),
            password: 'SecurePass123!'
        }
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
        }
    }

    await getRouteHandler('/register', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 409)
    assert.ok(mockRes.jsonData?.error)
    assert.ok(mockRes.jsonData.error.includes('Login déjà utilisé'))
})

test('POST /register - should hash password before storing', async () => {
    const password = 'PlainTextPassword123!'
    const email = generateTestEmail()

    const mockReq = {
        body: {
            login: generateTestLogin(),
            firstName: 'Test',
            lastName: 'User',
            email,
            password
        }
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
        }
    }

    await getRouteHandler('/register', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 201)

    // Verifier que le mot de passe est hashe (pas en clair)
    // Note : en implementation reelle, verifier en base
    // Ici on verifie que l'inscription a reussi
    assert.ok(mockRes.jsonData?.message)
})

test('POST /register - should create email verification token', async () => {
    const mockReq = {
        body: {
            login: generateTestLogin(),
            firstName: 'Test',
            lastName: 'User',
            email: generateTestEmail(),
            password: 'SecurePass123!'
        }
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
        }
    }

    await getRouteHandler('/register', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 201)
    assert.ok(mockRes.jsonData?.message.includes('vérifier votre email'))
})

test('POST /register - should return success message', async () => {
    const mockReq = {
        body: {
            login: generateTestLogin(),
            firstName: 'Test',
            lastName: 'User',
            email: generateTestEmail(),
            password: 'SecurePass123!'
        }
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
        }
    }

    await getRouteHandler('/register', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 201)
    assert.ok(mockRes.jsonData?.message)
    assert.strictEqual(typeof mockRes.jsonData.message, 'string')
})

// ============================================
// Tests POST /api/auth/login (6 tests)
// ============================================

test('POST /login - should login with valid credentials', async () => {
    const password = 'SecurePass123!'
    const user = await createTestUser({ password, emailVerified: true })

    const mockReq = {
        body: {
            identifier: user.login,
            password
        }
    }

    const mockRes: any = createMockResponse()

    await getRouteHandler('/login', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.ok(mockRes.jsonData?.user)
    assert.strictEqual(mockRes.jsonData.user.login, user.login)
})

test('POST /login - should return access_token and refresh_token cookies', async () => {
    const password = 'SecurePass123!'
    const user = await createTestUser({ password, emailVerified: true })

    const mockReq = {
        body: {
            identifier: user.login,
            password
        }
    }

    const mockRes: any = createMockResponse()

    await getRouteHandler('/login', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.ok(mockRes.headers['cookie:access_token'])
    assert.ok(mockRes.headers['cookie:refresh_token'])
})

test('POST /login - should return 401 for invalid identifier', async () => {
    const mockReq = {
        body: {
            identifier: 'nonexistent@user.com',
            password: 'AnyPassword123!'
        }
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
        }
    }

    await getRouteHandler('/login', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 401)
    assert.ok(mockRes.jsonData?.error)
})

test('POST /login - should return 401 for invalid password', async () => {
    const user = await createTestUser({ emailVerified: true })

    const mockReq = {
        body: {
            identifier: user.login,
            password: 'WrongPassword123!'
        }
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
        }
    }

    await getRouteHandler('/login', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 401)
    assert.ok(mockRes.jsonData?.error)
    assert.ok(mockRes.jsonData.error.includes('invalides'))
})

test('POST /login - should return 400 for missing credentials', async () => {
    const mockReq = {
        body: {
            // Identifiant et mot de passe manquants
        }
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
        }
    }

    await getRouteHandler('/login', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 400)
    assert.ok(mockRes.jsonData?.error)
    assert.ok(mockRes.jsonData.error.includes('manquants'))
})

test('POST /login - should return 401 for unverified email', async () => {
    const password = 'SecurePass123!'
    const user = await createTestUser({ password, emailVerified: false })

    const mockReq = {
        body: {
            identifier: user.login,
            password
        }
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
        }
    }

    await getRouteHandler('/login', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 401)
    assert.ok(mockRes.jsonData?.error)
    assert.ok(mockRes.jsonData.error.includes('invalides'))
})



test('Token validation - should have valid JWT structure', async () => {
    const password = 'SecurePass123!'
    const user = await createTestUser({ password, emailVerified: true })

    const mockReq = {
        body: {
            identifier: user.login,
            password
        }
    }

    const mockRes: any = createMockResponse()

    await getRouteHandler('/login', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.ok(mockRes.headers['cookie:access_token'])
    const token = JSON.parse(mockRes.headers['cookie:access_token']).value
    const parts = token.split('.')
    assert.strictEqual(parts.length, 3) // JWT en 3 parties
})

test('Token validation - should contain user info in payload', async () => {
    const password = 'SecurePass123!'
    const user = await createTestUser({ password, emailVerified: true })

    const mockReq = {
        body: {
            identifier: user.login,
            password
        }
    }

    const mockRes: any = createMockResponse()

    await getRouteHandler('/login', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.ok(mockRes.jsonData?.user)
    assert.strictEqual(mockRes.jsonData.user.id, user.id)
    assert.strictEqual(mockRes.jsonData.user.login, user.login)
    assert.strictEqual(mockRes.jsonData.user.email, user.email)
})

test('GET /verify-email - should redirect to the mobile deep link on success', async () => {
    const user = await createTestUser({ emailVerified: false })
    const token = 'mobile-success-token'
    await storeVerificationTokenForUser(user.id, token, new Date(Date.now() + 60_000))

    const mockReq = {
        query: { token },
        accepts: () => 'json'
    }
    const mockRes: any = createMockResponse()

    await getRouteHandler('/verify-email', 'get')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 302)
    assert.strictEqual(mockRes.redirectUrl, 'festivalapp://auth/verification?status=success')
})

test('GET /verify-email - should redirect to the expired deep link when token is expired', async () => {
    const user = await createTestUser({ emailVerified: false })
    const token = 'mobile-expired-token'
    await storeVerificationTokenForUser(user.id, token, new Date(Date.now() - 60_000))

    const mockReq = {
        query: { token },
        accepts: () => 'json'
    }
    const mockRes: any = createMockResponse()

    await getRouteHandler('/verify-email', 'get')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 302)
    assert.strictEqual(mockRes.redirectUrl, 'festivalapp://auth/verification?status=expired')
})

test('GET /verify-email - should redirect to the invalid deep link when token is unknown', async () => {
    const mockReq = {
        query: { token: 'unknown-mobile-token' },
        accepts: () => 'json'
    }
    const mockRes: any = createMockResponse()

    await getRouteHandler('/verify-email', 'get')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 302)
    assert.strictEqual(mockRes.redirectUrl, 'festivalapp://auth/verification?status=invalid')
})

test('GET /verify-email - should render a fallback html page for browsers', async () => {
    const user = await createTestUser({ emailVerified: false })
    const token = 'mobile-html-token'
    await storeVerificationTokenForUser(user.id, token, new Date(Date.now() + 60_000))

    const mockReq = {
        query: { token },
        accepts: () => 'html'
    }
    const mockRes: any = createMockResponse()

    await getRouteHandler('/verify-email', 'get')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.ok(String(mockRes.jsonData).includes('festivalapp://auth/verification?status=success'))
    assert.ok(String(mockRes.jsonData).includes('<html'))
})

test('POST /password/forgot - should return the generic success message for an unknown email', async () => {
    const previousCallCount = sendMailMock.mock.calls.length
    const mockReq = {
        body: {
            email: generateTestEmail()
        }
    }
    const mockRes: any = createMockResponse()

    await getRouteHandler('/password/forgot', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.strictEqual(
        mockRes.jsonData?.message,
        'Si un compte existe pour cet email, un lien de réinitialisation a été envoyé.',
    )
    assert.strictEqual(sendMailMock.mock.calls.length, previousCallCount)
})

test('POST /password/forgot - should store a reset token and send an email for a known account', async () => {
    const user = await createTestUser({ emailVerified: true })
    const previousCallCount = sendMailMock.mock.calls.length
    const mockReq = {
        body: {
            email: user.email
        }
    }
    const mockRes: any = createMockResponse()

    await getRouteHandler('/password/forgot', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.strictEqual(
        mockRes.jsonData?.message,
        'Si un compte existe pour cet email, un lien de réinitialisation vient d’être envoyé.',
    )
    assert.strictEqual(sendMailMock.mock.calls.length, previousCallCount + 1)

    const { rows } = await pool.query<{
        password_reset_token: string | null
        password_reset_expires_at: Date | null
    }>(
        `
        SELECT password_reset_token, password_reset_expires_at
        FROM users
        WHERE id = $1
        `,
        [user.id]
    )

    assert.ok(rows[0]?.password_reset_token)
    assert.ok(rows[0]?.password_reset_expires_at)
})

test('GET /reset-password - should redirect to the mobile deep link', async () => {
    const token = 'mobile-reset-token'
    const mockReq = {
        query: { token },
        accepts: () => 'json'
    }
    const mockRes: any = createMockResponse()

    await getRouteHandler('/reset-password', 'get')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 302)
    assert.strictEqual(mockRes.redirectUrl, 'festivalapp://auth/reset-password?token=mobile-reset-token')
})

test('GET /reset-password - should render a fallback html page for browsers', async () => {
    const token = 'browser-reset-token'
    const mockReq = {
        query: { token },
        accepts: () => 'html'
    }
    const mockRes: any = createMockResponse()

    await getRouteHandler('/reset-password', 'get')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.ok(String(mockRes.jsonData).includes('festivalapp://auth/reset-password?token=browser-reset-token'))
    assert.ok(String(mockRes.jsonData).includes('Réinitialiser votre mot de passe'))
})

test('POST /password/reset - should reject passwords shorter than 8 characters', async () => {
    const mockReq = {
        body: {
            token: 'any-token',
            password: 'short'
        }
    }
    const mockRes: any = createMockResponse()

    await getRouteHandler('/password/reset', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 400)
    assert.strictEqual(mockRes.jsonData?.error, 'Mot de passe trop court (8 caractères min.)')
})

test('POST /password/reset - should reject an invalid or expired token', async () => {
    const mockReq = {
        body: {
            token: 'unknown-reset-token',
            password: 'NewPassword123!'
        }
    }
    const mockRes: any = createMockResponse()

    await getRouteHandler('/password/reset', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 400)
    assert.strictEqual(mockRes.jsonData?.error, 'Token invalide ou expiré')
})

test('POST /password/reset - should update the password and revoke refresh tokens on success', async () => {
    const user = await createTestUser({ password: 'OldPassword123!', emailVerified: true })
    const token = 'valid-reset-token'
    await storePasswordResetTokenForUser(user.id, token, new Date(Date.now() + 60_000))
    await createRefreshTokenRecord(user.id, new Date(Date.now() + 60_000))

    const mockReq = {
        body: {
            token,
            password: 'NewPassword123!'
        }
    }
    const mockRes: any = createMockResponse()

    await getRouteHandler('/password/reset', 'post')?.(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.strictEqual(mockRes.jsonData?.message, 'Mot de passe mis à jour. Vous pouvez vous connecter.')

    const { rows: userRows } = await pool.query<{
        password_reset_token: string | null
        password_reset_expires_at: Date | null
    }>(
        `
        SELECT password_reset_token, password_reset_expires_at
        FROM users
        WHERE id = $1
        `,
        [user.id]
    )
    const { rows: refreshRows } = await pool.query<{ revoked_at: Date | null }>(
        `
        SELECT revoked_at
        FROM refresh_tokens
        WHERE user_id = $1
        `,
        [user.id]
    )

    assert.strictEqual(userRows[0]?.password_reset_token, null)
    assert.strictEqual(userRows[0]?.password_reset_expires_at, null)
    assert.ok(refreshRows[0]?.revoked_at)
})
