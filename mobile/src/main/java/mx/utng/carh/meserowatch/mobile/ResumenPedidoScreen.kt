package mx.utng.carh.meserowatch.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenPedidoScreen(mesaId: Int, platillosSeleccionados: List<PlatilloSeleccionado>, onBack: () -> Unit) {
    val database = FirebaseDatabase.getInstance().getReference("pedidos")
    val items = remember { mutableStateListOf(*platillosSeleccionados.toTypedArray()) }
    // Mapa para notas por instancia (PlatilloId -> List<Nota>)
    val notasPorInstancia = remember { mutableStateMapOf<String, MutableList<String>>() }

    // Inicializar notas si no existen
    LaunchedEffect(items) {
        items.forEach { item ->
            if (!notasPorInstancia.containsKey(item.id)) {
                notasPorInstancia[item.id] = mutableListOf<String>().apply {
                    repeat(item.cantidad) { add("") }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp)
    ) {
        Text("Resumen del pedido", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Text("Mesa $mesaId · Luis Cervantes", color = Color.Gray, fontSize = 16.sp)

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(items) { index, item ->
                CardResumenItem(
                    item = item,
                    notas = notasPorInstancia[item.id] ?: mutableListOf(),
                    onCantidadChange = { nuevaCantidad ->
                        if (nuevaCantidad <= 0) {
                            items.removeAt(index)
                            notasPorInstancia.remove(item.id)
                        } else {
                            val diff = nuevaCantidad - item.cantidad
                            val listaNotas = notasPorInstancia[item.id] ?: mutableListOf()
                            if (diff > 0) {
                                repeat(diff) { listaNotas.add("") }
                            } else if (diff < 0) {
                                repeat(-diff) { if (listaNotas.isNotEmpty()) listaNotas.removeAt(listaNotas.size - 1) }
                            }
                            items[index] = item.copy(cantidad = nuevaCantidad)
                        }
                    },
                    onNotaChange = { instIndex, nuevaNota ->
                        notasPorInstancia[item.id]?.set(instIndex, nuevaNota)
                    }
                )
            }
        }

        val total = items.sumOf { it.precio * it.cantidad }
        
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total del pedido", color = Color.Gray, fontSize = 16.sp)
            Text("$${total.toInt()}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val nuevoPedidoKey = database.push().key ?: "p"
                
                // Construir descripción detallada con notas
                val descBuilder = StringBuilder()
                items.forEach { item ->
                    val notas = notasPorInstancia[item.id] ?: emptyList<String>()
                    descBuilder.append("${item.cantidad} orden ${item.nombre.lowercase()}")
                    if (notas.any { it.isNotEmpty() }) {
                        descBuilder.append(" (")
                        descBuilder.append(notas.filter { it.isNotEmpty() }.joinToString(", "))
                        descBuilder.append(")")
                    }
                    descBuilder.append(" ")
                }

                val nuevoPedido = hashMapOf(
                    "id" to nuevoPedidoKey,
                    "mesa" to mesaId.toLong(),
                    "descripcion" to descBuilder.toString().trim(),
                    "nota" to "Notas por instancia incluidas en descripción",
                    "estado" to "EN_PREPARACION",
                    "timestamp" to ServerValue.TIMESTAMP
                )
                database.child(nuevoPedidoKey).setValue(nuevoPedido).addOnCompleteListener {
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Enviar a cocina", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardResumenItem(
    item: PlatilloSeleccionado,
    notas: List<String>,
    onCantidadChange: (Int) -> Unit,
    onNotaChange: (Int, String) -> Unit
) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(32.dp).background(Color(0xFF0F172A), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Text("🥑", fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text(item.nombre, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("$${(item.precio * item.cantidad).toInt()}", color = Color.White)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                IconButton(onClick = { onCantidadChange(item.cantidad - 1) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = Color.Gray)
                }
                Text(item.cantidad.toString(), color = Color.White, modifier = Modifier.padding(horizontal = 12.dp))
                IconButton(onClick = { onCantidadChange(item.cantidad + 1) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                }
                Spacer(Modifier.width(8.dp))
                Text("órdenes", color = Color.Gray, fontSize = 12.sp)
            }

            repeat(item.cantidad) { i ->
                var notaTexto by remember(item.id, i) { mutableStateOf(notas.getOrElse(i) { "" }) }
                
                Text("Orden ${i + 1}", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                OutlinedTextField(
                    value = notaTexto,
                    onValueChange = { 
                        notaTexto = it
                        onNotaChange(i, it) 
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    placeholder = { Text("Agregar nota...", color = Color.DarkGray, fontSize = 12.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF3B82F6)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.White)
                )
            }
        }
    }
}
