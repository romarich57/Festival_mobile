// Role : Tester les routes /api/reservant.
import test from 'node:test'
import assert from 'node:assert/strict'
import pool from '../../db/database.js'
import reservantRouter from '../../routes/reservant.js'
import {
    createTestFestival,
    createTestReservant,
    generateTestEmail,
    setupTests,
    teardownTests
} from '../test-helpers.js'

type MockResponse = {
    statusCode: number
    jsonData: any
    headersSent?: boolean
    status: (code: number) => MockResponse
    json: (data: any) => MockResponse
}

type Handler = (req: any, res: MockResponse, next: () => void) => Promise<void> | void

function createMockResponse(): MockResponse {
    return {
        statusCode: 200,
        jsonData: null,
        headersSent: false,
        status(code: number) {
            this.statusCode = code
            return this
        },
        json(data: any) {
            this.jsonData = data
            this.headersSent = true
            return this
        }
    }
}

function getRouteStack(path: string, method: 'get' | 'post' | 'put' | 'patch' | 'delete'): Handler[] {
    const layer: any = reservantRouter.stack.find(
        (item: any) => item.route?.path === path && item.route?.methods?.[method]
    )
    return (layer?.route?.stack ?? []).map((entry: any) => entry.handle as Handler)
}

async function runRoute(
    path: string,
    method: 'get' | 'post' | 'put' | 'patch' | 'delete',
    req: any
) {
    const handlers = getRouteStack(path, method)
    assert.ok(handlers.length > 0, `Route ${method.toUpperCase()} ${path} introuvable`)

    const res = createMockResponse()

    const invokeAt = async (index: number): Promise<void> => {
        const handler = handlers[index]
        if (!handler || res.headersSent) {
            return
        }

        let nextCalled = false
        await handler(req, res, () => {
            nextCalled = true
        })

        if (nextCalled && !res.headersSent) {
            await invokeAt(index + 1)
        }
    }

    await invokeAt(0)
    return res
}

async function createReservantContact(input: {
    reservantId: number
    name?: string
    email?: string
    phoneNumber?: string
    jobTitle?: string
    priority?: number
}) {
    const { rows } = await pool.query(
        `
            INSERT INTO contact (name, email, phone_number, job_title, reservant_id, priority)
            VALUES ($1, $2, $3, $4, $5, $6)
            RETURNING id, name, email, phone_number, job_title, reservant_id, priority
        `,
        [
            input.name ?? 'Contact Test',
            input.email ?? generateTestEmail(),
            input.phoneNumber ?? '0601020304',
            input.jobTitle ?? 'Responsable',
            input.reservantId,
            input.priority ?? 1
        ]
    )

    return rows[0]
}

async function createWorkflowForReservant(reservantId: number, festivalId: number, state = 'Pas_de_contact') {
    const { rows } = await pool.query(
        `
            INSERT INTO suivi_workflow (reservant_id, festival_id, state)
            VALUES ($1, $2, $3)
            RETURNING id, reservant_id, festival_id, state
        `,
        [reservantId, festivalId, state]
    )

    return rows[0]
}

async function createContactEvent(contactId: number, workflowId: number, dateContact = new Date('2024-06-01T10:00:00Z')) {
    const { rows } = await pool.query(
        `
            INSERT INTO suivi_contact (contact_id, workflow_id, date_contact)
            VALUES ($1, $2, $3)
            RETURNING id, contact_id, workflow_id, date_contact
        `,
        [contactId, workflowId, dateContact]
    )

    return rows[0]
}

async function createReservationForReservant(input: {
    reservantId: number
    festivalId: number
    workflowId: number
}) {
    const { rows } = await pool.query(
        `
            INSERT INTO reservation (
                reservant_id,
                festival_id,
                workflow_id,
                start_price,
                table_discount_offered,
                direct_discount,
                nb_prises,
                final_price,
                statut_paiement
            )
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
            RETURNING id, reservant_id, festival_id, workflow_id, statut_paiement
        `,
        [input.reservantId, input.festivalId, input.workflowId, 100, 0, 0, 1, 100, 'non_payé']
    )

    return rows[0]
}

// Tests des routes /api/reservant (CRUD)

// Preparation et nettoyage
test.before(async () => {
    await setupTests()
})

test.after(async () => {
    await teardownTests()
})

