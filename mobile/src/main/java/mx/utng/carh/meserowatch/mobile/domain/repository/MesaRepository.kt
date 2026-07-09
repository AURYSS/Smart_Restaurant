package mx.utng.carh.meserowatch.mobile.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.utng.carh.meserowatch.mobile.domain.model.Mesa

interface MesaRepository {
    /**
     * Obtiene todas las mesas (1..12 + configuradas) con su estado actual
     */
    fun getTodasLasMesas(): Flow<List<Mesa>>
    suspend fun addMesa(mesa: Mesa)
    suspend fun updateMesa(mesa: Mesa)
    suspend fun deleteMesa(id: Int)
}