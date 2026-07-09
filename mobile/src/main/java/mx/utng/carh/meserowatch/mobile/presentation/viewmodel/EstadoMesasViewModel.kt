package mx.utng.carh.meserowatch.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.carh.meserowatch.mobile.domain.model.Mesa
import mx.utng.carh.meserowatch.mobile.domain.repository.MesaRepository
import mx.utng.carh.meserowatch.mobile.presentation.di.AppModule

data class EstadoMesasState(
    val mesas: List<Mesa> = emptyList(),
    val showNuevaMesaDialog: Boolean = false
)

class EstadoMesasViewModel : ViewModel() {
    private val repo = AppModule.mesaRepository

    private val _state = MutableStateFlow(EstadoMesasState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getTodasLasMesas().collect { mesas ->
                _state.value = _state.value.copy(mesas = mesas)
            }
        }
    }

    fun showNuevaMesaDialog() {
        _state.value = _state.value.copy(showNuevaMesaDialog = true)
    }

    fun hideNuevaMesaDialog() {
        _state.value = _state.value.copy(showNuevaMesaDialog = false)
    }

    fun addMesa(mesa: Mesa) {
        viewModelScope.launch {
            repo.addMesa(mesa)
            hideNuevaMesaDialog()
        }
    }

    fun updateMesa(mesa: Mesa) {
        viewModelScope.launch { repo.updateMesa(mesa) }
    }

    fun deleteMesa(id: Int) {
        viewModelScope.launch { repo.deleteMesa(id) }
    }
}