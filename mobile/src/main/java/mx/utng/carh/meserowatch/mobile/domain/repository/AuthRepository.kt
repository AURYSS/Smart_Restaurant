package mx.utng.carh.meserowatch.mobile.domain.repository

import mx.utng.carh.meserowatch.mobile.domain.model.Usuario

interface AuthRepository {
    suspend fun loginAsAdmin(): Result<Usuario>
    suspend fun login(nombreUsuario: String): Result<Usuario>
    suspend fun register(usuario: Usuario): Result<Unit>
}