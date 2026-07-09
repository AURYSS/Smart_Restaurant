package mx.utng.carh.meserowatch.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.carh.meserowatch.mobile.SessionManager
import mx.utng.carh.meserowatch.mobile.presentation.di.AppModule

data class LoginUiState(
    val user: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val passwordVisible: Boolean = false,
    val loginSuccess: Boolean = false
)

class LoginViewModel : ViewModel() {
    private val authRepo = AppModule.authRepository

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onUserChanged(value: String) {
        _uiState.value = _uiState.value.copy(user = value, error = null)
    }
    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }
    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }

    fun login() {
        val state = _uiState.value
        if (state.user == "admin" && state.password == "admin123") {
            viewModelScope.launch {
                authRepo.loginAsAdmin().onSuccess { admin ->
                    SessionManager.loginAsAdmin() // Mantenemos SessionManager
                    _uiState.value = _uiState.value.copy(isLoggedIn = true, loginSuccess = true)
                }.onFailure {
                    _uiState.value = _uiState.value.copy(error = "Error al autenticar admin")
                }
            }
            return
        }

        // Validación de contraseña (misma lógica que antes)
        val hasMinLength = state.password.length >= 8
        val hasNumber = state.password.any { it.isDigit() }
        val hasUppercase = state.password.any { it.isUpperCase() }
        val hasSpecialChar = state.password.any { !it.isLetterOrDigit() }
        if (!hasMinLength || !hasNumber || !hasUppercase || !hasSpecialChar) {
            _uiState.value = _uiState.value.copy(error = "La contraseña no cumple los requisitos")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepo.login(state.user.trim())
                .onSuccess { usuario ->
                    SessionManager.loginAsUser(usuario)
                    _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }
}