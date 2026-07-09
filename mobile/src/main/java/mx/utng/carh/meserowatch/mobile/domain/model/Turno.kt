package mx.utng.carh.meserowatch.mobile.domain.model

data class Turno(
    val id: String = "",
    val horaInicio: Long = 0,
    val horaFin: Long? = null,
    val usuarioId: String = ""
)