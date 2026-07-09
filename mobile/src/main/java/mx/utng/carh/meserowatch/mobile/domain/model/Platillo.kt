package mx.utng.carh.meserowatch.mobile.domain.model

data class Platillo(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val disponible: Boolean = true,
    val ingredientes: List<String> = emptyList(),
    val emoji: String = "🍽️"
)