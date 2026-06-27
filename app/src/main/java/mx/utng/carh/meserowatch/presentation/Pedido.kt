package mx.utng.carh.meserowatch.presentation

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Pedido(
    val id: String = "",
    val mesa: Int = 0,
    val descripcion: String = "",
    val nota: String = "",
    val estado: EstadoPedido = EstadoPedido.PENDIENTE,
    val timestamp: Long = 0
)

enum class EstadoPedido {
    PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO
}
