package mx.utng.carh.meserowatch.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.carh.meserowatch.mobile.domain.model.Usuario
import mx.utng.carh.meserowatch.mobile.domain.repository.UsuarioRepository
import mx.utng.carh.meserowatch.mobile.presentation.di.AppModule

data class UsuariosState(
    val usuarios: List<Usuario> = emptyList(),
    val filter: String = "Todos",
    val isLoading: Boolean = false,
    val showNuevoDialog: Boolean = false,
    val usuarioAEditar: Usuario? = null
)

class UsuariosViewModel : ViewModel() {
    private val repo = AppModule.usuarioRepository

    private val _state = MutableStateFlow(UsuariosState())
    val state = _state.asStateFlow()

    init {
        loadUsuarios()
    }

    private fun loadUsuarios() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repo.getUsuarios().collect { lista ->
                _state.value = _state.value.copy(usuarios = lista, isLoading = false)
            }
        }
    }

    fun setFilter(filter: String) {
        _state.value = _state.value.copy(filter = filter)
    }

    fun showNuevoUsuarioDialog() {
        _state.value = _state.value.copy(showNuevoDialog = true)
    }

    fun hideNuevoUsuarioDialog() {
        _state.value = _state.value.copy(showNuevoDialog = false)
    }

    fun editUsuario(usuario: Usuario) {
        _state.value = _state.value.copy(usuarioAEditar = usuario)
    }

    fun cancelEditUsuario() {
        _state.value = _state.value.copy(usuarioAEditar = null)
    }

    fun addUsuario(usuario: Usuario) {
        viewModelScope.launch {
            repo.addUsuario(usuario)
            hideNuevoUsuarioDialog()
        }
    }

    fun updateUsuario(usuario: Usuario) {
        viewModelScope.launch {
            repo.updateUsuario(usuario)
            cancelEditUsuario()
        }
    }

    fun deleteUsuario(id: String) {
        viewModelScope.launch {
            repo.deleteUsuario(id)
        }
    }
}