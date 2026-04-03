import test from 'node:test';
import assert from 'node:assert/strict';
import pool from '../../db/database.js';
import { cleanupTestData } from '../test-helpers.js';
test('cleanupTestData supprime les fixtures games_test du catalogue jeux', async () => {
    const prefix = `games_test_${Date.now()}_cleanup`;
    const editorResult = await pool.query(`
      INSERT INTO editor (name, email, website, description)
      VALUES ($1, $2, $3, $4)
      RETURNING id
    `, [
        `${prefix}_editor`,
        `${prefix}@editor.test.com`,
        'https://example.com',
        'cleanup fixture',
    ]);
    const editorId = editorResult.rows[0]?.id;
    assert.ok(editorId, 'Un éditeur de test devait être créé');
    const mechanismResult = await pool.query(`
      INSERT INTO mechanism (name, description)
      VALUES ($1, $2)
      RETURNING id
    `, [`${prefix}_mechanism`, 'cleanup fixture']);
    const mechanismId = mechanismResult.rows[0]?.id;
    assert.ok(mechanismId, 'Un mécanisme de test devait être créé');
    const gameResult = await pool.query(`
      INSERT INTO games (title, type, editor_id, min_age, authors, min_players, max_players)
      VALUES ($1, $2, $3, $4, $5, $6, $7)
      RETURNING id
    `, [
        `${prefix}_title`,
        `${prefix}_type`,
        editorId,
        12,
        'cleanup author',
        1,
        4,
    ]);
    const gameId = gameResult.rows[0]?.id;
    assert.ok(gameId, 'Un jeu de test devait être créé');
    await pool.query('INSERT INTO game_mechanism (game_id, mechanism_id) VALUES ($1, $2)', [gameId, mechanismId]);
    await cleanupTestData();
    const remainingTypes = await pool.query("SELECT type FROM games WHERE type LIKE 'games_test_%'");
    const remainingMechanisms = await pool.query("SELECT name FROM mechanism WHERE name LIKE 'games_test_%'");
    const remainingEditors = await pool.query("SELECT email FROM editor WHERE email LIKE 'games_test_%@editor.test.com'");
    assert.equal(remainingTypes.rowCount, 0);
    assert.equal(remainingMechanisms.rowCount, 0);
    assert.equal(remainingEditors.rowCount, 0);
});
//# sourceMappingURL=test-data-cleanup.test.js.map