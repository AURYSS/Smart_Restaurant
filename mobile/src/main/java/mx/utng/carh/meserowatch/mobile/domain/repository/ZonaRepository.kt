package mx.utng.carh.meserowatch.mobile.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.utng.carh.meserowatch.mobile.domain.model.Zona

interface ZonaRepository {
    fun getZonas(): Flow<List<Zona>>
    suspend fun addZona(zona: Zona)
    suspend fun updateZona(zona: Zona)
    suspend fun deleteZona(id: String)
}