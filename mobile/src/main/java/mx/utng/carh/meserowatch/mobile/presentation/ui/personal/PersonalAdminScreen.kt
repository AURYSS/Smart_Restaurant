package mx.utng.carh.meserowatch.mobile.presentation.ui.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import mx.utng.carh.meserowatch.mobile.domain.model.*
import mx.utng.carh.meserowatch.mobile.presentation.ui.nuevopedido.FilterChip
import mx.utng.carh.meserowatch.mobile.presentation.viewmodel.UsuariosViewModel

@Composable
fun PersonalAdminScreen(
    onNavigateToZonas: () -> Unit,
    viewModel: UsuariosViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.showNuevoDialog) {
        NuevoUsuarioDialog(
            onDismiss = { viewModel.hideNuevoUsuarioDialog() },
            onGuardar = { viewModel.addUsuario(it) }
        )
    }
    if (state.usuarioAEditar != null) {
        EditarUsuarioDialog(
            usuario = state.usuarioAEditar!!,
            onDismiss = { viewModel.cancelEditUsuario() },
            onGuardar = { viewModel.updateUsuario(it) }
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
            Column {
                Text("Usuarios", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${state.usuarios.size} registrados", color = Color.Gray)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onNavigateToZonas,
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Zonas", fontSize = 14.sp)
                }
                Button(
                    onClick = { viewModel.showNuevoUsuarioDialog() },
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Nuevo", fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val filtros = listOf("Todos", "Activo", "Inactivo", "En descanso")
            items(filtros) { filtro ->
                FilterChip(filtro, state.filter == filtro) { viewModel.setFilter(filtro) }
            }
        }

        Spacer(Modifier.height(16.dp))

        val usuariosFiltrados = if (state.filter == "Todos") {
            state.usuarios
        } else {
            state.usuarios.filter { it.estadoUsuario.name.equals(state.filter.replace(" ", "_"), ignoreCase = true) }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
            items(usuariosFiltrados) { usuario ->
                UsuarioItem(usuario) { viewModel.editUsuario(usuario) }
            }
        }
    }
}

@Composable
fun UsuarioItem(usuario: Usuario, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp).background(Color(0xFF1E293B), CircleShape), contentAlignment = Alignment.Center) {
            Text(usuario.fotoEmoji, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(usuario.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("${usuario.rol} · ${usuario.zonaAsignada}", color = Color.Gray, fontSize = 14.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            val statusColor = when (usuario.estadoUsuario) {
                EstadoUsuario.ACTIVO -> Color(0xFF10B981)
                EstadoUsuario.INACTIVO -> Color(0xFFEF4444)
                EstadoUsuario.EN_DESCANSO -> Color(0xFFF59E0B)
            }
            Text(
                usuario.estadoUsuario.name.lowercase().replaceFirstChar { it.uppercase() },
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(if (usuario.activo) "En turno" else "Fuera", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

// Diálogos adaptados
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoUsuarioDialog(onDismiss: () -> Unit, onGuardar: (Usuario) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf(RolUsuario.MESERO) }
    var zonaId by remember { mutableStateOf("") }
    var estadoInicial by remember { mutableStateOf(EstadoUsuario.ACTIVO) }
    var expandedRol by remember { mutableStateOf(false) }
    // En un caso real obtendrías las zonas del ViewModel, aquí simplificamos con vacío
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Nuevo usuario", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                // Selección de rol
                ExposedDropdownMenuBox(expanded = expandedRol, onExpandedChange = { expandedRol = !expandedRol }) {
                    OutlinedTextField(value = rol.name, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    ExposedDropdownMenu(expanded = expandedRol, onDismissRequest = { expandedRol = false }) {
                        RolUsuario.values().forEach { r ->
                            DropdownMenuItem(text = { Text(r.name) }, onClick = { rol = r; expandedRol = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Estado inicial (chips)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoUsuario.values().forEach { estado ->
                        FilterChip(selected = estadoInicial == estado, onClick = { estadoInicial = estado }, label = { Text(estado.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    if (nombre.isNotBlank()) {
                        onGuardar(Usuario(nombre = nombre, rol = rol, activo = estadoInicial == EstadoUsuario.ACTIVO, estadoUsuario = estadoInicial, zonaId = zonaId, zonaAsignada = ""))
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Guardar usuario") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarUsuarioDialog(usuario: Usuario, onDismiss: () -> Unit, onGuardar: (Usuario) -> Unit) {
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var rol by remember { mutableStateOf(usuario.rol) }
    var zonaId by remember { mutableStateOf(usuario.zonaId) }
    var estado by remember { mutableStateOf(usuario.estadoUsuario) }
    var expandedRol by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Editar usuario", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expandedRol, onExpandedChange = { expandedRol = !expandedRol }) {
                    OutlinedTextField(value = rol.name, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    ExposedDropdownMenu(expanded = expandedRol, onDismissRequest = { expandedRol = false }) {
                        RolUsuario.values().forEach { r ->
                            DropdownMenuItem(text = { Text(r.name) }, onClick = { rol = r; expandedRol = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoUsuario.values().forEach { e ->
                        FilterChip(selected = estado == e, onClick = { estado = e }, label = { Text(e.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    onGuardar(usuario.copy(nombre = nombre, rol = rol, activo = estado == EstadoUsuario.ACTIVO, estadoUsuario = estado, zonaId = zonaId))
                }, modifier = Modifier.fillMaxWidth()) { Text("Actualizar usuario") }
            }
        }
    }
}