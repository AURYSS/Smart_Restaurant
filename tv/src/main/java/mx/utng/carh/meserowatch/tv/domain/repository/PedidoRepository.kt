package mx.utng.carh.meserowatch.tv.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.utng.carh.meserowatch.tv.domain.model.EstadoPedido
import mx.utng.carh.meserowatch.tv.domain.model.Pedido

interface PedidoRepository {
    fun observarPedidos(): Flow<List<Pedido>>
    suspend fun actualizarEstado(id: String, nuevoEstado: EstadoPedido)
    suspend fun eliminarPedido(id: String)
}