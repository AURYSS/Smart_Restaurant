package mx.utng.carh.meserowatch.mobile.data.repository

import mx.utng.carh.meserowatch.mobile.data.source.AuthDataSource
import mx.utng.carh.meserowatch.mobile.domain.model.Usuario
import mx.utng.carh.meserowatch.mobile.domain.repository.AuthRepository

class AuthRepositoryImpl(private val dataSource: AuthDataSource) : AuthRepository {

    override suspend fun loginAsAdmin(): Result<Usuario> {
        return try {
            val admin = dataSource.loginAsAdmin()
            if (admin != null) Result.success(admin)
            else Result.failure(Exception("No se pudo autenticar como admin"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(nombreUsuario: String): Result<Usuario> {
        return try {
            val user = dataSource.login(nombreUsuario)
            if (user != null) Result.success(user)
            else Result.failure(Exception("Usuario no encontrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(usuario: Usuario): Result<Unit> {
        return try {
            dataSource.register(usuario)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}