package mx.utng.carh.meserowatch.tv

enum class EstadoPedido {
    PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO
}

data class PlatilloSeleccionado(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1
)

data class ItemPedido(
    val descripcion: String = "",
    val nota: String = ""
)

data class Pedido(
    val id: String = "",
    val mesa: Int = 0,
    val descripcion: String = "",
    val nota: String = "",
    val estado: EstadoPedido = EstadoPedido.EN_PREPARACION,
    val timestamp: Long = 0,
    val imagenUrl: String = "",
    val items: List<ItemPedido> = emptyList(),
    val platillos: List<PlatilloSeleccionado> = emptyList()
)
