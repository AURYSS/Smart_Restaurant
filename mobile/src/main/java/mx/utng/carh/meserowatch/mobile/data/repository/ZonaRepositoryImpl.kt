package mx.utng.carh.meserowatch.mobile.data.repository

import kotlinx.coroutines.flow.Flow
import mx.utng.carh.meserowatch.mobile.data.source.ZonaDataSource
import mx.utng.carh.meserowatch.mobile.domain.model.Zona
import mx.utng.carh.meserowatch.mobile.domain.repository.ZonaRepository

class ZonaRepositoryImpl(private val dataSource: ZonaDataSource) : ZonaRepository {
    override fun getZonas(): Flow<List<Zona>> = dataSource.getZonas()
    override suspend fun addZona(zona: Zona) = dataSource.addZona(zona)
    override suspend fun updateZona(zona: Zona) = dataSource.updateZona(zona)
    override suspend fun deleteZona(id: String) = dataSource.deleteZona(id)
}