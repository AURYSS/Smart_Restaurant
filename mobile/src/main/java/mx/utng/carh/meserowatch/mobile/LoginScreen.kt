package mx.utng.carh.meserowatch.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("MeseroWatch", fontSize = 40.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Text("Inicia sesión para continuar", color = Color.Gray, fontSize = 16.sp)

        Spacer(Modifier.height(48.dp))

        OutlinedTextField(
            value = user,
            onValueChange = { user = it },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF3B82F6)
            )
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF3B82F6)
            )
        )

        if (error.isNotEmpty()) {
            Text(error, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                val inputUser = user.trim()
                val inputPass = password.trim()
                
                if (inputUser == "admin" && inputPass == "admin123") {
                    SessionManager.loginAsAdmin()
                    onLoginSuccess()
                } else {
                    isLoading = true
                    val db = FirebaseDatabase.getInstance().getReference("usuarios")
                    db.orderByChild("nombre").equalTo(inputUser).addListenerForSingleValueEvent(object : ValueEventListener {
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
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White)
            else Text("Entrar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
