// Role : Tester les helpers du flux de verification mobile.
import test from 'node:test';
import assert from 'node:assert/strict';
import { buildVerificationDeepLink, renderVerificationFallbackHtml, } from '../../routes/auth.js';
import { MOBILE_DEEP_LINK_BASE } from '../../config/env.js';
test('buildVerificationDeepLink should append the verification status to the mobile deep link base', () => {
    const deepLink = buildVerificationDeepLink('expired');
    const expectedBase = MOBILE_DEEP_LINK_BASE.replace(/\/$/, '');
    assert.ok(deepLink.startsWith(expectedBase));
    assert.ok(deepLink.includes('status=expired'));
});
test('renderVerificationFallbackHtml should expose the deep link and a fallback message', () => {
    const deepLink = buildVerificationDeepLink('success');
    const html = renderVerificationFallbackHtml({
        status: 'success',
        deepLink,
    });
    assert.ok(html.includes(deepLink));
    assert.ok(html.includes("Ouvrir l'application"));
    assert.ok(html.includes('Email vérifié'));
});
//# sourceMappingURL=auth.mobile-verification.test.js.map