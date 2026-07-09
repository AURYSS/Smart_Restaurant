package mx.utng.carh.meserowatch.mobile.data.repository

import kotlinx.coroutines.flow.Flow
import mx.utng.carh.meserowatch.mobile.data.source.PedidoDataSource
import mx.utng.carh.meserowatch.mobile.domain.model.EstadoPedido
import mx.utng.carh.meserowatch.mobile.domain.model.Pedido
import mx.utng.carh.meserowatch.mobile.domain.repository.PedidoRepository

class PedidoRepositoryImpl(private val dataSource: PedidoDataSource) : PedidoRepository {
    override fun getPedidos(): Flow<List<Pedido>> = dataSource.getPedidos()

    override suspend fun addPedido(pedido: Pedido, items: List<Map<String, String>>) =
        dataSource.addPedido(pedido, items)

    override suspend fun updateEstado(pedidoId: String, nuevoEstado: EstadoPedido) =
        dataSource.updateEstado(pedidoId, nuevoEstado)

    override suspend fun deletePedido(pedidoId: String) = dataSource.deletePedido(pedidoId)
}