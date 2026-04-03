import type { CookieOptions } from 'express'

const ACCESS_COOKIE_MAX_AGE_MS = 15 * 60 * 1000
const REFRESH_COOKIE_MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000

const isHttpsEnabled = (): boolean => process.env.HTTPS_ENABLED !== 'false'

export const createAuthCookieBaseOptions = (): CookieOptions => ({
  httpOnly: true,
  secure: isHttpsEnabled(),
  sameSite: 'strict',
})

export const createAuthAccessCookieOptions = (): CookieOptions => ({
  ...createAuthCookieBaseOptions(),
  maxAge: ACCESS_COOKIE_MAX_AGE_MS,
})

export const createAuthRefreshCookieOptions = (): CookieOptions => ({
  ...createAuthCookieBaseOptions(),
  maxAge: REFRESH_COOKIE_MAX_AGE_MS,
})
