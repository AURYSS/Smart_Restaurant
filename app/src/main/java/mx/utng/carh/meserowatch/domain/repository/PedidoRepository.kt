package mx.utng.carh.meserowatch.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.utng.carh.meserowatch.domain.model.Pedido

interface PedidoRepository {
    fun getPedidos(): Flow<List<Pedido>>
    suspend fun confirmarEntrega(id: String)
    suspend fun posponerPedido(id: String)
    suspend fun completarEntrega(id: String)
}