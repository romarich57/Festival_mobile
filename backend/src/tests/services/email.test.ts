// Role : Tester la construction des liens email transactionnels.
import test from 'node:test'
import assert from 'node:assert/strict'
import { PUBLIC_BACKEND_URL } from '../../config/env.js'
import {
  buildPasswordResetEmailUrl,
  buildVerificationEmailUrl,
} from '../../services/email.js'

test('buildVerificationEmailUrl should target the backend verification endpoint', () => {
  const token = 'token avec espace'
  const verificationUrl = buildVerificationEmailUrl(token)
  const parsedUrl = new URL(verificationUrl)

  assert.ok(
    verificationUrl.startsWith(
      `${PUBLIC_BACKEND_URL.replace(/\/$/, '')}/api/auth/verify-email?token=`,
    ),
  )
  assert.strictEqual(parsedUrl.searchParams.get('token'), token)
})

test('buildPasswordResetEmailUrl should target the backend password reset endpoint', () => {
  const token = 'reset token avec espace'
  const resetUrl = buildPasswordResetEmailUrl(token)
  const parsedUrl = new URL(resetUrl)

  assert.ok(
    resetUrl.startsWith(
      `${PUBLIC_BACKEND_URL.replace(/\/$/, '')}/api/auth/reset-password?token=`,
    ),
  )
  assert.strictEqual(parsedUrl.searchParams.get('token'), token)
})
