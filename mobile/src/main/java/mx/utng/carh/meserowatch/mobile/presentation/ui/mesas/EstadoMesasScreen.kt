package mx.utng.carh.meserowatch.mobile.presentation.ui.mesas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.carh.meserowatch.mobile.domain.model.EstadoMesa
import mx.utng.carh.meserowatch.mobile.domain.model.Mesa
import mx.utng.carh.meserowatch.mobile.presentation.viewmodel.EstadoMesasViewModel

@Composable
fun EstadoMesasScreen(viewModel: EstadoMesasViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    if (state.showNuevaMesaDialog) {
        NuevaMesaDialog(
            onDismiss = { viewModel.hideNuevaMesaDialog() },
            onGuardar = { mesa -> viewModel.addMesa(mesa) }
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
                Text("Mesas", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${state.mesas.count { it.estado == EstadoMesa.OCUPADA }} ocupadas ahora", color = Color.Gray)
            }
            Button(
                onClick = { viewModel.showNuevaMesaDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(" Nueva", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusIndicator("Libre", Color(0xFF3B82F6))
            StatusIndicator("Ocupada", Color(0xFF10B981))
        }

        Spacer(Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.mesas) { mesa ->
                MesaAdminItem(mesa)
            }
        }
    }
}

@Composable
fun MesaAdminItem(mesa: Mesa) {
    val color = when (mesa.estado) {
        EstadoMesa.LIBRE -> Color(0xFF3B82F6)
        EstadoMesa.OCUPADA -> Color(0xFF10B981)
        EstadoMesa.RESERVADA -> Color(0xFFF59E0B)
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .border(2.dp, color, RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(mesa.id.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(mesa.estado.name.lowercase(), fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun StatusIndicator(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(Modifier.width(6.dp))
        Text(text, color = Color.Gray, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaMesaDialog(onDismiss: () -> Unit, onGuardar: (Mesa) -> Unit) {
    var numeroMesa by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Nueva mesa", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = numeroMesa, onValueChange = { numeroMesa = it }, label = { Text("Número") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = capacidad, onValueChange = { capacidad = it }, label = { Text("Capacidad") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    val num = numeroMesa.toIntOrNull() ?: 0
                    if (num > 0) {
                        onGuardar(Mesa(id = num, capacidad = capacidad.toIntOrNull() ?: 4))
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Guardar mesa") }
            }
        }
    }
}