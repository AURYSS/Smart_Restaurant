package mx.utng.carh.meserowatch.mobile.domain.model

data class Pedido(
    val id: String = "",
    val mesa: Int = 0,
    val mesaId: Int = 0,
    val meseroId: String = "",
    val descripcion: String = "",
    val nota: String = "",
    val estado: EstadoPedido = EstadoPedido.PENDIENTE,
    val total: Double = 0.0,
    val timestamp: Long = 0,
    val platillos: List<PlatilloSeleccionado> = emptyList(),
    val usuarioId: String = ""
)

data class PlatilloSeleccionado(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1,
    val nota: String = ""
)