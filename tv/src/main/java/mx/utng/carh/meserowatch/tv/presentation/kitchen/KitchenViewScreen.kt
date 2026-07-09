package mx.utng.carh.meserowatch.tv.presentation.kitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.carh.meserowatch.tv.domain.model.EstadoPedido
import mx.utng.carh.meserowatch.tv.domain.model.Pedido
import mx.utng.carh.meserowatch.tv.domain.usecase.ActualizarEstadoPedidoUseCase
import mx.utng.carh.meserowatch.tv.domain.usecase.EliminarPedidoUseCase
import mx.utng.carh.meserowatch.tv.domain.usecase.ObservarPedidosUseCase

class KitchenViewModel(
    private val observarPedidos: ObservarPedidosUseCase,
    private val actualizarEstado: ActualizarEstadoPedidoUseCase,
    private val eliminarPedido: EliminarPedidoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(KitchenUiState())
    val uiState: StateFlow<KitchenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observarPedidos().collect { pedidos ->
                _uiState.update { it.copy(pedidos = pedidos, cargando = false) }
            }
        }
    }

    fun seleccionarPedido(pedido: Pedido, lista: List<Pedido>) {
        _uiState.update {
            it.copy(
                pedidoSeleccionado = pedido,
                listaSeleccionada = lista,
                indiceSeleccionado = lista.indexOf(pedido)
            )
        }
    }

    fun irAnterior() {
        _uiState.update { state ->
            val nuevoIndice = state.indiceSeleccionado - 1
            if (nuevoIndice >= 0) {
                state.copy(
                    pedidoSeleccionado = state.listaSeleccionada[nuevoIndice],
                    indiceSeleccionado = nuevoIndice
                )
            } else state
        }
    }

    fun irSiguiente() {
        _uiState.update { state ->
            val nuevoIndice = state.indiceSeleccionado + 1
            if (nuevoIndice < state.listaSeleccionada.size) {
                state.copy(
                    pedidoSeleccionado = state.listaSeleccionada[nuevoIndice],
                    indiceSeleccionado = nuevoIndice
                )
            } else state
        }
    }

    fun volver() {
        _uiState.update { it.copy(pedidoSeleccionado = null) }
    }

    fun completarPedido(pedido: Pedido) {
        viewModelScope.launch {
            actualizarEstado(pedido.id, EstadoPedido.LISTO)
            // Al cambiar en Firebase, el Flow se actualizará automáticamente
            // Volvemos a la pantalla principal
            volver()
        }
    }

    fun eliminarPedido(pedido: Pedido) {
        viewModelScope.launch {
            eliminarPedido(pedido.id)
            volver()
        }
    }

    // Factory para poder instanciar el ViewModel pasando los casos de uso
    class Factory(
        private val observarPedidos: ObservarPedidosUseCase,
        private val actualizarEstado: ActualizarEstadoPedidoUseCase,
        private val eliminarPedido: EliminarPedidoUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(KitchenViewModel::class.java)) {
                return KitchenViewModel(observarPedidos, actualizarEstado, eliminarPedido) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class KitchenUiState(
    val pedidos: List<Pedido> = emptyList(),
    val pedidoSeleccionado: Pedido? = null,
    val listaSeleccionada: List<Pedido> = emptyList(),
    val indiceSeleccionado: Int = 0,
    val cargando: Boolean = true
)