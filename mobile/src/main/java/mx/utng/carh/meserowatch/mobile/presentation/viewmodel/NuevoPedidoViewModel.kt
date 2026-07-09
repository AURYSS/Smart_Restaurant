package mx.utng.carh.meserowatch.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.carh.meserowatch.mobile.SessionManager
import mx.utng.carh.meserowatch.mobile.domain.model.*
import mx.utng.carh.meserowatch.mobile.domain.repository.MenuRepository
import mx.utng.carh.meserowatch.mobile.domain.repository.MesaRepository
import mx.utng.carh.meserowatch.mobile.domain.repository.PedidoRepository
import mx.utng.carh.meserowatch.mobile.presentation.di.AppModule

data class NuevoPedidoState(
    val mesas: List<Mesa> = emptyList(),
    val searchMesa: String = "",
    val filterMesa: String = "Todas",
    val mesaSeleccionada: Int? = null,
    val platillos: List<Platillo> = emptyList(),
    val searchPlatillo: String = "",
    val filterCategoria: String = "Todos",
    val seleccionados: Map<String, Int> = emptyMap(),
    val mostrandoResumen: Boolean = false,
    val pedidoEnviado: Boolean = false
)

class NuevoPedidoViewModel : ViewModel() {
    private val mesaRepo = AppModule.mesaRepository
    private val menuRepo = AppModule.menuRepository
    private val pedidoRepo = AppModule.pedidoRepository

    private val _state = MutableStateFlow(NuevoPedidoState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            mesaRepo.getTodasLasMesas().collect { mesas ->
                _state.update { it.copy(mesas = mesas) }
            }
        }
    }

    fun onSearchMesaChanged(q: String) { _state.update { it.copy(searchMesa = q) } }
    fun onFilterMesaChanged(f: String) { _state.update { it.copy(filterMesa = f) } }

    fun seleccionarMesa(id: Int) {
        _state.update { it.copy(mesaSeleccionada = id) }
        // Cargar platillos cuando se selecciona mesa
        viewModelScope.launch {
            menuRepo.getMenu().collect { lista ->
                _state.update { it.copy(platillos = lista) }
            }
        }
    }

    fun volverAMesas() { _state.update { it.copy(mesaSeleccionada = null, mostrandoResumen = false) } }

    fun onSearchPlatilloChanged(q: String) { _state.update { it.copy(searchPlatillo = q) } }
    fun onCategoriaChanged(cat: String) { _state.update { it.copy(filterCategoria = cat) } }

    fun togglePlatillo(platilloId: String) {
        _state.update { current ->
            val map = current.seleccionados.toMutableMap()
            if (map.containsKey(platilloId)) map.remove(platilloId) else map[platilloId] = 1
            current.copy(seleccionados = map)
        }
    }

    fun setCantidad(platilloId: String, cantidad: Int) {
        if (cantidad <= 0) {
            _state.update { it.copy(seleccionados = it.seleccionados - platilloId) }
        } else {
            _state.update { it.copy(seleccionados = it.seleccionados + (platilloId to cantidad)) }
        }
    }

    fun irAResumen() { _state.update { it.copy(mostrandoResumen = true) } }
    fun volverAPlatillos() { _state.update { it.copy(mostrandoResumen = false) } }

    fun enviarPedido(
        mesaId: Int,
        platillosConNotas: List<PlatilloSeleccionado>,
        itemsFirebase: List<Map<String, String>>,
        total: Double,
        descripcion: String
    ) {
        viewModelScope.launch {
            val pedido = Pedido(
                mesa = mesaId,
                descripcion = descripcion,
                total = total,
                estado = EstadoPedido.EN_PREPARACION,
                platillos = platillosConNotas,
                usuarioId = SessionManager.currentUser?.id ?: ""
            )
            pedidoRepo.addPedido(pedido, itemsFirebase)
            _state.update { it.copy(pedidoEnviado = true) }
        }
    }
}