package mx.utng.carh.meserowatch.mobile

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Pedido(
    val id: String = "",
    val mesa: Int = 0,
    val mesaId: Int = 0, // Según diagrama
    val meseroId: String = "", // Según diagrama
    val descripcion: String = "",
    val nota: String = "",
    val estado: EstadoPedido = EstadoPedido.PENDIENTE,
    val total: Double = 0.0,
    val timestamp: Long = 0,
    val platillos: List<PlatilloSeleccionado> = emptyList(),
    val usuarioId: String = "" // Quién lo registró
)

enum class EstadoPedido {
    PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO
}

data class PlatilloSeleccionado(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1,
    val nota: String = ""
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
    val numero: Int = 0,
    val estado: EstadoMesa = EstadoMesa.LIBRE,
    val capacidad: Int = 4,
    val meseroAsignado: String = "",
    val zonaId: String = "" // Vinculado a Zona
)

enum class EstadoMesa {
    LIBRE, OCUPADA, RESERVADA, FUERA_DE_SERVICIO
}

data class Zona(
    val id: String = "",
    val nombreZona: String = "",
    val estadoZona: EstadoZona = EstadoZona.DISPONIBLE
)

enum class EstadoZona {
    DISPONIBLE, NO_DISPONIBLE
}

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val rol: RolUsuario = RolUsuario.MESERO,
    val activo: Boolean = true,
    val estadoUsuario: EstadoUsuario = EstadoUsuario.ACTIVO,
    val zonaId: String = "",
    val zonaAsignada: String = "", // Para retrocompatibilidad o visualización rápida
    val fotoEmoji: String = "👤"
)

enum class RolUsuario {
    MESERO, CHEF, CAJERO, ADMIN
}

enum class EstadoUsuario {
    ACTIVO, INACTIVO, EN_DESCANSO
}

data class Turno(
    val id: String = "",
    val horaInicio: Long = 0,
    val horaFin: Long? = null,
    val usuarioId: String = ""
)

data class Notificacion(
    val id: String = "",
    val pedidoId: String = "",
    val usuarioId: String = "",
    val mensaje: String = "",
    val confirmada: Boolean = false
)
