package mx.utng.carh.meserowatch.mobile.presentation.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.carh.meserowatch.mobile.domain.model.Platillo
import mx.utng.carh.meserowatch.mobile.presentation.viewmodel.MenuAdminViewModel

// --- FilterChip personalizado (reemplaza al de Material3) ---
@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF1E293B),
        modifier = Modifier.height(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text, color = if (isSelected) Color.White else Color.Gray, fontSize = 14.sp)
        }
    }
}
// ------------------------------------------------------------

@Composable
fun MenuAdminScreen(viewModel: MenuAdminViewModel = viewModel()) {
    var mostrarNuevoPlatillo by remember { mutableStateOf(false) }
    var platilloAEditar by remember { mutableStateOf<Platillo?>(null) }

    if (mostrarNuevoPlatillo) {
        NuevoPlatilloDialog(onDismiss = { mostrarNuevoPlatillo = false }) { nuevo ->
            viewModel.addPlatillo(nuevo)
        }
    }

    if (platilloAEditar != null) {
        EditarPlatilloDialog(platillo = platilloAEditar!!, onDismiss = { platilloAEditar = null }) { editado ->
            viewModel.updatePlatillo(editado)
        }
    }

    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Menú", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${state.platillos.size} platillos registrados", color = Color.Gray)
            }
            Button(
                onClick = { mostrarNuevoPlatillo = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(" Agregar")
            }
        }

        Spacer(Modifier.height(24.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val categorias = listOf("Todos", "Entradas", "Platos", "Bebidas", "Postres", "Complementos", "Especiales")
            items(categorias) { cat ->
                FilterChip(cat, state.selectedCategory == cat) { viewModel.onCategorySelected(cat) }
            }
        }

        Spacer(Modifier.height(16.dp))

        val platillosFiltrados = if (state.selectedCategory == "Todos") {
            state.platillos
        } else {
            state.platillos.filter { it.categoria == state.selectedCategory }
        }.filter {
            state.searchQuery.isEmpty() || it.nombre.contains(state.searchQuery, ignoreCase = true)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(platillosFiltrados) { platillo ->
                AdminPlatilloItem(
                    platillo = platillo,
                    onEdit = { platilloAEditar = platillo },
                    onDelete = { viewModel.deletePlatillo(platillo.id) }
                )
            }
        }
    }
}

@Composable
fun AdminPlatilloItem(platillo: Platillo, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(platillo.emoji, fontSize = 28.sp)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(platillo.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(platillo.categoria, color = Color.Gray, fontSize = 14.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${platillo.precio.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                    AssistChip(
                        onClick = onEdit,
                        label = { Text("Editar", color = Color(0xFF6366F1)) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6366F1)) },
                        border = null,
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF312E81).copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

// Diálogos (adaptados para usar callbacks)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoPlatilloDialog(onDismiss: () -> Unit, onGuardar: (Platillo) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Platos") }
    var expanded by remember { mutableStateOf(false) }
    val categorias = listOf("Entradas", "Platos", "Bebidas", "Postres", "Complementos", "Especiales")

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Nuevo platillo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = categoria, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { categoria = cat; expanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(onClick = {
                        if (nombre.isNotBlank() && precio.isNotBlank()) {
                            onGuardar(Platillo(nombre = nombre, precio = precio.toDoubleOrNull() ?: 0.0, categoria = categoria, emoji = when(categoria) { "Bebidas" -> "🥤"; "Postres" -> "🍰"; "Entradas" -> "🥑"; "Complementos" -> "🍟"; else -> "🍽️" }))
                        }
                    }) { Text("Guardar") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPlatilloDialog(platillo: Platillo, onDismiss: () -> Unit, onGuardar: (Platillo) -> Unit) {
    var nombre by remember { mutableStateOf(platillo.nombre) }
    var precio by remember { mutableStateOf(platillo.precio.toString()) }
    var categoria by remember { mutableStateOf(platillo.categoria) }
    var expanded by remember { mutableStateOf(false) }
    val categorias = listOf("Entradas", "Platos", "Bebidas", "Postres", "Complementos", "Especiales")

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Editar platillo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = categoria, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { categoria = cat; expanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(onClick = {
                        if (nombre.isNotBlank() && precio.isNotBlank()) {
                            onGuardar(platillo.copy(nombre = nombre, precio = precio.toDoubleOrNull() ?: 0.0, categoria = categoria, emoji = when(categoria) { "Bebidas" -> "🥤"; "Postres" -> "🍰"; "Entradas" -> "🥑"; "Complementos" -> "🍟"; else -> "🍽️" }))
                        }
                    }) { Text("Actualizar") }
                }
            }
        }
    }
}