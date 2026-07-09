package mx.utng.carh.meserowatch.mobile.presentation.ui.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import mx.utng.carh.meserowatch.mobile.domain.model.RolUsuario
import mx.utng.carh.meserowatch.mobile.presentation.ui.login.ValidationRow
import mx.utng.carh.meserowatch.mobile.presentation.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.registerSuccess) {
        if (state.registerSuccess) onRegisterSuccess()
    }

    val hasMinLength = state.password.length >= 8
    val hasNumber = state.password.any { it.isDigit() }
    val hasUppercase = state.password.any { it.isUpperCase() }
    val hasSpecialChar = state.password.any { !it.isLetterOrDigit() }
    val passwordsMatch = state.password.isNotEmpty() && state.password == state.confirmPassword
    val allRulesMet = hasMinLength && hasNumber && hasUppercase && hasSpecialChar && passwordsMatch

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1200&auto=format&fit=crop",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color(0xFF0F172A).copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Crear Cuenta",
                fontSize = 42.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )
            Text(
                "Únete al equipo de MeseroWatch",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.7f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = state.user,
                        onValueChange = viewModel::onUserChanged,
                        label = { Text("Nombre de Usuario") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF3B82F6)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6)
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    var expandedRol by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedRol,
                        onExpandedChange = { expandedRol = !expandedRol }
                    ) {
                        OutlinedTextField(
                            value = state.rol.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Rol") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedRol,
                            onDismissRequest = { expandedRol = false }
                        ) {
                            RolUsuario.values().filter { it != RolUsuario.ADMIN }.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption.name) },
                                    onClick = {
                                        viewModel.onRolChanged(selectionOption)
                                        expandedRol = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF3B82F6)) },
                        trailingIcon = {
                            IconButton(onClick = viewModel::togglePasswordVisibility) {
                                Icon(
                                    imageVector = if (state.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6)
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChanged,
                        label = { Text("Confirmar Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF3B82F6)) },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6)
                        )
                    )

                    AnimatedVisibility(visible = state.password.isNotEmpty()) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            ValidationRow("Mínimo 8 caracteres", hasMinLength)
                            ValidationRow("Al menos un número", hasNumber)
                            ValidationRow("Una letra mayúscula", hasUppercase)
                            ValidationRow("Un carácter especial", hasSpecialChar)
                            ValidationRow("Las contraseñas coinciden", passwordsMatch)
                        }
                    }

                    if (state.error != null) {
                        Text(state.error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = viewModel::register,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (allRulesMet) Color(0xFF3B82F6) else Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Registrarse", fontWeight = FontWeight.Bold)
                    }
                }
            }

            TextButton(onClick = onBackToLogin, modifier = Modifier.padding(top = 16.dp)) {
                Text("¿Ya tienes cuenta? Inicia Sesión", color = Color(0xFF3B82F6))
            }
        }
    }
}