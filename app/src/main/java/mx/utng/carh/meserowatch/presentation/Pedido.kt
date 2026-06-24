package mx.utng.carh.meserowatch.presentation

data class Pedido(
    val id: Int,
    val mesa: Int,
    val descripcion: String,
    val estado: EstadoPedido
)

enum class EstadoPedido {
    LISTO, EN_PREPARACION
}