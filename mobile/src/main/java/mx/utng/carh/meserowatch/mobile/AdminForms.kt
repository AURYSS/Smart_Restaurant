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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoPlatilloDialog(onDismiss: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Platos") }
    var expanded by remember { mutableStateOf(false) }
    val categorias = listOf("Entradas", "Platos", "Bebidas", "Postres", "Complementos", "Especiales")
    
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
                        Text("Agregar al menú por categoría", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text("Nombre del platillo *", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = nombre, 
                    onValueChange = { nombre = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej. Coca Cola", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text("Categoría *", color = Color.White, fontSize = 14.sp)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categorias.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    categoria = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                
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
                                val emoji = when(categoria) {
                                    "Bebidas" -> "🥤"
                                    "Postres" -> "🍰"
                                    "Entradas" -> "🥑"
                                    "Complementos" -> "🍟"
                                    else -> "🍽️"
                                }
                                database.child(key).setValue(mapOf(
                                    "id" to key,
                                    "nombre" to nombre,
                                    "precio" to (precio.toDoubleOrNull() ?: 0.0),
                                    "categoria" to categoria,
                                    "emoji" to emoji,
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
fun EditarPlatilloDialog(platillo: Platillo, onDismiss: () -> Unit) {
    var nombre by remember { mutableStateOf(platillo.nombre) }
    var precio by remember { mutableStateOf(platillo.precio.toString()) }
    var categoria by remember { mutableStateOf(platillo.categoria) }
    var expanded by remember { mutableStateOf(false) }
    val categorias = listOf("Entradas", "Platos", "Bebidas", "Postres", "Complementos", "Especiales")
    
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
                        Text("Editar platillo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Modificar detalles del menú", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text("Nombre del platillo *", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = nombre, 
                    onValueChange = { nombre = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text("Categoría *", color = Color.White, fontSize = 14.sp)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categorias.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    categoria = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                
                Text("Precio *", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = precio, 
                    onValueChange = { precio = it },
                    modifier = Modifier.fillMaxWidth(),
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
                                val emoji = when(categoria) {
                                    "Bebidas" -> "🥤"
                                    "Postres" -> "🍰"
                                    "Entradas" -> "🥑"
                                    "Complementos" -> "🍟"
                                    else -> "🍽️"
                                }
                                database.child(platillo.id).updateChildren(mapOf(
                                    "nombre" to nombre,
                                    "precio" to (precio.toDoubleOrNull() ?: 0.0),
                                    "categoria" to categoria,
                                    "emoji" to emoji
                                ))
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("✓ Actualizar")
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
    var rol by remember { mutableStateOf(RolUsuario.MESERO) }
    var zonaId by remember { mutableStateOf("") }
    var estadoInicial by remember { mutableStateOf(EstadoUsuario.ACTIVO) }
    var expandedRol by remember { mutableStateOf(false) }
    var expandedZona by remember { mutableStateOf(false) }
    
    val database = FirebaseDatabase.getInstance().getReference("usuarios")
    val databaseZonas = FirebaseDatabase.getInstance().getReference("zonas")
    var listaZonas by remember { mutableStateOf<List<Zona>>(emptyList()) }

    LaunchedEffect(Unit) {
        databaseZonas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val zonas = mutableListOf<Zona>()
                snapshot.children.forEach { child ->
                    zonas.add(Zona(id = child.key ?: "", nombreZona = child.child("nombreZona").value?.toString() ?: ""))
                }
                listaZonas = zonas
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

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

                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rol *", color = Color.White, fontSize = 14.sp)
                        ExposedDropdownMenuBox(
                            expanded = expandedRol,
                            onExpandedChange = { expandedRol = !expandedRol }
                        ) {
                            OutlinedTextField(
                                value = rol.name,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) },
                                modifier = Modifier.menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedRol,
                                onDismissRequest = { expandedRol = false }
                            ) {
                                RolUsuario.values().forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption.name) },
                                        onClick = {
                                            rol = selectionOption
                                            expandedRol = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Zona asignada *", color = Color.White, fontSize = 14.sp)
                        ExposedDropdownMenuBox(
                            expanded = expandedZona,
                            onExpandedChange = { expandedZona = !expandedZona }
                        ) {
                            OutlinedTextField(
                                value = listaZonas.find { it.id == zonaId }?.nombreZona ?: "Seleccionar...",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedZona) },
                                modifier = Modifier.menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedZona,
                                onDismissRequest = { expandedZona = false }
                            ) {
                                listaZonas.forEach { zona ->
                                    DropdownMenuItem(
                                        text = { Text(zona.nombreZona) },
                                        onClick = {
                                            zonaId = zona.id
                                            expandedZona = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Estado inicial *", color = Color.White, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoUsuario.values().forEach { estado ->
                        val isSelected = estadoInicial == estado
                        FilterChip(
                            selected = isSelected,
                            onClick = { estadoInicial = estado },
                            label = { Text(estado.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3B82F6),
                                labelColor = if (isSelected) Color.White else Color.Gray
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
                            if (nombre.isNotEmpty()) {
                                val key = database.push().key ?: ""
                                database.child(key).setValue(Usuario(
                                    id = key,
                                    nombre = nombre,
                                    rol = rol,
                                    activo = estadoInicial == EstadoUsuario.ACTIVO,
                                    estadoUsuario = estadoInicial,
                                    zonaId = zonaId,
                                    zonaAsignada = listaZonas.find { it.id == zonaId }?.nombreZona ?: ""
                                ))
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
fun EditarUsuarioDialog(usuario: Usuario, onDismiss: () -> Unit) {
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var rol by remember { mutableStateOf(usuario.rol) }
    var zonaId by remember { mutableStateOf(usuario.zonaId) }
    var estado by remember { mutableStateOf(usuario.estadoUsuario) }
    var expandedRol by remember { mutableStateOf(false) }
    var expandedZona by remember { mutableStateOf(false) }
    
    val database = FirebaseDatabase.getInstance().getReference("usuarios")
    val databaseZonas = FirebaseDatabase.getInstance().getReference("zonas")
    var listaZonas by remember { mutableStateOf<List<Zona>>(emptyList()) }

    LaunchedEffect(Unit) {
        databaseZonas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val zonas = mutableListOf<Zona>()
                snapshot.children.forEach { child ->
                    zonas.add(Zona(id = child.key ?: "", nombreZona = child.child("nombreZona").value?.toString() ?: ""))
                }
                listaZonas = zonas
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

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
                        Text("Editar usuario", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Modificar información del personal", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text("Nombre completo *", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = nombre, 
                    onValueChange = { nombre = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rol *", color = Color.White, fontSize = 14.sp)
                        ExposedDropdownMenuBox(
                            expanded = expandedRol,
                            onExpandedChange = { expandedRol = !expandedRol }
                        ) {
                            OutlinedTextField(
                                value = rol.name,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) },
                                modifier = Modifier.menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedRol,
                                onDismissRequest = { expandedRol = false }
                            ) {
                                RolUsuario.values().forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption.name) },
                                        onClick = {
                                            rol = selectionOption
                                            expandedRol = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Zona *", color = Color.White, fontSize = 14.sp)
                        ExposedDropdownMenuBox(
                            expanded = expandedZona,
                            onExpandedChange = { expandedZona = !expandedZona }
                        ) {
                            OutlinedTextField(
                                value = listaZonas.find { it.id == zonaId }?.nombreZona ?: "Seleccionar...",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedZona) },
                                modifier = Modifier.menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedZona,
                                onDismissRequest = { expandedZona = false }
                            ) {
                                listaZonas.forEach { zona ->
                                    DropdownMenuItem(
                                        text = { Text(zona.nombreZona) },
                                        onClick = {
                                            zonaId = zona.id
                                            expandedZona = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Estado *", color = Color.White, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoUsuario.values().forEach { e ->
                        val isSelected = estado == e
                        FilterChip(
                            selected = isSelected,
                            onClick = { estado = e },
                            label = { Text(e.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3B82F6),
                                labelColor = if (isSelected) Color.White else Color.Gray
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
                            if (nombre.isNotEmpty()) {
                                database.child(usuario.id).updateChildren(mapOf(
                                    "nombre" to nombre,
                                    "rol" to rol.name,
                                    "activo" to (estado == EstadoUsuario.ACTIVO),
                                    "estadoUsuario" to estado.name,
                                    "zonaId" to zonaId,
                                    "zonaAsignada" to (listaZonas.find { it.id == zonaId }?.nombreZona ?: "")
                                ))
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("✓ Actualizar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaZonaDialog(onDismiss: () -> Unit) {
    var nombreZona by remember { mutableStateOf("") }
    var estadoInicial by remember { mutableStateOf(EstadoZona.DISPONIBLE) }
    val database = FirebaseDatabase.getInstance().getReference("zonas")

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
                        Text("Nueva zona", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Agregar zona al plano del restaurante", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text("Nombre de zona *", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = nombreZona, 
                    onValueChange = { nombreZona = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej. Terraza exterior", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(Modifier.height(16.dp))
                Text("Estado inicial *", color = Color.White, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoZona.values().forEach { estado ->
                        val isSelected = estadoInicial == estado
                        FilterChip(
                            selected = isSelected,
                            onClick = { estadoInicial = estado },
                            label = { Text(estado.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                labelColor = if (isSelected) Color.White else Color.Gray
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
                            if (nombreZona.isNotEmpty()) {
                                val key = database.push().key ?: ""
                                database.child(key).setValue(Zona(
                                    id = key,
                                    nombreZona = nombreZona,
                                    estadoZona = estadoInicial
                                ))
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("✓ Guardar zona")
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
