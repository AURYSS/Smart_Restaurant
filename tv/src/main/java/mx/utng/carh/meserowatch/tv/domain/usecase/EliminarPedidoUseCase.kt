package mx.utng.carh.meserowatch.tv.domain.usecase

import mx.utng.carh.meserowatch.tv.domain.repository.PedidoRepository

class EliminarPedidoUseCase(private val repository: PedidoRepository) {
    suspend operator fun invoke(id: String) {
        repository.eliminarPedido(id)
    }
}