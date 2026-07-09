package mx.utng.carh.meserowatch.tv.domain.usecase

import kotlinx.coroutines.flow.Flow
import mx.utng.carh.meserowatch.tv.domain.model.Pedido
import mx.utng.carh.meserowatch.tv.domain.repository.PedidoRepository

class ObservarPedidosUseCase(private val repository: PedidoRepository) {
    operator fun invoke(): Flow<List<Pedido>> = repository.observarPedidos()
}