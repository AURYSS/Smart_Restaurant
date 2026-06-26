package mx.utng.carh.meserowatch.presentation

data class Pedido(
    val id: String = "",
    val mesa: Int = 0,
    val descripcion: String = "",
    val nota: String = "",
    val estado: EstadoPedido = EstadoPedido.EN_PREPARACION,
    val timestamp: Long = 0
)

enum class EstadoPedido {
    LISTO, EN_PREPARACION
}