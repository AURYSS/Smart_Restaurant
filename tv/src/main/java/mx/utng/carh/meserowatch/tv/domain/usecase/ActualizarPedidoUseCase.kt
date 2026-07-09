package mx.utng.carh.meserowatch.tv.domain.usecase

import mx.utng.carh.meserowatch.tv.domain.model.EstadoPedido
import mx.utng.carh.meserowatch.tv.domain.repository.PedidoRepository

class ActualizarEstadoPedidoUseCase(private val repository: PedidoRepository) {
    suspend operator fun invoke(id: String, nuevoEstado: EstadoPedido) {
        repository.actualizarEstado(id, nuevoEstado)
    }
}