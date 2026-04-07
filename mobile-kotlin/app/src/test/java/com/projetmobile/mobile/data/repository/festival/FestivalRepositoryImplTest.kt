package com.projetmobile.mobile.data.repository.festival

import android.content.ContextWrapper
import com.projetmobile.mobile.data.dao.FestivalDao
import com.projetmobile.mobile.data.database.SyncPreferenceStore
import com.projetmobile.mobile.data.remote.festival.CreateFestivalResponseDto
import com.projetmobile.mobile.data.remote.festival.FestivalApiService
import com.projetmobile.mobile.data.remote.festival.FestivalDto
import com.projetmobile.mobile.data.room.FestivalRoomEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FestivalRepositoryImplTest {

    @Test
    fun addFestival_fetchesCanonicalFestivalBeforeCaching() = runTest {
        val canonicalFestival = festivalDto(
            id = 18,
            name = "Festival du Jeu",
            stockTablesStandard = 24,
            stockChaises = 120,
        )
        val service = FakeFestivalApiService(
            addResponse = CreateFestivalResponseDto(
                festival = festivalDto(
                    id = 18,
                    name = "Festival du Jeu",
                    stockTablesStandard = 0,
                    stockChaises = 0,
                ),
            ),
            festivalById = canonicalFestival,
        )
        val dao = FakeFestivalDao()
        val repository = FestivalRepositoryImpl(
            festivalApiService = service,
            festivalDao = dao,
            syncPreferenceStore = FakeFestivalSyncPreferenceStore(),
        )

        val created = repository.addFestival(
            festivalDto(
                name = "Festival du Jeu",
                startDate = "2026-05-01",
                endDate = "2026-05-03",
            ),
        ).getOrThrow()

        assertEquals(18, service.lastRequestedFestivalId)
        assertEquals(24, created.stockTablesStandard)
        assertNotNull(dao.getById(18))
        assertEquals(120, dao.getById(18)?.stockChaises)
    }
}

private class FakeFestivalDao : FestivalDao {
    private val store = MutableStateFlow<Map<Int, FestivalRoomEntity>>(emptyMap())

    override fun observeAll(): Flow<List<FestivalRoomEntity>> =
        store.map { festivals -> festivals.values.sortedByDescending { it.startDate } }

    override fun observeById(id: Int): Flow<FestivalRoomEntity?> = store.map { it[id] }

    override suspend fun upsertAll(festivals: List<FestivalRoomEntity>) {
        store.value = store.value + festivals.associateBy { it.id }
    }

    override suspend fun upsert(festival: FestivalRoomEntity) {
        store.value = store.value + (festival.id to festival)
    }

    override suspend fun deleteById(id: Int) {
        store.value = store.value - id
    }

    fun getById(id: Int): FestivalRoomEntity? = store.value[id]
}

private class FakeFestivalSyncPreferenceStore : SyncPreferenceStore(ContextWrapper(null)) {
    override suspend fun getLastSyncedAt(key: String): Long? = null
    override suspend fun setLastSyncedAt(key: String, timestamp: Long) {}
    override suspend fun needsRefresh(key: String, ttlMs: Long): Boolean = true
    override suspend fun invalidate(key: String) {}
}

private class FakeFestivalApiService(
    private val addResponse: CreateFestivalResponseDto,
    private val festivalById: FestivalDto,
) : FestivalApiService {
    var lastRequestedFestivalId: Int? = null

    override suspend fun getFestivals(): List<FestivalDto> = listOf(festivalById)

    override suspend fun getFestival(id: Int): FestivalDto {
        lastRequestedFestivalId = id
        return festivalById
    }

    override suspend fun addFestival(festival: FestivalDto): CreateFestivalResponseDto = addResponse

    override suspend fun deleteFestival(id: Int) = Unit
}

private fun festivalDto(
    id: Int? = null,
    name: String,
    startDate: String = "2026-05-01",
    endDate: String = "2026-05-03",
    stockTablesStandard: Int = 12,
    stockChaises: Int = 80,
) = FestivalDto(
    id = id,
    name = name,
    startDate = startDate,
    endDate = endDate,
    stockTablesStandard = stockTablesStandard,
    stockTablesGrande = 4,
    stockTablesMairie = 2,
    stockChaises = stockChaises,
    prixPrises = 15.0,
    zonesTarifaires = emptyList(),
)
