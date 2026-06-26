package mx.utng.carh.meserowatch.tv

enum class EstadoPedido {
    EN_PREPARACION, LISTO, ENTREGADO
}

data class PlatilloSeleccionado(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1
)

data class Pedido(
    val id: String = "",
    val mesa: Int = 0,
    val descripcion: String = "",
    val nota: String = "",
    val estado: EstadoPedido = EstadoPedido.EN_PREPARACION,
    val timestamp: Long = 0,
    val platillos: List<PlatilloSeleccionado> = emptyList()
)
