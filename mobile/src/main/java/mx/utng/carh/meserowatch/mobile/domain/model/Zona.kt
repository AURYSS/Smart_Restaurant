package mx.utng.carh.meserowatch.mobile.domain.model

data class Zona(
    val id: String = "",
    val nombreZona: String = "",
    val estadoZona: EstadoZona = EstadoZona.DISPONIBLE
)