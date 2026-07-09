package mx.utng.carh.meserowatch.mobile.domain.model

data class Notificacion(
    val id: String = "",
    val pedidoId: String = "",
    val usuarioId: String = "",
    val mensaje: String = "",
    val confirmada: Boolean = false
)