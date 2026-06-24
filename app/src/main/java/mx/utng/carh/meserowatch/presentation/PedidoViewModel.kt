package mx.utng.carh.meserowatch.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PedidoViewModel : ViewModel() {

    private val _pedidos = MutableStateFlow(
        listOf(
            Pedido(1, 4, "Carne asada · Agua", EstadoPedido.LISTO),
            Pedido(2, 7, "Tacos x3 · Refresco", EstadoPedido.LISTO),
            Pedido(3, 2, "En preparacion...", EstadoPedido.EN_PREPARACION)
        )
    )
    val pedidos: StateFlow<List<Pedido>> = _pedidos.asStateFlow()

    val pedidoActual: Pedido?
        get() = _pedidos.value.firstOrNull { it.estado == EstadoPedido.LISTO }

    fun confirmarEntrega(id: Int) {
        _pedidos.value = _pedidos.value.filter { it.id != id }
    }

    fun posponerPedido(id: Int) {
        val lista = _pedidos.value.toMutableList()
        val idx = lista.indexOfFirst { it.id == id }
        if (idx != -1) {
            val pedido = lista.removeAt(idx)
            lista.add(pedido)
            _pedidos.value = lista
        }
    }
}