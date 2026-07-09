package mx.utng.carh.meserowatch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.utng.carh.meserowatch.data.repository.PedidoRepositoryImpl
import mx.utng.carh.meserowatch.data.source.PedidoDataSource
import mx.utng.carh.meserowatch.domain.model.EstadoPedido
import mx.utng.carh.meserowatch.domain.model.Pedido
import mx.utng.carh.meserowatch.domain.repository.PedidoRepository

class PedidoViewModel : ViewModel() {

    private val repository: PedidoRepository = PedidoRepositoryImpl(PedidoDataSource())

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos.asStateFlow()

    val pedidoActual: Pedido?
        get() = _pedidos.value.firstOrNull { it.estado == EstadoPedido.LISTO }

    init {
        viewModelScope.launch {
            repository.getPedidos().collect { lista ->
                _pedidos.value = lista
            }
        }
    }

    fun confirmarEntrega(id: String) {
        viewModelScope.launch {
            repository.confirmarEntrega(id)
        }
    }

    fun posponerPedido(id: String) {
        viewModelScope.launch {
            repository.posponerPedido(id)
        }
    }

    fun completarEntrega(id: String) {
        viewModelScope.launch {
            repository.completarEntrega(id)
        }
    }
}