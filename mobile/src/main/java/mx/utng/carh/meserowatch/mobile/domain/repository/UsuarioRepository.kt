package mx.utng.carh.meserowatch.mobile.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.utng.carh.meserowatch.mobile.domain.model.Usuario

interface UsuarioRepository {
    fun getUsuarios(): Flow<List<Usuario>>
    suspend fun addUsuario(usuario: Usuario)
    suspend fun updateUsuario(usuario: Usuario)
    suspend fun deleteUsuario(id: String)
}