const ACCESS_COOKIE_MAX_AGE_MS = 15 * 60 * 1000;
const REFRESH_COOKIE_MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000;
const isHttpsEnabled = () => process.env.HTTPS_ENABLED !== 'false';
export const createAuthCookieBaseOptions = () => ({
    httpOnly: true,
    secure: isHttpsEnabled(),
    sameSite: 'strict',
});
export const createAuthAccessCookieOptions = () => ({
    ...createAuthCookieBaseOptions(),
    maxAge: ACCESS_COOKIE_MAX_AGE_MS,
});
export const createAuthRefreshCookieOptions = () => ({
    ...createAuthCookieBaseOptions(),
    maxAge: REFRESH_COOKIE_MAX_AGE_MS,
});
//# sourceMappingURL=auth-cookie-options.js.map