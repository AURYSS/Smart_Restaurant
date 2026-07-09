package mx.utng.carh.meserowatch.mobile.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.utng.carh.meserowatch.mobile.domain.model.EstadoPedido
import mx.utng.carh.meserowatch.mobile.domain.model.Pedido

interface PedidoRepository {
    fun getPedidos(): Flow<List<Pedido>>
    suspend fun addPedido(pedido: Pedido, items: List<Map<String, String>>)
    suspend fun updateEstado(pedidoId: String, nuevoEstado: EstadoPedido)
    suspend fun deletePedido(pedidoId: String)
}