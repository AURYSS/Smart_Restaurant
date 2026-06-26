package mx.utng.carh.meserowatch.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoPedidoScreen() {
    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todas") }
    var mesaSeleccionadaParaPedido by remember { mutableStateOf<Int?>(null) }

    if (mesaSeleccionadaParaPedido != null) {
        ElegirPlatillosScreen(mesaId = mesaSeleccionadaParaPedido!!) {
            mesaSeleccionadaParaPedido = null
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(24.dp)
        ) {
            Text(
                "Nuevo pedido",
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Selecciona una mesa para comenzar",
                color = Color.Gray,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(24.dp))

            // Barra de búsqueda
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = { Text("Buscar mesa...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFF1E293B),
                unfocusedIndicatorColor = Color(0xFF1E293B),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
            )

            Spacer(Modifier.height(16.dp))

            // Filtros
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip("Todas", selectedFilter == "Todas") { selectedFilter = "Todas" }
                FilterChip("Libres", selectedFilter == "Libres") { selectedFilter = "Libres" }
                FilterChip("Mis mesas", selectedFilter == "Mis mesas") { selectedFilter = "Mis mesas" }
            }

            Spacer(Modifier.height(16.dp))

            // Leyenda
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatusIndicator("Libre", Color(0xFF3B82F6))
                StatusIndicator("Ocupada", Color(0xFF10B981))
                StatusIndicator("Mis mesas", Color(0xFF6366F1))
            }

            Spacer(Modifier.height(24.dp))

            Text("Zona A", color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            val mesas = listOf(
                Mesa(1, EstadoMesa.OCUPADA, 2, zona = "A", meseroAsignado = "Yo"),
                Mesa(2, EstadoMesa.OCUPADA, 2, zona = "A"),
                Mesa(3, EstadoMesa.LIBRE, 4, zona = "A"),
                Mesa(4, EstadoMesa.OCUPADA, 4, zona = "A", meseroAsignado = "Yo"),
                Mesa(5, EstadoMesa.LIBRE, 2, zona = "A"),
                Mesa(6, EstadoMesa.OCUPADA, 5, zona = "A")
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(mesas) { mesa ->
                    MesaItem(mesa) {
                        mesaSeleccionadaParaPedido = mesa.id
                    }
                }
            }
        }
    }
}

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

@Composable
fun StatusIndicator(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(Modifier.width(6.dp))
        Text(text, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun MesaItem(mesa: Mesa, onClick: () -> Unit) {
    val borderColor = when {
        mesa.meseroAsignado == "Yo" -> Color(0xFF6366F1)
        mesa.estado == EstadoMesa.OCUPADA -> Color(0xFF10B981)
        else -> Color(0xFF3B82F6)
    }

    val backgroundColor = borderColor.copy(alpha = 0.1f)

    Box(
        modifier = Modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                mesa.id.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                if (mesa.meseroAsignado == "Yo") "Mi mesa" else if (mesa.estado == EstadoMesa.OCUPADA) "Ocupada" else "Libre",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                "${mesa.capacidad} lug.",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
