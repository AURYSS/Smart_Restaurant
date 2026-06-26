package mx.utng.carh.meserowatch.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit

@Composable
fun KitchenScreen() {
    val database = FirebaseDatabase.getInstance().getReference("pedidos")
    var pedidos by remember { mutableStateOf<List<Pedido>>(emptyList()) }
    var pedidoSeleccionado by remember { mutableStateOf<Pedido?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cargando = false
                val lista = mutableListOf<Pedido>()
                // Log para verificar si llega algo
                android.util.Log.d("CocinaTV", "Snapshot recibido. Existe: ${snapshot.exists()}, Hijos: ${snapshot.childrenCount}")
                
                if (snapshot.exists()) {
                    snapshot.children.forEach { child ->
                        try {
                            val id = child.key ?: ""
                            // Lectura ultra-robusta usando String como puente
                            val mesa = child.child("mesa").value?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                            val desc = child.child("descripcion").value?.toString() ?: ""
                            val nota = child.child("nota").value?.toString() ?: ""
                            val estadoStr = child.child("estado").value?.toString() ?: "EN_PREPARACION"
                            val time = child.child("timestamp").value?.toString()?.toLongOrNull() ?: 0L

                            lista.add(Pedido(
                                id = id,
                                mesa = mesa,
                                descripcion = desc,
                                nota = nota,
                                estado = try { EstadoPedido.valueOf(estadoStr) } catch(e: Exception) { EstadoPedido.EN_PREPARACION },
                                timestamp = time
                            ))
                        } catch (e: Exception) {
                            android.util.Log.e("CocinaTV", "Error en pedido ${child.key}: ${e.message}")
                        }
                    }
                }
                pedidos = lista
            }
            override fun onCancelled(error: DatabaseError) {
                cargando = false
                android.util.Log.e("CocinaTV", "Error de Firebase: ${error.message}")
            }
        })
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = SurfaceDefaults.colors(containerColor = Color(0xFF121212))
    ) {
        if (cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cargando pedidos...", color = Color.White, fontSize = 24.sp)
            }
        } else if (pedidoSeleccionado == null) {
            MainDashboard(
                pedidos = pedidos.filter { it.estado == EstadoPedido.EN_PREPARACION },
                entregados = pedidos.filter { it.estado == EstadoPedido.LISTO },
                onSelect = { pedidoSeleccionado = it }
            )
        } else {
            OrderDetail(
                pedido = pedidoSeleccionado!!,
                onBack = { pedidoSeleccionado = null },
                onCompletar = {
                    database.child(it.id).child("estado").setValue("LISTO")
                    database.child(it.id).child("timestamp").setValue(ServerValue.TIMESTAMP)
                    pedidoSeleccionado = null
                },
                onEliminar = {
                    database.child(it.id).removeValue()
                    pedidoSeleccionado = null
                }
            )
        }
    }
}





@Composable
fun MainDashboard(
    pedidos: List<Pedido>,
    entregados: List<Pedido>,
    onSelect: (Pedido) -> Unit
) {
    Column(modifier = Modifier.padding(48.dp)) {
        Text("Pedidos", fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Normal)
        Spacer(Modifier.height(24.dp))
        
        if (pedidos.isEmpty()) {
            Text("No hay pedidos pendientes", color = Color.Gray, fontSize = 24.sp)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                items(pedidos) { pedido ->
                    OrderCard(pedido, onSelect)
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        Text("Entregados", fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Normal)
        Spacer(Modifier.height(24.dp))
        if (entregados.isEmpty()) {
            Text("No hay pedidos listos", color = Color.Gray, fontSize = 24.sp)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                items(entregados) { pedido ->
                    OrderCard(pedido, onSelect)
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun OrderCard(pedido: Pedido, onSelect: (Pedido) -> Unit) {
    Surface(
        onClick = { onSelect(pedido) },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
        modifier = Modifier
            .width(360.dp)
            .height(220.dp)
    ) {
        Box {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=360&h=220&auto=format&fit=crop",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                        )
                    )
            )
            Text(
                text = "Mesa ${pedido.mesa}",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun OrderDetail(
    pedido: Pedido,
    onBack: () -> Unit,
    onCompletar: (Pedido) -> Unit,
    onEliminar: (Pedido) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón Atrás (Doble flecha)
        IconButton(onClick = onBack, modifier = Modifier.size(80.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Atrás", 
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(Modifier.width(24.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Top) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=400&h=300&auto=format&fit=crop",
                    contentDescription = null,
                    modifier = Modifier
                        .width(400.dp)
                        .height(300.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(Modifier.width(32.dp))
                
                Column {
                    Text(
                        "Mesa ${pedido.mesa}", 
                        fontSize = 42.sp, 
                        color = Color.White, 
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        pedido.descripcion, 
                        fontSize = 24.sp, 
                        color = Color.White,
                        lineHeight = 32.sp
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.width(400.dp)) {
                    if (pedido.estado == EstadoPedido.EN_PREPARACION) {
                        Button(
                            onClick = { onCompletar(pedido) },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = ButtonDefaults.shape(RoundedCornerShape(32.dp)),
                            colors = ButtonDefaults.colors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text("Completar pedido", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { onEliminar(pedido) },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = ButtonDefaults.shape(RoundedCornerShape(32.dp)),
                            colors = ButtonDefaults.colors(
                                containerColor = Color(0xFF616161),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text("Eliminar pedido", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        val diff = System.currentTimeMillis() - pedido.timestamp
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                        Text(
                            "Terminada hace\n$minutes minutos", 
                            fontSize = 36.sp, 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold,
                            lineHeight = 44.sp
                        )
                    }
                }
                
                Spacer(Modifier.width(48.dp))
                
                Column {
                    Text("Nota:", color = Color.White, fontSize = 24.sp)
                    Text(
                        pedido.nota.ifEmpty { "Sin notas" }, 
                        color = Color.White, 
                        fontSize = 22.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(24.dp))

        // Botón Siguiente (Doble flecha)
        IconButton(onClick = { }, modifier = Modifier.size(80.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Siguiente", 
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}
