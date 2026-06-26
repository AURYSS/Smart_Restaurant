package mx.utng.carh.meserowatch.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoPlatilloDialog(onDismiss: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    val database = FirebaseDatabase.getInstance().getReference("menu")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFFF59E0B))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Nuevo platillo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Agregar al menú con ingredientes y precio", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text("Nombre del platillo *", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = nombre, 
                    onValueChange = { nombre = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej. Tacos de pastor", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(Modifier.height(16.dp))
                
                Text("Precio *", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = precio, 
                    onValueChange = { precio = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
                    Spacer(Modifier.width(16.dp))
                    Button(
                        onClick = { 
                            if (nombre.isNotEmpty() && precio.isNotEmpty()) {
                                val key = database.push().key ?: ""
                                database.child(key).setValue(mapOf(
                                    "id" to key,
                                    "nombre" to nombre,
                                    "precio" to (precio.toDoubleOrNull() ?: 0.0),
                                    "disponible" to true
                                ))
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("✓ Guardar platillo")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoUsuarioDialog(onDismiss: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    val database = FirebaseDatabase.getInstance().getReference("usuarios")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF3B82F6))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Nuevo usuario", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Registrar mesero, chef, cajero o administrador", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text("Nombre completo *", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = nombre, 
                    onValueChange = { nombre = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej. Luis Cervantes García", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
                    Spacer(Modifier.width(16.dp))
                    Button(
                        onClick = { 
                            if (nombre.isNotEmpty()) {
                                val key = database.push().key ?: ""
                                database.child(key).setValue(mapOf("id" to key, "nombre" to nombre))
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("✓ Guardar usuario")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaMesaDialog(onDismiss: () -> Unit) {
    var numeroMesa by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }
    val database = FirebaseDatabase.getInstance().getReference("mesas_config")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TableBar, contentDescription = null, tint = Color(0xFF10B981))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Nueva mesa", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Agregar mesa al plano del restaurante", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Número *", color = Color.White, fontSize = 14.sp)
                        OutlinedTextField(
                            value = numeroMesa, 
                            onValueChange = { numeroMesa = it },
                            placeholder = { Text("Ej. 12", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Capacidad *", color = Color.White, fontSize = 14.sp)
                        OutlinedTextField(
                            value = capacidad, 
                            onValueChange = { capacidad = it },
                            placeholder = { Text("Ej. 4", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
                    Spacer(Modifier.width(16.dp))
                    Button(
                        onClick = { 
                            if (numeroMesa.isNotEmpty()) {
                                val num = numeroMesa.toIntOrNull() ?: 0
                                database.child(num.toString()).setValue(mapOf(
                                    "id" to num,
                                    "capacidad" to (capacidad.toIntOrNull() ?: 4),
                                    "estado" to "LIBRE"
                                ))
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("✓ Guardar mesa")
                    }
                }
            }
        }
    }
}
