package mx.utng.carh.meserowatch.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.carh.meserowatch.mobile.presentation.di.AppModule

data class AdminDashboardState(
    val ventasHoy: Double = 0.0,
    val totalPedidos: Int = 0,
    val pedidosEnCurso: Int = 0,
    val personalActivo: Int = 0,
    val mesasOcupadas: Int = 0,
    val mesasTotales: Int = 12
)

class AdminDashboardViewModel : ViewModel() {
    private val pedidoRepo = AppModule.pedidoRepository
    private val usuarioRepo = AppModule.usuarioRepository
    private val mesaRepo = AppModule.mesaRepository

    private val _state = MutableStateFlow(AdminDashboardState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                pedidoRepo.getPedidos(),
                usuarioRepo.getUsuarios(),
                mesaRepo.getTodasLasMesas()
            ) { pedidos, usuarios, mesas ->
                val enCurso = pedidos.count { it.estado != mx.utng.carh.meserowatch.mobile.domain.model.EstadoPedido.ENTREGADO && it.estado != mx.utng.carh.meserowatch.mobile.domain.model.EstadoPedido.CANCELADO }
                val entregados = pedidos.filter { it.estado == mx.utng.carh.meserowatch.mobile.domain.model.EstadoPedido.ENTREGADO }
                val ventas = entregados.sumOf { it.total }
                val activos = usuarios.count { it.activo }
                val ocupadas = mesas.count { it.estado == mx.utng.carh.meserowatch.mobile.domain.model.EstadoMesa.OCUPADA }
                AdminDashboardState(
                    ventasHoy = ventas,
                    totalPedidos = pedidos.size,
                    pedidosEnCurso = enCurso,
                    personalActivo = activos,
                    mesasOcupadas = ocupadas,
                    mesasTotales = mesas.size
                )
            }.collect { _state.value = it }
        }
    }
}