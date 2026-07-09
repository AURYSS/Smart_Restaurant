package mx.utng.carh.meserowatch.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.carh.meserowatch.mobile.domain.model.EstadoUsuario
import mx.utng.carh.meserowatch.mobile.domain.model.RolUsuario
import mx.utng.carh.meserowatch.mobile.domain.model.Usuario
import mx.utng.carh.meserowatch.mobile.domain.repository.AuthRepository
import mx.utng.carh.meserowatch.mobile.presentation.di.AppModule

data class RegisterUiState(
    val user: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val rol: RolUsuario = RolUsuario.MESERO,
    val isLoading: Boolean = false,
    val error: String? = null,
    val registerSuccess: Boolean = false,
    val passwordVisible: Boolean = false
)

class RegisterViewModel : ViewModel() {
    private val authRepo = AppModule.authRepository

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onUserChanged(v: String) { _uiState.value = _uiState.value.copy(user = v, error = null) }
    fun onPasswordChanged(v: String) { _uiState.value = _uiState.value.copy(password = v, error = null) }
    fun onConfirmPasswordChanged(v: String) { _uiState.value = _uiState.value.copy(confirmPassword = v, error = null) }
    fun onRolChanged(rol: RolUsuario) { _uiState.value = _uiState.value.copy(rol = rol) }
    fun togglePasswordVisibility() { _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible) }

    fun register() {
        val state = _uiState.value
        if (state.user.isEmpty()) { _uiState.value = state.copy(error = "Ingresa un nombre de usuario"); return }
        val hasMinLength = state.password.length >= 8
        val hasNumber = state.password.any { it.isDigit() }
        val hasUppercase = state.password.any { it.isUpperCase() }
        val hasSpecialChar = state.password.any { !it.isLetterOrDigit() }
        val passwordsMatch = state.password.isNotEmpty() && state.password == state.confirmPassword
        if (!hasMinLength || !hasNumber || !hasUppercase || !hasSpecialChar || !passwordsMatch) {
            _uiState.value = state.copy(error = "Revisa los requisitos de seguridad")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val nuevoUsuario = Usuario(
                nombre = state.user.trim(),
                rol = state.rol,
                activo = true,
                estadoUsuario = EstadoUsuario.ACTIVO
            )
            authRepo.register(nuevoUsuario)
                .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false, registerSuccess = true) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
        }
    }
}