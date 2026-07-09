package mx.utng.carh.meserowatch.mobile.presentation.di

import mx.utng.carh.meserowatch.mobile.data.source.*
import mx.utng.carh.meserowatch.mobile.data.repository.*
import mx.utng.carh.meserowatch.mobile.domain.repository.*

object AppModule {
    // Data sources
    private val authDataSource by lazy { AuthDataSource() }
    private val menuDataSource by lazy { MenuDataSource() }
    private val pedidoDataSource by lazy { PedidoDataSource() }
    private val mesaDataSource by lazy { MesaDataSource() }
    private val zonaDataSource by lazy { ZonaDataSource() }
    private val usuarioDataSource by lazy { UsuarioDataSource() }

    // Repositories
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(authDataSource) }
    val menuRepository: MenuRepository by lazy { MenuRepositoryImpl(menuDataSource) }
    val pedidoRepository: PedidoRepository by lazy { PedidoRepositoryImpl(pedidoDataSource) }
    val mesaRepository: MesaRepository by lazy { MesaRepositoryImpl(mesaDataSource) }
    val zonaRepository: ZonaRepository by lazy { ZonaRepositoryImpl(zonaDataSource) }
    val usuarioRepository: UsuarioRepository by lazy { UsuarioRepositoryImpl(usuarioDataSource) }
}