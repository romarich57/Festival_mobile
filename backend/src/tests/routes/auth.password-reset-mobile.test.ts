// Role : Tester les helpers du flux mobile de reset password.
import test from 'node:test'
import assert from 'node:assert/strict'
import {
  buildPasswordResetDeepLink,
  renderPasswordResetFallbackHtml,
} from '../../routes/auth.js'
import { MOBILE_PASSWORD_RESET_DEEP_LINK_BASE } from '../../config/env.js'

test('buildPasswordResetDeepLink should append the token to the mobile deep link base', () => {
  const token = 'reset-token-123'
  const deepLink = buildPasswordResetDeepLink(token)
  const expectedBase = MOBILE_PASSWORD_RESET_DEEP_LINK_BASE.replace(/\/$/, '')

  assert.ok(deepLink.startsWith(expectedBase))
  assert.ok(deepLink.includes('token=reset-token-123'))
})

test('renderPasswordResetFallbackHtml should expose the deep link and fallback CTA', () => {
  const token = 'fallback-token'
  const deepLink = buildPasswordResetDeepLink(token)
  const html = renderPasswordResetFallbackHtml(token)

  assert.ok(html.includes(deepLink))
  assert.ok(html.includes("Ouvrir l'application"))
  assert.ok(html.includes('Réinitialiser votre mot de passe'))
})
