package mx.utng.carh.meserowatch.mobile.presentation.ui.turnos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.carh.meserowatch.mobile.domain.model.EstadoUsuario
import mx.utng.carh.meserowatch.mobile.domain.model.Usuario
import mx.utng.carh.meserowatch.mobile.presentation.ui.personal.EditarUsuarioDialog
import mx.utng.carh.meserowatch.mobile.presentation.ui.personal.NuevoUsuarioDialog
import mx.utng.carh.meserowatch.mobile.presentation.viewmodel.UsuariosViewModel

@Composable
fun TurnosPersonalScreen(viewModel: UsuariosViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    // Diálogo para agregar usuario
    if (state.showNuevoDialog) {
        NuevoUsuarioDialog(
            onDismiss = { viewModel.hideNuevoUsuarioDialog() },
            onGuardar = { usuario -> viewModel.addUsuario(usuario) }
        )
    }
    // Diálogo para editar usuario (cuando corresponda)
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
            Text("Turnos", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Button(
                onClick = { viewModel.showNuevoUsuarioDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Nuevo", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.usuarios) { usuario ->
                UsuarioTurnoItem(usuario) {
                    viewModel.editUsuario(usuario)
                }
            }
        }
    }
}

@Composable
fun UsuarioTurnoItem(usuario: Usuario, onEdit: () -> Unit) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onEdit() }
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(Color(0xFF312E81), CircleShape), contentAlignment = Alignment.Center) {
                Text(usuario.fotoEmoji)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(usuario.nombre, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${usuario.rol} · ${usuario.zonaAsignada}", color = Color.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Turno rotativo", color = Color.Gray, fontSize = 12.sp)
                val (statusText, statusColor) = when (usuario.estadoUsuario) {
                    EstadoUsuario.ACTIVO -> "Activo" to Color(0xFF10B981)
                    EstadoUsuario.INACTIVO -> "Inactivo" to Color(0xFFEF4444)
                    EstadoUsuario.EN_DESCANSO -> "En descanso" to Color(0xFFF59E0B)
                }
                Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}