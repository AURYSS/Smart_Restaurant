package mx.utng.carh.meserowatch.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.carh.meserowatch.mobile.domain.model.Platillo
import mx.utng.carh.meserowatch.mobile.domain.repository.MenuRepository
import mx.utng.carh.meserowatch.mobile.presentation.di.AppModule

data class MenuAdminState(
    val platillos: List<Platillo> = emptyList(),
    val selectedCategory: String = "Todos",
    val searchQuery: String = ""
)

class MenuAdminViewModel : ViewModel() {
    private val repo = AppModule.menuRepository

    private val _state = MutableStateFlow(MenuAdminState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getMenu().collect { lista ->
                _state.value = _state.value.copy(platillos = lista)
            }
        }
    }

    fun onCategorySelected(cat: String) { _state.value = _state.value.copy(selectedCategory = cat) }
    fun onSearchQueryChanged(q: String) { _state.value = _state.value.copy(searchQuery = q) }

    fun addPlatillo(platillo: Platillo) {
        viewModelScope.launch { repo.addPlatillo(platillo) }
    }
    fun updatePlatillo(platillo: Platillo) {
        viewModelScope.launch { repo.updatePlatillo(platillo) }
    }
    fun deletePlatillo(id: String) {
        viewModelScope.launch { repo.deletePlatillo(id) }
    }
}