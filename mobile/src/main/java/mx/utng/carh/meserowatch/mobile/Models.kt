package mx.utng.carh.meserowatch.mobile

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Pedido(
    val id: String = "",
    val mesa: Int = 0,
    val descripcion: String = "",
    val nota: String = "",
    val estado: EstadoPedido = EstadoPedido.EN_PREPARACION,
    val timestamp: Long = 0,
    val platillos: List<PlatilloSeleccionado> = emptyList()
)

enum class EstadoPedido {
    EN_PREPARACION, LISTO, ENTREGADO
}

data class PlatilloSeleccionado(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1
)

data class Platillo(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val disponible: Boolean = true,
    val ingredientes: List<String> = emptyList(),
    val emoji: String = "🍽️"
)

data class Mesa(
    val id: Int = 0,
    val estado: EstadoMesa = EstadoMesa.LIBRE,
    val capacidad: Int = 4,
    val meseroAsignado: String = "",
    val zona: String = "A"
)

enum class EstadoMesa {
    LIBRE, OCUPADA, RESERVADA, FUERA_DE_SERVICIO
}

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val rol: RolUsuario = RolUsuario.MESERO,
    val activo: Boolean = true,
    val zonaAsignada: String = "A",
    val fotoEmoji: String = "👤"
)

enum class RolUsuario {
    MESERO, CHEF, CAJERO, ADMIN
}
