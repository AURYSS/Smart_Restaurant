package mx.utng.carh.meserowatch.domain.model

data class Pedido(
    val id: String = "",
    val mesa: Int = 0,
    val descripcion: String = "",
    val nota: String = "",
    val estado: EstadoPedido = EstadoPedido.PENDIENTE,
    val timestamp: Long = 0
)