package mx.utng.carh.meserowatch.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.carh.meserowatch.mobile.domain.model.Pedido
import mx.utng.carh.meserowatch.mobile.domain.repository.PedidoRepository
import mx.utng.carh.meserowatch.mobile.presentation.di.AppModule

data class AlertasState(
    val pedidos: List<Pedido> = emptyList(),
    val filtro: String = "Todos",
    val pedidoDetalle: Pedido? = null
)

class AlertasViewModel : ViewModel() {
    private val repo = AppModule.pedidoRepository

    private val _state = MutableStateFlow(AlertasState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getPedidos().collect { lista ->
                _state.update { it.copy(pedidos = lista) }
            }
        }
    }

    fun setFiltro(filtro: String) { _state.update { it.copy(filtro = filtro) } }
    fun verDetalle(pedido: Pedido) { _state.update { it.copy(pedidoDetalle = pedido) } }
    fun cerrarDetalle() { _state.update { it.copy(pedidoDetalle = null) } }
}