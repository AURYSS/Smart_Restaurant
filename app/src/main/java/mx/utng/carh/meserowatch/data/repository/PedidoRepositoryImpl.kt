package mx.utng.carh.meserowatch.data.repository

import kotlinx.coroutines.flow.Flow
import mx.utng.carh.meserowatch.data.source.PedidoDataSource
import mx.utng.carh.meserowatch.domain.model.Pedido
import mx.utng.carh.meserowatch.domain.repository.PedidoRepository

class PedidoRepositoryImpl(private val dataSource: PedidoDataSource) : PedidoRepository {

    override fun getPedidos(): Flow<List<Pedido>> = dataSource.observePedidos()

    override suspend fun confirmarEntrega(id: String) {
        dataSource.confirmarEntrega(id)
    }

    override suspend fun posponerPedido(id: String) {
        dataSource.posponerPedido(id)
    }

    override suspend fun completarEntrega(id: String) {
        dataSource.completarEntrega(id)
    }
}