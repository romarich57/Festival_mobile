package com.projetmobile.mobile.data.repository.reservation

import android.content.ContextWrapper
import com.projetmobile.mobile.data.dao.ReservationDao
import com.projetmobile.mobile.data.database.SyncPreferenceStore
import com.projetmobile.mobile.data.remote.reservation.ReservationApiService
import com.projetmobile.mobile.data.remote.reservation.ReservationCreatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.ReservationDashboardRowDto
import com.projetmobile.mobile.data.remote.reservation.ReservationDetailsDto
import com.projetmobile.mobile.data.remote.reservation.ReservationUpdatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.WorkflowDto
import com.projetmobile.mobile.data.remote.reservation.WorkflowUpdatePayload
import com.projetmobile.mobile.data.remote.reservation.ZoneTarifaireDto
import com.projetmobile.mobile.data.room.ReservationRoomEntity
import com.projetmobile.mobile.data.room.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReservationRepositoryImplTest {

    @Test
    fun deleteReservation_marksSyncedItemForDeletionAndSchedulesSync() = runTest {
        val dao = FakeReservationDao(
            initialReservations = listOf(reservationRoomEntity(id = 21)),
        )
        val service = FakeReservationApiService()
        var scheduled = false
        val repository = ReservationRepositoryImpl(
            api = service,
            reservationDao = dao,
            syncPreferenceStore = FakeReservationSyncPreferenceStore(),
            context = ContextWrapper(null),
            syncScheduler = { scheduled = true },
        )

        repository.deleteReservation(21).getOrThrow()

        assertTrue(scheduled)
        assertEquals(0, service.deleteReservationCalls)
        assertEquals(SyncStatus.PENDING_DELETE, dao.getById(21)?.syncStatus)
    }

    @Test
    fun deleteReservation_removesPendingCreateItemLocally() = runTest {
        val dao = FakeReservationDao(
            initialReservations = listOf(
                reservationRoomEntity(
                    id = -9,
                    syncStatus = SyncStatus.PENDING_CREATE,
                ),
            ),
        )
        val service = FakeReservationApiService()
        var scheduled = false
        val repository = ReservationRepositoryImpl(
            api = service,
            reservationDao = dao,
            syncPreferenceStore = FakeReservationSyncPreferenceStore(),
            context = ContextWrapper(null),
            syncScheduler = { scheduled = true },
        )

        repository.deleteReservation(-9).getOrThrow()

        assertNull(dao.getById(-9))
        assertEquals(0, service.deleteReservationCalls)
        assertTrue(!scheduled)
    }
}

private class FakeReservationDao(
    initialReservations: List<ReservationRoomEntity> = emptyList(),
) : ReservationDao {
    private val store = MutableStateFlow(initialReservations.associateBy { it.id })

    override fun observeByFestival(festivalId: Int): Flow<List<ReservationRoomEntity>> =
        store.map { reservations ->
            reservations.values.filter { it.festivalId == festivalId }.sortedBy { it.reservantName }
        }

    override fun observeById(id: Int): Flow<ReservationRoomEntity?> = store.map { it[id] }

    override suspend fun getById(id: Int): ReservationRoomEntity? = store.value[id]

    override suspend fun getPending(): List<ReservationRoomEntity> = emptyList()

    override suspend fun upsertAll(reservations: List<ReservationRoomEntity>) {
        store.value = store.value + reservations.associateBy { it.id }
    }

    override suspend fun upsert(reservation: ReservationRoomEntity) {
        store.value = store.value + (reservation.id to reservation)
    }

    override suspend fun deleteById(id: Int) {
        store.value = store.value - id
    }

    override suspend fun markForDeletion(id: Int) {
        val reservation = store.value[id] ?: return
        store.value = store.value + (id to reservation.copy(syncStatus = SyncStatus.PENDING_DELETE))
    }

    override suspend fun updateSyncStatus(id: Int, status: String) {
        val reservation = store.value[id] ?: return
        store.value = store.value + (id to reservation.copy(syncStatus = status))
    }
}

private class FakeReservationSyncPreferenceStore : SyncPreferenceStore(ContextWrapper(null)) {
    override suspend fun getLastSyncedAt(key: String): Long? = null
    override suspend fun setLastSyncedAt(key: String, timestamp: Long) {}
    override suspend fun needsRefresh(key: String, ttlMs: Long): Boolean = true
    override suspend fun invalidate(key: String) {}
}

private class FakeReservationApiService : ReservationApiService {
    var deleteReservationCalls: Int = 0

    override suspend fun getReservationsByFestival(festivalId: Int): List<ReservationDashboardRowDto> =
        emptyList()

    override suspend fun createReservation(payload: ReservationCreatePayloadDto) = Unit

    override suspend fun deleteReservation(id: Int) {
        deleteReservationCalls += 1
    }

    override suspend fun getWorkflowByReservationId(reservationId: Int): WorkflowDto =
        WorkflowDto(id = 1, state = "new", contact_dates = emptyList())

    override suspend fun updateWorkflow(id: Int, workflowData: WorkflowUpdatePayload): WorkflowDto =
        WorkflowDto(id = id, state = workflowData.state, contact_dates = emptyList())

    override suspend fun addContactDate(id: Int): List<String> = emptyList()

    override suspend fun getReservationDetails(reservationId: Int): ReservationDetailsDto =
        ReservationDetailsDto(
            id = reservationId,
            festivalId = 1,
            startPrice = 0.0,
            nbPrises = 0,
            finalPrice = 0.0,
            zonesTarifaires = emptyList(),
        )

    override suspend fun updateReservation(id: Int, payload: ReservationUpdatePayloadDto) = Unit

    override suspend fun getZonesTarifaires(festivalId: Int): List<ZoneTarifaireDto> = emptyList()
}

private fun reservationRoomEntity(
    id: Int,
    syncStatus: String = SyncStatus.SYNCED,
) = ReservationRoomEntity(
    id = id,
    festivalId = 1,
    reservantName = "Blue Fox",
    reservantType = "editeur",
    workflowState = "new",
    syncStatus = syncStatus,
    pendingDraftJson = null,
)
