package mx.utng.carh.meserowatch.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElegirPlatillosScreen(mesaId: Int, onBack: () -> Unit) {
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Entradas") }
    val database = FirebaseDatabase.getInstance().getReference("pedidos")

    // Lista de platillos seleccionados (ID -> Cantidad)
    val seleccionados = remember { mutableStateMapOf<String, Int>() }

    val platillos = listOf(
        Platillo("1", "Guacamole", 55.0, "Entradas", true, emoji = "🥑"),
        Platillo("2", "Tacos de pastor", 65.0, "Entradas", true, emoji = "🌮"),
        Platillo("3", "Sopa del día", 75.0, "Entradas", false, emoji = "🍲"),
        Platillo("4", "Ensalada César", 90.0, "Entradas", true, emoji = "🥗"),
        Platillo("5", "Flautas gratinadas", 85.0, "Entradas", false, emoji = "🌯")
    )

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
                Text("Elegir platillos", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Mesa $mesaId · 6 lugares", color = Color.Gray, fontSize = 16.sp)
            }
            Box(
                modifier = Modifier.size(36.dp).background(Color(0xFF1E293B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(seleccionados.values.sum().toString(), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Categorías
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Entradas", "Platos", "Postres", "Bebidas").forEach { cat ->
                FilterChip(cat, selectedCategory == cat) { selectedCategory = cat }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Barra de búsqueda
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            placeholder = { Text("Buscar platillo...", color = Color.Gray) },
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

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(platillos) { platillo ->
                val count = seleccionados[platillo.id] ?: 0
                val isSelected = count > 0

                Surface(
                    color = Color(0xFF1E293B).copy(alpha = if (platillo.disponible) 1f else 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = platillo.disponible) {
                            if (isSelected) seleccionados.remove(platillo.id) else seleccionados[platillo.id] = 1
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(platillo.emoji, fontSize = 32.sp)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(platillo.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("$${platillo.precio.toInt()}", color = Color.Gray, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        if (isSelected) Color(0xFF10B981) else Color(0xFF0F172A),
                                        RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (platillo.disponible) "Disponible" else "No disponible",
                                color = if (platillo.disponible) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Barra inferior de confirmación
        if (seleccionados.isNotEmpty()) {
            val totalItems = seleccionados.values.sum()
            val totalPrice = platillos.filter { seleccionados.containsKey(it.id) }.sumOf { it.precio * (seleccionados[it.id] ?: 0) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$totalItems platillo seleccionado", color = Color.Gray, fontSize = 16.sp)
                Text("$${totalPrice.toInt()}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val itemsList = platillos.filter { seleccionados.containsKey(it.id) }.map { p ->
                        PlatilloSeleccionado(p.id, p.nombre, p.precio, seleccionados[p.id] ?: 1)
                    }
                    val descripcion = itemsList.joinToString("\n") { "${it.cantidad} orden ${it.nombre.lowercase()}" }
                    
                    val nuevoPedidoKey = database.push().key ?: "p"
                    val nuevoPedido = mapOf(
                        "id" to nuevoPedidoKey,
                        "mesa" to mesaId,
                        "descripcion" to descripcion,
                        "nota" to "Sin notas",
                        "estado" to "EN_PREPARACION",
                        "timestamp" to ServerValue.TIMESTAMP
                    )
                    database.child(nuevoPedidoKey).setValue(nuevoPedido).addOnSuccessListener {
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Ver pedido", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