// ============================================
// Tests GET /api/reservant (4 tests)
// ============================================

test('GET / - should return all reservants', async () => {
    // Creer des donnees de test
    await createTestReservant({ name: 'Reservant 1' })
    await createTestReservant({ name: 'Reservant 2' })

    const mockReq = {}
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/' && layer.route?.methods?.get)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.ok(Array.isArray(mockRes.jsonData))
    assert.ok(mockRes.jsonData.length >= 2)
})

test('GET / - should return empty array when no data', async () => {
    await teardownTests() // Vider toutes les donnees
    await setupTests()

    const mockReq = {}
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/')
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.ok(Array.isArray(mockRes.jsonData))
    assert.ok(
        mockRes.jsonData.every((item: any) => !String(item.email ?? '').endsWith('@test.com')),
        'Aucune fixture de test ne doit subsister après le nettoyage'
    )
})

test('GET / - should return correct fields', async () => {
    await createTestReservant()

    const mockReq = {}
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/')
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    const first = mockRes.jsonData[0]
    assert.ok(first.id)
    assert.ok(first.name)
    assert.ok(first.email)
    assert.ok(first.type)
})

test('GET / - should sort by name ASC', async () => {
    await createTestReservant({ name: 'Zebra' })
    await createTestReservant({ name: 'Alpha' })
    await createTestReservant({ name: 'Beta' })

    const mockReq = {}
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/')
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    const names = mockRes.jsonData.map((r: any) => r.name)
    const sorted = [...names].sort()
    assert.deepStrictEqual(names, sorted)
})

// ============================================
// Tests GET /api/reservant/:id (4 tests)
// ============================================

