package mx.utng.carh.meserowatch.mobile.domain.model

data class Mesa(
    val id: Int = 0,
    val numero: Int = 0,
    val estado: EstadoMesa = EstadoMesa.LIBRE,
    val capacidad: Int = 4,
    val meseroAsignado: String = "",
    val zonaId: String = ""
)