import test from 'node:test'
import assert from 'node:assert/strict'
import pool from '../../db/database.js'

function uniqueAdminSeed() {
  const suffix = `${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
  return {
    email: `admin_bootstrap_${suffix}@test.com`,
    login: `admin_bootstrap_${suffix}`,
    legacyLogin: `legacy_admin_${suffix}`,
  }
}

async function cleanupAdminCandidates(email: string, login: string, legacyLogin: string) {
  await pool.query(
    `
    DELETE FROM users
    WHERE email = $1
       OR login = $2
       OR login = $3
    `,
    [email, login, legacyLogin],
  )
}

test('ensureAdmin updates the existing account when ADMIN_EMAIL already exists', async () => {
  const previousAdminEmail = process.env.ADMIN_EMAIL
  const previousAdminLogin = process.env.ADMIN_LOGIN
  const previousAdminPassword = process.env.ADMIN_PASSWORD
  const previousAdminFirstName = process.env.ADMIN_FIRST_NAME
  const previousAdminLastName = process.env.ADMIN_LAST_NAME
  const previousAdminPhone = process.env.ADMIN_PHONE
  const previousAdminAvatarUrl = process.env.ADMIN_AVATAR_URL

  const adminSeed = uniqueAdminSeed()

  process.env.ADMIN_EMAIL = adminSeed.email
  process.env.ADMIN_LOGIN = adminSeed.login
  process.env.ADMIN_PASSWORD = 'StrongAdminPassword123!'
  process.env.ADMIN_FIRST_NAME = 'Principal'
  process.env.ADMIN_LAST_NAME = 'Admin'
  process.env.ADMIN_PHONE = '0102030405'
  process.env.ADMIN_AVATAR_URL = 'https://example.com/admin.png'

  await cleanupAdminCandidates(adminSeed.email, adminSeed.login, adminSeed.legacyLogin)

  try {
    const { rows: existingRows } = await pool.query<{
      id: number
      login: string
      role: string
      email_verified: boolean
      password_hash: string
    }>(
      `
      INSERT INTO users (
        login,
        password_hash,
        role,
        first_name,
        last_name,
        email,
        email_verified
      )
      VALUES ($1, $2, $3, $4, $5, $6, FALSE)
      RETURNING id, login, role, email_verified, password_hash
      `,
      [adminSeed.legacyLogin, 'legacy-hash', 'benevole', 'Legacy', 'Owner', adminSeed.email],
    )

    const existingUser = existingRows[0]
    assert.ok(existingUser, 'Le compte de depart doit exister')

    const moduleUrl = new URL(`../../db/initAdmin.js?test=${Date.now()}`, import.meta.url).href
    const { ensureAdmin } = await import(moduleUrl)

    await assert.doesNotReject(ensureAdmin())

    const { rows: users } = await pool.query<{
      id: number
      login: string
      role: string
      first_name: string
      last_name: string
      email: string
      phone: string | null
      avatar_url: string | null
      email_verified: boolean
      email_verification_token: string | null
      email_verification_expires_at: Date | null
      password_hash: string
    }>(
      `
      SELECT
        id,
        login,
        role,
        first_name,
        last_name,
        email,
        phone,
        avatar_url,
        email_verified,
        email_verification_token,
        email_verification_expires_at,
        password_hash
      FROM users
      WHERE email = $1 OR login = $2
      ORDER BY id
      `,
      [adminSeed.email, adminSeed.login],
    )

    assert.strictEqual(users.length, 1)
    assert.strictEqual(users[0]?.id, existingUser.id)
    assert.strictEqual(users[0]?.login, adminSeed.login)
    assert.strictEqual(users[0]?.role, 'admin')
    assert.strictEqual(users[0]?.first_name, 'Principal')
    assert.strictEqual(users[0]?.last_name, 'Admin')
    assert.strictEqual(users[0]?.email, adminSeed.email)
    assert.strictEqual(users[0]?.phone, '0102030405')
    assert.strictEqual(users[0]?.avatar_url, 'https://example.com/admin.png')
    assert.strictEqual(users[0]?.email_verified, true)
    assert.strictEqual(users[0]?.email_verification_token, null)
    assert.strictEqual(users[0]?.email_verification_expires_at, null)
    assert.notStrictEqual(users[0]?.password_hash, existingUser.password_hash)
  } finally {
    if (previousAdminEmail === undefined) {
      delete process.env.ADMIN_EMAIL
    } else {
      process.env.ADMIN_EMAIL = previousAdminEmail
    }

    if (previousAdminLogin === undefined) {
      delete process.env.ADMIN_LOGIN
    } else {
      process.env.ADMIN_LOGIN = previousAdminLogin
    }

    if (previousAdminPassword === undefined) {
      delete process.env.ADMIN_PASSWORD
    } else {
      process.env.ADMIN_PASSWORD = previousAdminPassword
    }

    if (previousAdminFirstName === undefined) {
      delete process.env.ADMIN_FIRST_NAME
    } else {
      process.env.ADMIN_FIRST_NAME = previousAdminFirstName
    }

    if (previousAdminLastName === undefined) {
      delete process.env.ADMIN_LAST_NAME
    } else {
      process.env.ADMIN_LAST_NAME = previousAdminLastName
    }

    if (previousAdminPhone === undefined) {
      delete process.env.ADMIN_PHONE
    } else {
      process.env.ADMIN_PHONE = previousAdminPhone
    }

    if (previousAdminAvatarUrl === undefined) {
      delete process.env.ADMIN_AVATAR_URL
    } else {
      process.env.ADMIN_AVATAR_URL = previousAdminAvatarUrl
    }

    await cleanupAdminCandidates(adminSeed.email, adminSeed.login, adminSeed.legacyLogin)
  }
})
