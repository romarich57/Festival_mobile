// Role : Verifier que le module base de donnees charge les variables d'environnement.
import test from 'node:test'
import assert from 'node:assert/strict'

test('database charge DATABASE_URL depuis .env meme si aucun autre module ne l a encore charge', async () => {
  const previousDatabaseUrl = process.env.DATABASE_URL

  delete process.env.DATABASE_URL

  try {
    const moduleUrl = new URL(`../../db/database.js?test=${Date.now()}`, import.meta.url).href
    await assert.doesNotReject(import(moduleUrl))
    assert.ok(process.env.DATABASE_URL, 'DATABASE_URL devrait etre charge depuis .env')
  } finally {
    if (previousDatabaseUrl) {
      process.env.DATABASE_URL = previousDatabaseUrl
    } else {
      delete process.env.DATABASE_URL
    }
  }
})
