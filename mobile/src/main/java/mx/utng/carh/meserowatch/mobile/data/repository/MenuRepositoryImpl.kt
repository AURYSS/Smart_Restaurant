package mx.utng.carh.meserowatch.mobile.data.repository

import kotlinx.coroutines.flow.Flow
import mx.utng.carh.meserowatch.mobile.data.source.MenuDataSource
import mx.utng.carh.meserowatch.mobile.domain.model.Platillo
import mx.utng.carh.meserowatch.mobile.domain.repository.MenuRepository

class MenuRepositoryImpl(private val dataSource: MenuDataSource) : MenuRepository {
    override fun getMenu(): Flow<List<Platillo>> = dataSource.getMenu()

    override suspend fun addPlatillo(platillo: Platillo) = dataSource.addPlatillo(platillo)
    override suspend fun updatePlatillo(platillo: Platillo) = dataSource.updatePlatillo(platillo)
    override suspend fun deletePlatillo(id: String) = dataSource.deletePlatillo(id)
}