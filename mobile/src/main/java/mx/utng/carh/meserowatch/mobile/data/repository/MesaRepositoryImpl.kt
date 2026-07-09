package mx.utng.carh.meserowatch.mobile.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import mx.utng.carh.meserowatch.mobile.data.source.MesaDataSource
import mx.utng.carh.meserowatch.mobile.domain.model.EstadoMesa
import mx.utng.carh.meserowatch.mobile.domain.model.Mesa
import mx.utng.carh.meserowatch.mobile.domain.repository.MesaRepository

class MesaRepositoryImpl(private val dataSource: MesaDataSource) : MesaRepository {

    override fun getTodasLasMesas(): Flow<List<Mesa>> {
        return combine(
            dataSource.getMesasConfig(),
            dataSource.getMesasOcupadas()
        ) { mesasConfig, ocupadas ->
            // Mesas base del 1 al 12
            val idsConfig = mesasConfig.map { it.id }.toSet()
            val mesasBase = (1..12)
                .filter { it !in idsConfig }
                .map { Mesa(id = it, numero = it, capacidad = 4) }

            // Combinamos
            val todas = (mesasBase + mesasConfig).sortedBy { it.id }.map { mesa ->
                val ocupada = mesa.id in ocupadas
                mesa.copy(estado = if (ocupada) EstadoMesa.OCUPADA else EstadoMesa.LIBRE)
            }
            todas
        }
    }

    override suspend fun addMesa(mesa: Mesa) = dataSource.addMesa(mesa)
    override suspend fun updateMesa(mesa: Mesa) = dataSource.updateMesa(mesa)
    override suspend fun deleteMesa(id: Int) = dataSource.deleteMesa(id)
}