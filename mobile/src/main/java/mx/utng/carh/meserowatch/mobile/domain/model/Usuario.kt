package mx.utng.carh.meserowatch.mobile.domain.model

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val rol: RolUsuario = RolUsuario.MESERO,
    val activo: Boolean = true,
    val estadoUsuario: EstadoUsuario = EstadoUsuario.ACTIVO,
    val zonaId: String = "",
    val zonaAsignada: String = "",
    val fotoEmoji: String = "👤"
)