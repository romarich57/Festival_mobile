import bcrypt from 'bcryptjs';
import pool from './database.js';
import { ADMIN_EMAIL, ADMIN_LOGIN } from '../config/env.js';
// Role : Garantir l'existence du compte administrateur principal.
// Preconditions : La connexion a la base est disponible et les variables d'environnement sont chargees.
// Postconditions : Le compte admin est cree ou mis a jour avec les valeurs definies.
export async function ensureAdmin() {
    const login = ADMIN_LOGIN;
    const role = 'admin';
    const firstName = process.env.ADMIN_FIRST_NAME ?? 'Admin';
    const lastName = process.env.ADMIN_LAST_NAME ?? 'Account';
    const phone = process.env.ADMIN_PHONE ?? null;
    const avatarUrl = process.env.ADMIN_AVATAR_URL ?? null;
    const email = ADMIN_EMAIL;
    const password = process.env.ADMIN_PASSWORD;
    if (!password || password.trim().length === 0 || password === 'adminadmin') {
        throw new Error('ADMIN_PASSWORD doit etre defini avec une valeur non par defaut');
    }
    const passwordHash = await bcrypt.hash(password, 10);
    const adminValues = [login, passwordHash, role, firstName, lastName, email, phone, avatarUrl];
    const client = await pool.connect();
    try {
        await client.query('BEGIN');
        const { rows: matchingRows } = await client.query(`
      SELECT id
      FROM users
      WHERE login = $1 OR email = $2
      ORDER BY
        CASE
          WHEN login = $1 AND email = $2 THEN 0
          WHEN email = $2 THEN 1
          ELSE 2
        END,
        id
      LIMIT 2
      FOR UPDATE
      `, [login, email]);
        if (matchingRows.length > 1) {
            throw new Error('ADMIN_LOGIN et ADMIN_EMAIL correspondent a deux utilisateurs differents');
        }
        if (matchingRows.length === 0) {
            await client.query(`
        INSERT INTO users (
          login,
          password_hash,
          role,
          first_name,
          last_name,
          email,
          phone,
          avatar_url,
          email_verified,
          email_verification_token,
          email_verification_expires_at
        )
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, TRUE, NULL, NULL)
        `, adminValues);
        }
        else {
            const existingAdmin = matchingRows[0];
            await client.query(`
        UPDATE users
        SET
          login = $1,
          password_hash = $2,
          role = $3,
          first_name = $4,
          last_name = $5,
          email = $6,
          phone = $7,
          avatar_url = $8,
          email_verified = TRUE,
          email_verification_token = NULL,
          email_verification_expires_at = NULL
        WHERE id = $9
        `, [...adminValues, existingAdmin.id]);
        }
        await client.query('COMMIT');
    }
    catch (error) {
        await client.query('ROLLBACK');
        throw error;
    }
    finally {
        client.release();
    }
}
//# sourceMappingURL=initAdmin.js.map