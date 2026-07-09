package mx.utng.carh.meserowatch.mobile.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.utng.carh.meserowatch.mobile.domain.model.Platillo

interface MenuRepository {
    fun getMenu(): Flow<List<Platillo>>
    suspend fun addPlatillo(platillo: Platillo)
    suspend fun updatePlatillo(platillo: Platillo)
    suspend fun deletePlatillo(id: String)
}