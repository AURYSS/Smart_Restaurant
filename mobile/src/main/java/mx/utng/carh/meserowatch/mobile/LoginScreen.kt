package mx.utng.carh.meserowatch.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onNavigateToRegister: () -> Unit) {
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Reglas de validación
    val hasMinLength = password.length >= 8
    val hasNumber = password.any { it.isDigit() }
    val hasUppercase = password.any { it.isUpperCase() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }
    
    val allRulesMet = hasMinLength && hasNumber && hasUppercase && hasSpecialChar

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo (Restaurante moderno)
        AsyncImage(
            model = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1200&auto=format&fit=crop",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay gradiente para legibilidad
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
                .imePadding() // Asegura que el contenido suba con el teclado
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(40.dp))
            
            Text(
                "MeseroWatch", 
                fontSize = 42.sp, 
                color = Color.White, 
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )
            Text(
                "Gestión inteligente para tu restaurante", 
                color = Color.Gray, 
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(48.dp))

            // Card del Formulario
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.7f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("BIENVENIDO", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Ingresa tus credenciales", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    
                    Spacer(Modifier.height(24.dp))

                    // Campo Usuario
                    OutlinedTextField(
                        value = user,
                        onValueChange = { user = it },
                        label = { Text("Usuario o Email") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF3B82F6)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // Campo Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF3B82F6)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )

                    // Tabla de Validación de Contraseña
                    AnimatedVisibility(visible = password.isNotEmpty()) {
                        Column(modifier = Modifier.padding(top = 16.dp, start = 4.dp)) {
                            Text("SEGURIDAD DE CONTRASEÑA", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            
                            ValidationRow("Mínimo 8 caracteres", hasMinLength)
                            ValidationRow("Al menos un número", hasNumber)
                            ValidationRow("Una letra mayúscula", hasUppercase)
                            ValidationRow("Un carácter especial", hasSpecialChar)
                        }
                    }

                    if (error.isNotEmpty()) {
                        Text(
                            error, 
                            color = Color(0xFFEF4444), 
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    // Botón Entrar
                    Button(
                        onClick = {
                            if (user == "admin" && password == "admin123") {
                                SessionManager.loginAsAdmin()
                                onLoginSuccess()
                            } else if (!allRulesMet && user != "admin") {
                                error = "La contraseña no cumple con los requisitos de seguridad"
                            } else {
                                isLoading = true
                                val db = FirebaseDatabase.getInstance().getReference("usuarios")
                                db.orderByChild("nombre").equalTo(user.trim()).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        isLoading = false
                                        if (snapshot.exists()) {
                                            val userDoc = snapshot.children.first()
                                            val u = Usuario(
                                                id = userDoc.key ?: "",
                                                nombre = userDoc.child("nombre").value.toString(),
                                                rol = try { 
                                                    RolUsuario.valueOf(userDoc.child("rol").value.toString())
                                                } catch(e: Exception) { RolUsuario.MESERO }
                                            )
                                            SessionManager.loginAsUser(u)
                                            onLoginSuccess()
                                        } else {
                                            error = "Usuario no encontrado"
                                        }
                                    }
                                    override fun onCancelled(dbError: DatabaseError) {
                                        isLoading = false
                                        error = "Error al conectar"
                                    }
                                })
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (allRulesMet || user == "admin") Color(0xFF3B82F6) else Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            TextButton(onClick = onNavigateToRegister, modifier = Modifier.padding(top = 16.dp)) {
                Text("¿No tienes cuenta? Regístrate aquí", color = Color(0xFF3B82F6))
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ValidationRow(text: String, isMet: Boolean) {
    val color by animateColorAsState(
        targetValue = if (isMet) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.5f),
        animationSpec = tween(300)
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = color, fontSize = 12.sp)
    }
}
