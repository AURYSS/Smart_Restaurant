package mx.utng.carh.meserowatch.mobile.presentation.ui.nuevopedido

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.carh.meserowatch.mobile.domain.model.EstadoMesa
import mx.utng.carh.meserowatch.mobile.domain.model.Mesa
import mx.utng.carh.meserowatch.mobile.domain.model.Platillo
import mx.utng.carh.meserowatch.mobile.domain.model.PlatilloSeleccionado
import mx.utng.carh.meserowatch.mobile.presentation.viewmodel.NuevoPedidoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoPedidoScreen(
    onNavigateToAlertas: () -> Unit,
    viewModel: NuevoPedidoViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Efecto para navegar cuando se envió el pedido
    LaunchedEffect(state.pedidoEnviado) {
        if (state.pedidoEnviado) onNavigateToAlertas()
    }

    if (state.mesaSeleccionada != null && state.mostrandoResumen) {
        // Pantalla Resumen
        ResumenPedidoScreen(
            mesaId = state.mesaSeleccionada!!,
            viewModel = viewModel,
            state = state
        )
    } else if (state.mesaSeleccionada != null) {
        // Pantalla Elegir platillos
        ElegirPlatillosScreen(
            mesaId = state.mesaSeleccionada!!,
            viewModel = viewModel,
            state = state
        )
    } else {
        // Pantalla Seleccionar mesa
        SeleccionarMesaScreen(viewModel = viewModel, state = state)
    }
}

@Composable
fun SeleccionarMesaScreen(viewModel: NuevoPedidoViewModel, state: mx.utng.carh.meserowatch.mobile.presentation.viewmodel.NuevoPedidoState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp)
    ) {
        Text("Nuevo pedido", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Text("Selecciona una mesa para comenzar", color = Color.Gray, fontSize = 16.sp)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.searchMesa,
            onValueChange = viewModel::onSearchMesaChanged,
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip("Todas", state.filterMesa == "Todas") { viewModel.onFilterMesaChanged("Todas") }
            FilterChip("Libres", state.filterMesa == "Libres") { viewModel.onFilterMesaChanged("Libres") }
            FilterChip("Ocupadas", state.filterMesa == "Ocupadas") { viewModel.onFilterMesaChanged("Ocupadas") }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusIndicator("Libre", Color(0xFF3B82F6))
            StatusIndicator("Ocupada", Color(0xFF10B981))
        }

        Spacer(Modifier.height(24.dp))

        Text("Todas las mesas", color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        val mesasFiltradas = state.mesas.filter { mesa ->
            (state.filterMesa == "Todas" ||
                    (state.filterMesa == "Libres" && mesa.estado == EstadoMesa.LIBRE) ||
                    (state.filterMesa == "Ocupadas" && mesa.estado == EstadoMesa.OCUPADA)) &&
                    (state.searchMesa.isEmpty() || mesa.id.toString().contains(state.searchMesa))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(mesasFiltradas) { mesa ->
                val isOcupada = mesa.estado == EstadoMesa.OCUPADA
                MesaItem(mesa = mesa, onClick = {
                    if (!isOcupada) viewModel.seleccionarMesa(mesa.id)
                })
            }
        }
    }
}

