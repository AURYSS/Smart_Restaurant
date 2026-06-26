package mx.utng.carh.meserowatch.mobile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SessionManager {
    var currentUser by mutableStateOf<Usuario?>(null)
    var isAdmin by mutableStateOf(false)

    fun loginAsAdmin() {
        currentUser = Usuario(id = "admin", nombre = "Administrador", rol = RolUsuario.ADMIN)
        isAdmin = true
    }

    fun loginAsUser(usuario: Usuario) {
        currentUser = usuario
        isAdmin = usuario.rol == RolUsuario.ADMIN
    }

    fun logout() {
        currentUser = null
        isAdmin = false
    }
}
