package com.projetmobile.mobile.data.repository.reservants

import android.content.ContextWrapper
import com.projetmobile.mobile.data.dao.ReservantDao
import com.projetmobile.mobile.data.database.SyncPreferenceStore
import com.projetmobile.mobile.data.remote.reservants.DeleteReservantResponseDto
import com.projetmobile.mobile.data.remote.reservants.ReservantContactDto
import com.projetmobile.mobile.data.remote.reservants.ReservantContactUpsertRequestDto
import com.projetmobile.mobile.data.remote.reservants.ReservantDeleteSummaryDto
import com.projetmobile.mobile.data.remote.reservants.ReservantDto
import com.projetmobile.mobile.data.remote.reservants.ReservantEditorDto
import com.projetmobile.mobile.data.remote.reservants.ReservantUpsertRequestDto
import com.projetmobile.mobile.data.remote.reservants.ReservantsApiService
import com.projetmobile.mobile.data.room.ReservantRoomEntity
import com.projetmobile.mobile.data.room.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReservantsRepositoryImplTest {

    @Test
    fun deleteReservant_marksSyncedItemForDeletionAndSchedulesSync() = runTest {
        val dao = FakeReservantDao(
            initialReservants = listOf(reservantRoomEntity(id = 14, name = "Blue Fox")),
        )
        val service = FakeReservantsApiService()
        var scheduled = false
        val repository = ReservantsRepositoryImpl(
            reservantsApiService = service,
            reservantDao = dao,
            syncPreferenceStore = FakeReservantSyncPreferenceStore(),
            context = ContextWrapper(null),
            syncScheduler = { scheduled = true },
        )

        val message = repository.deleteReservant(14).getOrThrow()

        assertEquals("Suppression planifiée.", message)
        assertTrue(scheduled)
        assertEquals(0, service.deleteReservantCalls)
        assertEquals(SyncStatus.PENDING_DELETE, dao.getById(14)?.syncStatus)
    }

    @Test
    fun deleteReservant_removesPendingCreateItemLocally() = runTest {
        val dao = FakeReservantDao(
            initialReservants = listOf(
                reservantRoomEntity(
                    id = -4,
                    name = "Local Only",
                    syncStatus = SyncStatus.PENDING_CREATE,
                ),
            ),
        )
        val service = FakeReservantsApiService()
        var scheduled = false
        val repository = ReservantsRepositoryImpl(
            reservantsApiService = service,
            reservantDao = dao,
            syncPreferenceStore = FakeReservantSyncPreferenceStore(),
            context = ContextWrapper(null),
            syncScheduler = { scheduled = true },
        )

        val message = repository.deleteReservant(-4).getOrThrow()

        assertEquals("Réservant supprimé localement.", message)
        assertNull(dao.getById(-4))
        assertEquals(0, service.deleteReservantCalls)
        assertTrue(!scheduled)
    }
}

private class FakeReservantDao(
    initialReservants: List<ReservantRoomEntity> = emptyList(),
) : ReservantDao {
    private val store = MutableStateFlow(initialReservants.associateBy { it.id })

    override fun observeAll(): Flow<List<ReservantRoomEntity>> =
        store.map { reservants -> reservants.values.sortedBy { it.name } }

    override fun observeById(id: Int): Flow<ReservantRoomEntity?> = store.map { it[id] }

    override suspend fun getById(id: Int): ReservantRoomEntity? = store.value[id]

    override suspend fun getPending(): List<ReservantRoomEntity> = emptyList()

    override suspend fun upsertAll(reservants: List<ReservantRoomEntity>) {
        store.value = store.value + reservants.associateBy { it.id }
    }

    override suspend fun upsert(reservant: ReservantRoomEntity) {
        store.value = store.value + (reservant.id to reservant)
    }

    override suspend fun deleteById(id: Int) {
        store.value = store.value - id
    }

    override suspend fun markForDeletion(id: Int) {
        val reservant = store.value[id] ?: return
        store.value = store.value + (id to reservant.copy(syncStatus = SyncStatus.PENDING_DELETE))
    }

    override suspend fun updateSyncStatus(id: Int, status: String) {
        val reservant = store.value[id] ?: return
        store.value = store.value + (id to reservant.copy(syncStatus = status))
    }
}

private class FakeReservantSyncPreferenceStore : SyncPreferenceStore(ContextWrapper(null)) {
    override suspend fun getLastSyncedAt(key: String): Long? = null
    override suspend fun setLastSyncedAt(key: String, timestamp: Long) {}
    override suspend fun needsRefresh(key: String, ttlMs: Long): Boolean = true
    override suspend fun invalidate(key: String) {}
}

private class FakeReservantsApiService : ReservantsApiService {
    var deleteReservantCalls: Int = 0

    override suspend fun getReservants(): List<ReservantDto> = emptyList()

    override suspend fun getReservant(reservantId: Int): ReservantDto =
        ReservantDto(id = reservantId, name = "Test", email = "test@example.com", type = "editeur")

    override suspend fun createReservant(request: ReservantUpsertRequestDto): ReservantDto =
        ReservantDto(id = 1, name = request.name, email = request.email, type = request.type)

    override suspend fun updateReservant(
        reservantId: Int,
        request: ReservantUpsertRequestDto,
    ): ReservantDto = ReservantDto(id = reservantId, name = request.name, email = request.email, type = request.type)

    override suspend fun deleteReservant(reservantId: Int): DeleteReservantResponseDto {
        deleteReservantCalls += 1
        return DeleteReservantResponseDto(message = "deleted:$reservantId")
    }

    override suspend fun getDeleteSummary(reservantId: Int): ReservantDeleteSummaryDto =
        ReservantDeleteSummaryDto(reservantId = reservantId)

    override suspend fun getContacts(reservantId: Int): List<ReservantContactDto> = emptyList()

    override suspend fun addContact(
        reservantId: Int,
        request: ReservantContactUpsertRequestDto,
    ): ReservantContactDto = ReservantContactDto(
        id = 1,
        name = request.name,
        email = request.email,
        phoneNumber = request.phoneNumber,
        jobTitle = request.jobTitle,
        priority = request.priority,
    )

    override suspend fun getEditors(): List<ReservantEditorDto> = emptyList()
}

private fun reservantRoomEntity(
    id: Int,
    name: String,
    syncStatus: String = SyncStatus.SYNCED,
) = ReservantRoomEntity(
    id = id,
    name = name,
    email = "$name@example.com",
    type = "editeur",
    editorId = 9,
    phoneNumber = null,
    address = null,
    siret = null,
    notes = null,
    syncStatus = syncStatus,
    pendingDraftJson = null,
)