test('GET /:id - should return specific reservant', async () => {
    const reservant = await createTestReservant({ name: 'Specific Reservant' })

    const mockReq = {
        params: { id: reservant.id }
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/:id' && layer.route?.methods?.get)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.strictEqual(mockRes.jsonData.id, reservant.id)
    assert.strictEqual(mockRes.jsonData.name, 'Specific Reservant')
})

test('GET /:id - should return 404 if not found', async () => {
    const mockReq = {
        params: { id: 99999 }
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/:id')
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 404)
    assert.ok(mockRes.jsonData?.error)
})

test('GET /:id - should validate ID parameter', async () => {
    const reservant = await createTestReservant()

    const mockReq = {
        params: { id: reservant.id.toString() }
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/:id')
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.ok(mockRes.jsonData.id)
})

test('GET /:id - should return correct structure', async () => {
    const reservant = await createTestReservant()

    const mockReq = {
        params: { id: reservant.id }
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/:id')
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.ok(mockRes.jsonData.id)
    assert.ok(mockRes.jsonData.name)
    assert.ok(mockRes.jsonData.email)
    assert.ok(mockRes.jsonData.type)
})

// ============================================
// Tests POST /api/reservant (8 tests)
// ============================================

test('POST / - should create reservant with required fields', async () => {
    const mockReq = {
        body: {
            name: 'New Reservant',
            email: generateTestEmail(),
            type: 'editeur'
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/' && layer.route?.methods?.post)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 201)
    assert.ok(mockRes.jsonData.id)
    assert.strictEqual(mockRes.jsonData.name, 'New Reservant')
})

test('POST / - should return created reservant with ID', async () => {
    const mockReq = {
        body: {
            name: 'Test',
            email: generateTestEmail(),
            type: 'boutique'
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/' && layer.route?.methods?.post)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 201)
    assert.ok(mockRes.jsonData.id)
    assert.strictEqual(typeof mockRes.jsonData.id, 'number')
})

test('POST / - should reject missing name', async () => {
    const mockReq = {
        body: {
            email: generateTestEmail(),
            type: 'editeur'
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/' && layer.route?.methods?.post)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 400)
    assert.ok(mockRes.jsonData?.error)
})

test('POST / - should reject missing email', async () => {
    const mockReq = {
        body: {
            name: 'Test',
            type: 'editeur'
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/' && layer.route?.methods?.post)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 400)
    assert.ok(mockRes.jsonData?.error)
})

test('POST / - should reject missing type', async () => {
    const mockReq = {
        body: {
            name: 'Test',
            email: generateTestEmail()
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/' && layer.route?.methods?.post)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 400)
    assert.ok(mockRes.jsonData?.error)
})

test('POST / - should reject invalid type value', async () => {
    const mockReq = {
        body: {
            name: 'Test',
            email: generateTestEmail(),
            type: 'invalid_type'
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/' && layer.route?.methods?.post)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 400)
    assert.ok(mockRes.jsonData?.error)
    assert.ok(mockRes.jsonData.error.includes('Type invalide'))
})

test('POST / - should reject duplicate email', async () => {
    const email = generateTestEmail()
    await createTestReservant({ email })

    const mockReq = {
        body: {
            name: 'Duplicate',
            email: email,
            type: 'editeur'
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/' && layer.route?.methods?.post)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 409)
    assert.ok(mockRes.jsonData?.error)
})

test('POST / - should set optional fields to null if not provided', async () => {
    const mockReq = {
        body: {
            name: 'Minimal',
            email: generateTestEmail(),
            type: 'editeur'
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/' && layer.route?.methods?.post)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 201)
    assert.strictEqual(mockRes.jsonData.phone_number, null)
    assert.strictEqual(mockRes.jsonData.address, null)
})

// ============================================
// Tests PUT /api/reservant/:id (6 tests)
// ============================================

test('PUT /:id - should update reservant', async () => {
    const reservant = await createTestReservant()

    const mockReq = {
        params: { id: reservant.id },
        body: {
            name: 'Updated Name',
            email: reservant.email,
            type: 'boutique'
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/:id' && layer.route?.methods?.put)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.strictEqual(mockRes.jsonData.name, 'Updated Name')
    assert.strictEqual(mockRes.jsonData.type, 'boutique')
})

test('PUT /:id - should return updated data', async () => {
    const reservant = await createTestReservant()

    const mockReq = {
        params: { id: reservant.id },
        body: {
            name: 'New Name',
            email: reservant.email,
            type: reservant.type
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/:id' && layer.route?.methods?.put)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.ok(mockRes.jsonData.id)
    assert.strictEqual(mockRes.jsonData.name, 'New Name')
})

test('PUT /:id - should return 404 if not found', async () => {
    const mockReq = {
        params: { id: 99999 },
        body: {
            name: 'Test',
            email: generateTestEmail(),
            type: 'editeur'
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/:id' && layer.route?.methods?.put)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 404)
    assert.ok(mockRes.jsonData?.error)
})

test('PUT /:id - should reject duplicate email', async () => {
    const reservant1 = await createTestReservant()
    const reservant2 = await createTestReservant()

    const mockReq = {
        params: { id: reservant2.id },
        body: {
            name: reservant2.name,
            email: reservant1.email, // Doublon
            type: reservant2.type
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/:id' && layer.route?.methods?.put)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 409)
    assert.ok(mockRes.jsonData?.error)
})

test('PUT /:id - should update all fields correctly', async () => {
    const reservant = await createTestReservant()

    const mockReq = {
        params: { id: reservant.id },
        body: {
            name: 'Updated',
            email: generateTestEmail(),
            type: 'association',
            phone_number: '9876543210',
            address: 'New Address',
            siret: '98765432109876',
            notes: 'Updated notes'
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/:id' && layer.route?.methods?.put)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.strictEqual(mockRes.jsonData.name, 'Updated')
    assert.strictEqual(mockRes.jsonData.type, 'association')
    assert.strictEqual(mockRes.jsonData.phone_number, '9876543210')
})

test('PUT /:id - should handle null optional fields', async () => {
    const reservant = await createTestReservant({
        phone_number: '123',
        address: 'Test'
    })

    const mockReq = {
        params: { id: reservant.id },
        body: {
            name: reservant.name,
            email: reservant.email,
            type: reservant.type,
            phone_number: null,
            address: null
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/:id' && layer.route?.methods?.put)
        ?.route?.stack[0]?.handle(mockReq as any, mockRes as any, () => {})

    assert.strictEqual(mockRes.statusCode, 200)
    assert.strictEqual(mockRes.jsonData.phone_number, null)
    assert.strictEqual(mockRes.jsonData.address, null)
})

// ============================================
// Tests DELETE /api/reservant/:id (5 tests)
// ============================================

test('DELETE /:id - should delete reservant', async () => {
    const reservant = await createTestReservant()
    const res = await runRoute('/:id', 'delete', {
        params: { id: reservant.id.toString() },
        user: { role: 'admin' }
    })

    assert.strictEqual(res.statusCode, 200)
    assert.ok(res.jsonData.message)
})

test('DELETE /:id - should return 404 if not found', async () => {
    const res = await runRoute('/:id', 'delete', {
        params: { id: '99999' },
        user: { role: 'admin' }
    })

    assert.strictEqual(res.statusCode, 404)
    assert.ok(res.jsonData?.error)
})

test('DELETE /:id - should return success message', async () => {
    const reservant = await createTestReservant()
    const res = await runRoute('/:id', 'delete', {
        params: { id: reservant.id.toString() },
        user: { role: 'admin' }
    })

    assert.strictEqual(res.statusCode, 200)
    assert.ok(res.jsonData.message)
    assert.ok(res.jsonData.message.includes('supprimé'))
})

test('DELETE /:id - should verify actually deleted from DB', async () => {
    const reservant = await createTestReservant()
    await runRoute('/:id', 'delete', {
        params: { id: reservant.id.toString() },
        user: { role: 'admin' }
    })

    const mockReqGet = {
        params: { id: reservant.id }
    }
    const mockResGet = {
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

    await reservantRouter.stack
        .find((layer: any) => layer.route?.path === '/:id' && layer.route?.methods?.get)
        ?.route?.stack[0]?.handle(mockReqGet as any, mockResGet as any, () => {})

    assert.strictEqual(mockResGet.statusCode, 404)
})

test('DELETE /:id - should reject forbidden role before delete handler', async () => {
    const reservant = await createTestReservant()
    const res = await runRoute('/:id', 'delete', {
        params: { id: reservant.id.toString() },
        user: { role: 'organizer' }
    })

    assert.strictEqual(res.statusCode, 403)
    assert.strictEqual(res.jsonData.error, 'Acces interdit')
})

// ============================================
// Tests normalisation des erreurs / delete-summary / contacts
// ============================================

test('POST / - should normalize database details as string array', async () => {
    const originalQuery = pool.query.bind(pool)
    let callCount = 0

    ;(pool as any).query = async (...args: any[]) => {
        callCount += 1
        if (callCount === 1) {
            return { rows: [] }
        }
        throw { code: '23502', detail: 'insert detail' }
    }

    try {
        const res = await runRoute('/', 'post', {
            body: {
                name: 'Stub Reservant',
                email: generateTestEmail(),
                type: 'editeur'
            }
        })

        assert.strictEqual(res.statusCode, 400)
        assert.strictEqual(res.jsonData.error, 'Champ requis manquant')
        assert.deepStrictEqual(res.jsonData.details, ['insert detail'])
    } finally {
        ;(pool as any).query = originalQuery
    }
})

test('GET /:id/delete-summary - should reject invalid reservant id', async () => {
    const res = await runRoute('/:id/delete-summary', 'get', {
        params: { id: 'abc' }
    })

    assert.strictEqual(res.statusCode, 400)
    assert.strictEqual(res.jsonData.error, 'Identifiant de réservant invalide')
})

test('GET /:id/delete-summary - should return 404 for unknown reservant', async () => {
    const res = await runRoute('/:id/delete-summary', 'get', {
        params: { id: '99999' }
    })

    assert.strictEqual(res.statusCode, 404)
    assert.strictEqual(res.jsonData.error, 'Réservant non trouvé')
})

test('GET /:id/delete-summary - should include linked contacts workflows and reservations', async () => {
    const reservant = await createTestReservant({ name: 'Summary Reservant' })
    const festival = await createTestFestival()
    const contact = await createReservantContact({ reservantId: reservant.id, name: 'Summary Contact' })
    const workflow = await createWorkflowForReservant(reservant.id, festival.id)
    const reservation = await createReservationForReservant({
        reservantId: reservant.id,
        festivalId: festival.id,
        workflowId: workflow.id
    })

    const res = await runRoute('/:id/delete-summary', 'get', {
        params: { id: reservant.id.toString() }
    })

    assert.strictEqual(res.statusCode, 200)
    assert.strictEqual(res.jsonData.reservant_id, reservant.id)
    assert.deepStrictEqual(res.jsonData.contacts.map((item: any) => item.id), [contact.id])
    assert.deepStrictEqual(res.jsonData.workflows.map((item: any) => item.id), [workflow.id])
    assert.deepStrictEqual(res.jsonData.reservations.map((item: any) => item.id), [reservation.id])
})

test('GET /:id/contacts - should reject invalid reservant id', async () => {
    const res = await runRoute('/:id/contacts', 'get', {
        params: { id: 'abc' }
    })

    assert.strictEqual(res.statusCode, 400)
    assert.strictEqual(res.jsonData.error, 'Identifiant de réservant invalide')
})

test('GET /:id/contacts - should return 404 for unknown reservant', async () => {
    const res = await runRoute('/:id/contacts', 'get', {
        params: { id: '99999' }
    })

    assert.strictEqual(res.statusCode, 404)
    assert.strictEqual(res.jsonData.error, 'Réservant non trouvé')
})

test('POST /:id/contacts - should return 404 when reservant does not exist', async () => {
    const res = await runRoute('/:id/contacts', 'post', {
        params: { id: '99999' },
        body: {
            name: 'Missing Reservant Contact',
            email: generateTestEmail(),
            phone_number: '0601020304',
            job_title: 'Coordination',
            priority: 1
        }
    })

    assert.strictEqual(res.statusCode, 404)
    assert.strictEqual(res.jsonData.error, 'Réservant non trouvé')
})

test('POST /:id/contacts/events - should reject invalid date payload', async () => {
    const reservant = await createTestReservant({ name: 'Date Reservant' })

    const res = await runRoute('/:id/contacts/events', 'post', {
        params: { id: reservant.id.toString() },
        body: {
            contactId: 1,
            dateContact: 'not-a-date'
        }
    })

    assert.strictEqual(res.statusCode, 400)
    assert.strictEqual(res.jsonData.error, 'dateContact invalide')
})

test('POST /:id/contacts/events - should return 409 when contact belongs to another reservant', async () => {
    const reservant = await createTestReservant({ name: 'Owner Reservant' })
    const otherReservant = await createTestReservant({ name: 'Other Reservant' })
    const festival = await createTestFestival()
    const workflow = await createWorkflowForReservant(reservant.id, festival.id)
    const foreignContact = await createReservantContact({
        reservantId: otherReservant.id,
        name: 'Foreign Contact'
    })

    const res = await runRoute('/:id/contacts/events', 'post', {
        params: { id: reservant.id.toString() },
        body: {
            contactId: foreignContact.id
        }
    })

    assert.strictEqual(res.statusCode, 409)
    assert.strictEqual(res.jsonData.error, 'Ce contact n’appartient pas à ce réservant')
    assert.ok(workflow.id)
})

test('DELETE /:id/contacts/:contactId - should return 409 when contact belongs to another reservant', async () => {
    const reservant = await createTestReservant({ name: 'Delete Contact Owner' })
    const otherReservant = await createTestReservant({ name: 'Delete Contact Other' })
    const foreignContact = await createReservantContact({
        reservantId: otherReservant.id,
        name: 'Delete Foreign Contact'
    })

    const res = await runRoute('/:id/contacts/:contactId', 'delete', {
        params: {
            id: reservant.id.toString(),
            contactId: foreignContact.id.toString()
        }
    })

    assert.strictEqual(res.statusCode, 409)
    assert.strictEqual(res.jsonData.error, 'Ce contact n’appartient pas à ce réservant')
})

test('DELETE /:id/contacts/events/:eventId - should return 409 when event belongs to another reservant', async () => {
    const reservant = await createTestReservant({ name: 'Delete Event Owner' })
    const otherReservant = await createTestReservant({ name: 'Delete Event Other' })
    const festival = await createTestFestival()
    const otherWorkflow = await createWorkflowForReservant(otherReservant.id, festival.id)
    const otherContact = await createReservantContact({
        reservantId: otherReservant.id,
        name: 'Event Contact'
    })
    const foreignEvent = await createContactEvent(otherContact.id, otherWorkflow.id)

    const res = await runRoute('/:id/contacts/events/:eventId', 'delete', {
        params: {
            id: reservant.id.toString(),
            eventId: foreignEvent.id.toString()
        }
    })

    assert.strictEqual(res.statusCode, 409)
    assert.strictEqual(res.jsonData.error, 'Cet événement n’appartient pas à ce réservant')
})