@Composable
fun MesaItem(mesa: Mesa, onClick: () -> Unit) {
    val isOcupada = mesa.estado == EstadoMesa.OCUPADA
    val borderColor = if (isOcupada) Color(0xFF10B981) else Color(0xFF3B82F6)
    val backgroundColor = borderColor.copy(alpha = if (isOcupada) 0.05f else 0.1f)

    Box(
        modifier = Modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = !isOcupada) { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                mesa.id.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOcupada) Color.Gray else Color.White
            )
            Text(
                if (isOcupada) "Ocupada" else "Libre",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElegirPlatillosScreen(
    mesaId: Int,
    viewModel: NuevoPedidoViewModel,
    state: mx.utng.carh.meserowatch.mobile.presentation.viewmodel.NuevoPedidoState
) {
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
                Text(state.seleccionados.values.sum().toString(), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(24.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val categorias = listOf("Todos", "Entradas", "Platos", "Postres", "Bebidas", "Complementos", "Especiales")
            items(categorias) { cat ->
                FilterChip(cat, state.filterCategoria == cat) { viewModel.onCategoriaChanged(cat) }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.searchPlatillo,
            onValueChange = viewModel::onSearchPlatilloChanged,
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

        val platillosFiltrados = state.platillos.filter {
            (state.filterCategoria == "Todos" || it.categoria == state.filterCategoria) &&
                    (state.searchPlatillo.isEmpty() || it.nombre.contains(state.searchPlatillo, ignoreCase = true))
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(platillosFiltrados) { platillo ->
                val count = state.seleccionados[platillo.id] ?: 0
                val isSelected = count > 0

                Surface(
                    color = Color(0xFF1E293B).copy(alpha = if (platillo.disponible) 1f else 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = platillo.disponible) {
                            viewModel.togglePlatillo(platillo.id)
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

        if (state.seleccionados.isNotEmpty()) {
            val totalItems = state.seleccionados.values.sum()
            val totalPrice = state.platillos
                .filter { state.seleccionados.containsKey(it.id) }
                .sumOf { it.precio * (state.seleccionados[it.id] ?: 0) }

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
                onClick = { viewModel.irAResumen() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Ver pedido", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenPedidoScreen(
    mesaId: Int,
    viewModel: NuevoPedidoViewModel,
    state: mx.utng.carh.meserowatch.mobile.presentation.viewmodel.NuevoPedidoState
) {
    // Convertir seleccionados a lista de PlatilloSeleccionado con cantidades iniciales
    val platillosSeleccionados = state.platillos
        .filter { state.seleccionados.containsKey(it.id) }
        .map { p -> PlatilloSeleccionado(p.id, p.nombre, p.precio, state.seleccionados[p.id]!!) }

    // Mapa de notas por instancia: key = "id#índice", para gestionar notas por cada unidad
    val notasPorInstancia = remember { mutableStateMapOf<String, String>() }

    // Inicializar notas vacías
    LaunchedEffect(platillosSeleccionados) {
        platillosSeleccionados.forEach { item ->
            repeat(item.cantidad) { i ->
                val key = "${item.id}#$i"
                if (!notasPorInstancia.containsKey(key)) {
                    notasPorInstancia[key] = ""
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
            items(platillosSeleccionados.size) { index ->
                val item = platillosSeleccionados[index]
                val notasDeEstePlatillo = (0 until item.cantidad).map { i ->
                    notasPorInstancia["${item.id}#$i"] ?: ""
                }
                CardResumenItem(
                    item = item,
                    notas = notasDeEstePlatillo,
                    onCantidadChange = { nuevaCantidad ->
                        viewModel.setCantidad(item.id, nuevaCantidad)
                    },
                    onNotaChange = { instIndex, nuevaNota ->
                        notasPorInstancia["${item.id}#$instIndex"] = nuevaNota
                    }
                )
            }
        }

        val total = platillosSeleccionados.sumOf { it.precio * it.cantidad }

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
                // Construir la lista de items para Firebase (cada unidad con su nota)
                val itemsFirebase = mutableListOf<Map<String, String>>()
                platillosSeleccionados.forEach { item ->
                    repeat(item.cantidad) { i ->
                        val nota = notasPorInstancia["${item.id}#$i"] ?: ""
                        itemsFirebase.add(mapOf("descripcion" to item.nombre, "nota" to nota))
                    }
                }
                val descResumen = platillosSeleccionados.joinToString(", ") { "${it.cantidad}x ${it.nombre}" }
                viewModel.enviarPedido(
                    mesaId = mesaId,
                    platillosConNotas = platillosSeleccionados.map { it.copy(nota = "") }, // Las notas van en itemsFirebase
                    itemsFirebase = itemsFirebase,
                    total = total,
                    descripcion = descResumen
                )
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
                var notaTexto by remember(notas.getOrElse(i) { "" }) { mutableStateOf(notas.getOrElse(i) { "" }) }

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