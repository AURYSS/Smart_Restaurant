package mx.utng.carh.meserowatch.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import mx.utng.carh.meserowatch.mobile.domain.model.Pedido
import mx.utng.carh.meserowatch.mobile.domain.repository.PedidoRepository
import mx.utng.carh.meserowatch.mobile.presentation.di.AppModule

data class HistorialPedidosState(
    val pedidos: List<Pedido> = emptyList(),
    val searchQuery: String = "",
    val filteredPedidos: List<Pedido> = emptyList()
)

class HistorialPedidosViewModel : ViewModel() {
    private val repo = AppModule.pedidoRepository

    private val _state = MutableStateFlow(HistorialPedidosState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.getPedidos(),
                _state.map { it.searchQuery }
            ) { pedidos, query ->
                val reversed = pedidos.reversed()
                if (query.isEmpty()) reversed
                else reversed.filter {
                    it.mesa.toString().contains(query) ||
                            it.descripcion.contains(query, ignoreCase = true)
                }
            }.collect { filtered ->
                _state.value = _state.value.copy(filteredPedidos = filtered)
            }
        }
    }

    fun onSearchChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }
}