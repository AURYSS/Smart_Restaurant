package mx.utng.carh.meserowatch.presentation

data class Pedido(
    val id: String = "",
    val mesa: Int = 0,
    val descripcion: String = "",
    val estado: EstadoPedido = EstadoPedido.EN_PREPARACION
)

enum class EstadoPedido {
    LISTO, EN_PREPARACION
}