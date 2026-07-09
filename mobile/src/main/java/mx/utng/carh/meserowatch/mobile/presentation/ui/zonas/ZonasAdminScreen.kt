package mx.utng.carh.meserowatch.mobile.presentation.ui.zonas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import mx.utng.carh.meserowatch.mobile.domain.model.EstadoZona
import mx.utng.carh.meserowatch.mobile.domain.model.Zona
import mx.utng.carh.meserowatch.mobile.presentation.viewmodel.ZonasAdminViewModel

@Composable
fun ZonasAdminScreen(viewModel: ZonasAdminViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    if (state.showNuevaZonaDialog) {
        NuevaZonaDialog(
            onDismiss = { viewModel.hideNuevaZonaDialog() },
            onGuardar = { viewModel.addZona(it) }
        )
    }
    if (state.zonaAEditar != null) {
        EditarZonaDialog(
            zona = state.zonaAEditar!!,
            onDismiss = { viewModel.cancelEditZona() },
            onGuardar = { viewModel.updateZona(it) },
            onEliminar = { viewModel.deleteZona(state.zonaAEditar!!.id) }
        )
    }

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
            Text("Zonas", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Button(
                onClick = { viewModel.showNuevaZonaDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Nueva", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        val clasificaciones = listOf("Zona A", "Zona B", "Zona C")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(clasificaciones) { clase ->
                Column {
                    Text(clase, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(12.dp))
                    val zonasClase = state.zonas.filter { it.nombreZona.contains(clase, ignoreCase = true) }
                    if (zonasClase.isEmpty()) {
                        Text("Sin zonas asignadas", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
                    } else {
                        zonasClase.forEach { zona ->
                            val personalEnZona = state.usuarios.filter { it.zonaId == zona.id }
                            ZonaItem(zona, personalEnZona) { viewModel.editZona(zona) }
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
            item {
                val otras = state.zonas.filter { zona -> !clasificaciones.any { c -> zona.nombreZona.contains(c, ignoreCase = true) } }
                if (otras.isNotEmpty()) {
                    Text("Otras Zonas", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(12.dp))
                    otras.forEach { zona ->
                        val personalEnZona = state.usuarios.filter { it.zonaId == zona.id }
                        ZonaItem(zona, personalEnZona) { viewModel.editZona(zona) }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZonaItem(zona: Zona, personal: List<mx.utng.carh.meserowatch.mobile.domain.model.Usuario>, onLongClick: () -> Unit) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (personal.isNotEmpty()) expandido = !expandido },
                onLongClick = onLongClick
            )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusColor = if (zona.estadoZona == EstadoZona.DISPONIBLE) Color(0xFF10B981) else Color(0xFFEF4444)
                Box(Modifier.size(12.dp).background(statusColor, CircleShape))
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(zona.nombreZona, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${personal.size} personas asignadas", color = Color.Gray, fontSize = 12.sp)
                }
                if (personal.isNotEmpty()) {
                    Icon(
                        imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }

            AnimatedVisibility(visible = expandido, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    Spacer(Modifier.height(12.dp))
                    personal.forEach { usuario ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(usuario.fotoEmoji, fontSize = 20.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(usuario.nombre, color = Color.White, fontSize = 14.sp)
                            Spacer(Modifier.weight(1f))
                            if (usuario.activo) {
                                Badge(containerColor = Color(0xFF10B981).copy(0.2f), contentColor = Color(0xFF10B981)) {
                                    Text("En turno", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Diálogos para agregar/editar zonas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaZonaDialog(onDismiss: () -> Unit, onGuardar: (Zona) -> Unit) {
    var nombreZona by remember { mutableStateOf("") }
    var estadoInicial by remember { mutableStateOf(EstadoZona.DISPONIBLE) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Nueva zona", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = nombreZona, onValueChange = { nombreZona = it }, label = { Text("Nombre de zona") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoZona.values().forEach { estado ->
                        FilterChip(selected = estadoInicial == estado, onClick = { estadoInicial = estado }, label = { Text(estado.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    if (nombreZona.isNotBlank()) {
                        onGuardar(Zona(nombreZona = nombreZona, estadoZona = estadoInicial))
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Guardar zona") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarZonaDialog(zona: Zona, onDismiss: () -> Unit, onGuardar: (Zona) -> Unit, onEliminar: () -> Unit) {
    var nombreZona by remember { mutableStateOf(zona.nombreZona) }
    var estado by remember { mutableStateOf(zona.estadoZona) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Editar zona", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = nombreZona, onValueChange = { nombreZona = it }, label = { Text("Nombre de zona") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoZona.values().forEach { e ->
                        FilterChip(selected = estado == e, onClick = { estado = e }, label = { Text(e.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEliminar) { Text("Eliminar", color = Color(0xFFEF4444)) }
                    Button(onClick = {
                        onGuardar(zona.copy(nombreZona = nombreZona, estadoZona = estado))
                    }) { Text("Actualizar") }
                }
            }
        }
    }
}