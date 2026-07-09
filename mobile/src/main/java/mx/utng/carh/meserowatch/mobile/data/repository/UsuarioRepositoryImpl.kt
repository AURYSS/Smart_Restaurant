package mx.utng.carh.meserowatch.mobile.data.repository

import kotlinx.coroutines.flow.Flow
import mx.utng.carh.meserowatch.mobile.data.source.UsuarioDataSource
import mx.utng.carh.meserowatch.mobile.domain.model.Usuario
import mx.utng.carh.meserowatch.mobile.domain.repository.UsuarioRepository

class UsuarioRepositoryImpl(private val dataSource: UsuarioDataSource) : UsuarioRepository {
    override fun getUsuarios(): Flow<List<Usuario>> = dataSource.getUsuarios()
    override suspend fun addUsuario(usuario: Usuario) = dataSource.addUsuario(usuario)
    override suspend fun updateUsuario(usuario: Usuario) = dataSource.updateUsuario(usuario)
    override suspend fun deleteUsuario(id: String) = dataSource.deleteUsuario(id)
}