package mx.utng.carh.meserowatch.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.carh.meserowatch.mobile.domain.model.Usuario
import mx.utng.carh.meserowatch.mobile.domain.model.Zona
import mx.utng.carh.meserowatch.mobile.domain.repository.UsuarioRepository
import mx.utng.carh.meserowatch.mobile.domain.repository.ZonaRepository
import mx.utng.carh.meserowatch.mobile.presentation.di.AppModule

data class ZonasState(
    val zonas: List<Zona> = emptyList(),
    val usuarios: List<Usuario> = emptyList(),
    val showNuevaZonaDialog: Boolean = false,
    val zonaAEditar: Zona? = null
)

class ZonasAdminViewModel : ViewModel() {
    private val zonaRepo = AppModule.zonaRepository
    private val usuarioRepo = AppModule.usuarioRepository

    private val _state = MutableStateFlow(ZonasState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            zonaRepo.getZonas().collect { zonas ->
                _state.value = _state.value.copy(zonas = zonas)
            }
        }
        viewModelScope.launch {
            usuarioRepo.getUsuarios().collect { usuarios ->
                _state.value = _state.value.copy(usuarios = usuarios)
            }
        }
    }

    fun showNuevaZonaDialog() {
        _state.value = _state.value.copy(showNuevaZonaDialog = true)
    }

    fun hideNuevaZonaDialog() {
        _state.value = _state.value.copy(showNuevaZonaDialog = false)
    }

    fun addZona(zona: Zona) {
        viewModelScope.launch {
            zonaRepo.addZona(zona)
            hideNuevaZonaDialog()
        }
    }

    fun editZona(zona: Zona) {
        _state.value = _state.value.copy(zonaAEditar = zona)
    }

    fun cancelEditZona() {
        _state.value = _state.value.copy(zonaAEditar = null)
    }

    fun updateZona(zona: Zona) {
        viewModelScope.launch {
            zonaRepo.updateZona(zona)
            cancelEditZona()
        }
    }

    fun deleteZona(id: String) {
        viewModelScope.launch {
            zonaRepo.deleteZona(id)
            cancelEditZona()
        }
    }
}