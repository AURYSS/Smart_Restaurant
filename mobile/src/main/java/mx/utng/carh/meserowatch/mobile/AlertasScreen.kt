package mx.utng.carh.meserowatch.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun AlertasScreen() {
    val database = FirebaseDatabase.getInstance().getReference("pedidos")
    var pedidos by remember { mutableStateOf<List<Pedido>>(emptyList()) }
    var pedidoSeleccionadoDetalle by remember { mutableStateOf<Pedido?>(null) }

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Pedido>()
                snapshot.children.forEach { child ->
                    try {
                        val estadoStr = child.child("estado").value?.toString() ?: "EN_PREPARACION"
                        val p = Pedido(
                            id = child.key ?: "",
                            mesa = child.child("mesa").value.toString().toDoubleOrNull()?.toInt() ?: 0,
                            descripcion = child.child("descripcion").value?.toString() ?: "",
                            estado = try { EstadoPedido.valueOf(estadoStr) } catch (e: Exception) { EstadoPedido.EN_PREPARACION },
                            timestamp = child.child("timestamp").value.toString().toLongOrNull() ?: 0L,
                            nota = child.child("nota").value?.toString() ?: ""
                        )
                        lista.add(p)
                    } catch (e: Exception) { }
                }
                pedidos = lista
            }
            override fun onCancelled(error: DatabaseError) { }
        })
    }

    if (pedidoSeleccionadoDetalle != null) {
        DetallePedidoDialog(pedido = pedidoSeleccionadoDetalle!!) {
            pedidoSeleccionadoDetalle = null
        }
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
                Text("Pedidos activos", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Actualizado hace un momento", color = Color.Gray)
            }
            Box(
                modifier = Modifier.background(Color(0xFF1E293B), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("${pedidos.size} pedidos", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        var selectedFilter by remember { mutableStateOf("Todos") }
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filtros = listOf("Todos", "Listos", "En cocina", "Cancelados")
            items(filtros) { filtro ->
                FilterChip(filtro, selectedFilter == filtro) { selectedFilter = filtro }
            }
        }

        Spacer(Modifier.height(24.dp))

        val pedidosFiltrados = when(selectedFilter) {
            "Listos" -> pedidos.filter { it.estado == EstadoPedido.LISTO }
            "En cocina" -> pedidos.filter { it.estado == EstadoPedido.EN_PREPARACION }
            "Cancelados" -> pedidos.filter { it.estado == EstadoPedido.CANCELADO }
            else -> pedidos
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(pedidosFiltrados) { pedido ->
                AlertaItem(pedido) {
                    pedidoSeleccionadoDetalle = pedido
                }
            }
        }
    }
}

@Composable
fun DetallePedidoDialog(pedido: Pedido, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("Detalle Mesa ${pedido.mesa}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                Text("Resumen:", color = Color.Gray, fontSize = 14.sp)
                Text(pedido.descripcion, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                
                Spacer(Modifier.height(16.dp))
                Text("Estado:", color = Color.Gray, fontSize = 14.sp)
                
                val statusColor = when(pedido.estado) {
                    EstadoPedido.LISTO -> Color(0xFF10B981)
                    EstadoPedido.CANCELADO -> Color(0xFFEF4444)
                    EstadoPedido.EN_PREPARACION -> Color(0xFFF59E0B)
                    else -> Color.White
                }
                
                Text(
                    pedido.estado.name.lowercase().replaceFirstChar { it.uppercase() }, 
                    color = statusColor, 
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
fun AlertaItem(pedido: Pedido, onClick: () -> Unit) {
    val (statusText, statusColor) = when(pedido.estado) {
        EstadoPedido.LISTO -> "Listo" to Color(0xFF10B981)
        EstadoPedido.CANCELADO -> "Cancelado" to Color(0xFFEF4444)
        EstadoPedido.EN_PREPARACION -> "Cocina" to Color(0xFFF59E0B)
        else -> pedido.estado.name to Color.Gray
    }

    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Mesa ${pedido.mesa}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(pedido.descripcion, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
            }
            Box(
                modifier = Modifier.background(
                    statusColor.copy(0.1f),
                    RoundedCornerShape(8.dp)
                ).padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
