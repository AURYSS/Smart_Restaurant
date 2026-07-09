package mx.utng.carh.meserowatch.mobile.domain.model

enum class EstadoPedido {
    PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO
}

enum class EstadoMesa {
    LIBRE, OCUPADA, RESERVADA, FUERA_DE_SERVICIO
}

enum class EstadoZona {
    DISPONIBLE, NO_DISPONIBLE
}

enum class RolUsuario {
    MESERO, CHEF, CAJERO, ADMIN
}

enum class EstadoUsuario {
    ACTIVO, INACTIVO, EN_DESCANSO
}