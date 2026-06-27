package mx.utng.carh.meserowatch.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.onFocusChanged
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Border
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.Dp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.tv.foundation.lazy.list.TvLazyListState
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.delay

@Composable
fun KitchenScreen() {
    val database = FirebaseDatabase.getInstance().getReference("pedidos")
    var pedidos by remember { mutableStateOf<List<Pedido>>(emptyList()) }
    var pedidoSeleccionado by remember { mutableStateOf<Pedido?>(null) }
    var listaSeleccionada by remember { mutableStateOf<List<Pedido>>(emptyList()) }
    var indiceSeleccionado by remember { mutableStateOf(0) }
    var pedidoEnfocado by remember { mutableStateOf<Pedido?>(null) }
    var cargando by remember { mutableStateOf(true) }

    val urlPorDefecto = "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=900&auto=format&fit=crop"

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cargando = false
                val lista = mutableListOf<Pedido>()
                if (snapshot.exists()) {
                    snapshot.children.forEach { child ->
                        try {
                            val id = child.key ?: ""
                            val mesa = child.child("mesa").value?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                            val desc = child.child("descripcion").value?.toString() ?: ""
                            val nota = child.child("nota").value?.toString() ?: ""
                            val estadoStr = child.child("estado").value?.toString() ?: "EN_PREPARACION"
                            val time = child.child("timestamp").value?.toString()?.toLongOrNull() ?: 0L
                            val imagen = child.child("imagenUrl").value?.toString() ?: ""
                            val listaItems = mutableListOf<ItemPedido>()
                            child.child("items").children.forEach { itemSnap ->
                                val descItem = itemSnap.child("descripcion").value?.toString() ?: ""
                                val notaItem = itemSnap.child("nota").value?.toString() ?: ""
                                if (descItem.isNotEmpty()) {
                                    listaItems.add(ItemPedido(descripcion = descItem, nota = notaItem))
                                }
                            }

                            lista.add(Pedido(
                                id = id, mesa = mesa, descripcion = desc, nota = nota,
                                estado = try { EstadoPedido.valueOf(estadoStr) } catch (e: Exception) { EstadoPedido.EN_PREPARACION },
                                timestamp = time, imagenUrl = imagen,
                                items = listaItems
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
            }
        })
    }

    val configuration = LocalConfiguration.current
    val margenH = (configuration.screenWidthDp * 0.05f).dp
    val margenV = (configuration.screenHeightDp * 0.027f).dp

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        val mesaParaFondo = pedidoSeleccionado ?: pedidoEnfocado

        Crossfade(targetState = mesaParaFondo, animationSpec = tween(400), label = "fondo") { mesa ->
            if (mesa != null) {
                Box(Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = mesa.imagenUrl.ifEmpty { urlPorDefecto },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(60.dp) else Modifier)
                    )
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)))
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            colors = SurfaceDefaults.colors(
                containerColor = Color.Transparent,
                contentColor = Color.White // NUEVO — fuerza blanco en vez del cálculo automático
            )
        ) {
            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando pedidos...", color = Color.White, fontSize = 24.sp)
                }
            } else if (pedidoSeleccionado == null) {
                MainDashboard(
                    pedidos = pedidos.filter { it.estado == EstadoPedido.EN_PREPARACION },
                    entregados = pedidos.filter { it.estado == EstadoPedido.LISTO },
                    onSelectPedido = { pedido, lista ->
                        pedidoSeleccionado = pedido
                        listaSeleccionada = lista
                        indiceSeleccionado = lista.indexOf(pedido)
                    },
                    onFocusChange = { pedido, focused -> if (focused) pedidoEnfocado = pedido },
                    margenH = margenH,
                    margenV = margenV
                )
            } else {
                OrderDetail(
                    pedido = pedidoSeleccionado!!,
                    hayAnterior = indiceSeleccionado > 0,
                    haySiguiente = indiceSeleccionado < listaSeleccionada.size - 1,
                    onAnterior = {
                        if (indiceSeleccionado > 0) {
                            indiceSeleccionado--
                            pedidoSeleccionado = listaSeleccionada[indiceSeleccionado]
                        }
                    },
                    onSiguiente = {
                        if (indiceSeleccionado < listaSeleccionada.size - 1) {
                            indiceSeleccionado++
                            pedidoSeleccionado = listaSeleccionada[indiceSeleccionado]
                        }
                    },
                    onBack = { pedidoSeleccionado = null },
                    onCompletar = {
                        database.child(it.id).child("estado").setValue("LISTO")
                        database.child(it.id).child("timestamp").setValue(ServerValue.TIMESTAMP)
                        pedidoSeleccionado = null
                    },
                    onEliminar = {
                        // Cambiamos a CANCELADO en lugar de eliminar físicamente para que el mobile se entere y libere la mesa
                        database.child(it.id).child("estado").setValue("CANCELADO")
                        pedidoSeleccionado = null
                    }
                )
            }
        }
    }
}




@Composable
fun MainDashboard(
    pedidos: List<Pedido>,
    entregados: List<Pedido>,
    onSelectPedido: (Pedido, List<Pedido>) -> Unit,
    onFocusChange: (Pedido, Boolean) -> Unit,
    margenH: Dp,
    margenV: Dp
) {
    val primerCardFocusRequester = remember { FocusRequester() }
    var enfocoInicial by remember { mutableStateOf(false) }

    // Solo enfoca la primera vez que llegan datos, no en cada actualización de Firebase
    LaunchedEffect(pedidos) {
        if (!enfocoInicial && pedidos.isNotEmpty()) {
            primerCardFocusRequester.requestFocus()
            enfocoInicial = true
        }
    }

    TvLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = margenH, vertical = margenV),
        verticalArrangement = Arrangement.spacedBy(40.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        pivotOffsets = PivotOffsets(parentFraction = 0.15f)
    ) {
        item {
            Column {
                Text("Pedidos", fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Normal)
                Spacer(Modifier.height(12.dp))
                if (pedidos.isEmpty()) {
                    Text("No hay pedidos pendientes", color = Color.Gray, fontSize = 24.sp)
                } else {
                    val estadoPedidosRow = rememberTvLazyListState()
                    TvLazyRow(
                        state = estadoPedidosRow,
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        modifier = Modifier.fadingEdgeHorizontal(estadoPedidosRow, edgeWidth = 80.dp)
                    ) {
                        itemsIndexed(pedidos) { index, pedido ->
                            OrderCard(
                                pedido = pedido,
                                onSelect = { onSelectPedido(it, pedidos) }, // NUEVO
                                onFocusChange = onFocusChange,
                                focusRequester = if (index == 0) primerCardFocusRequester else null
                            )
                        }
                    }
                }
            }
        }

        item {
            Column {
                Text("Entregados", fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Normal)
                Spacer(Modifier.height(12.dp))
                if (entregados.isEmpty()) {
                    Text("No hay pedidos listos", color = Color.Gray, fontSize = 24.sp)
                } else {
                    val estadoEntregadosRow = rememberTvLazyListState()
                    TvLazyRow(
                        state = estadoEntregadosRow,
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        modifier = Modifier.fadingEdgeHorizontal(estadoEntregadosRow, edgeWidth = 80.dp)
                    ) {
                        items(entregados) { pedido ->
                            OrderCard(pedido, onSelect = { onSelectPedido(it, entregados) }, onFocusChange)
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun OrderCard(
    pedido: Pedido,
    onSelect: (Pedido) -> Unit,
    onFocusChange: (Pedido, Boolean) -> Unit,
    focusRequester: FocusRequester? = null
) {
    val cardShape = RoundedCornerShape(24.dp)

    Surface(
        onClick = { onSelect(pedido) },
        shape = ClickableSurfaceDefaults.shape(cardShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.DarkGray,
            focusedContainerColor = Color.DarkGray,
            pressedContainerColor = Color.DarkGray
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(4.dp, Color.White), shape = cardShape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        modifier = Modifier
            .width(360.dp)
            .height(220.dp)
            .onFocusChanged { onFocusChange(pedido, it.isFocused) }
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
    ) {
        Box(modifier = Modifier.clip(cardShape)) {
            AsyncImage(
                model = pedido.imagenUrl.ifEmpty {
                    "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=360&h=220&auto=format&fit=crop"
                },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
            )
            Text(
                text = "Mesa ${pedido.mesa}",
                modifier = Modifier.align(Alignment.BottomStart).padding(24.dp),
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun OrderDetail(
    pedido: Pedido,
    hayAnterior: Boolean,
    haySiguiente: Boolean,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onBack: () -> Unit,
    onCompletar: (Pedido) -> Unit,
    onEliminar: (Pedido) -> Unit
) {
    BackHandler(onBack = onBack)

    var mostrarDialogoCompletar by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    val items = remember(pedido.id) {
        pedido.items.ifEmpty { listOf(ItemPedido(descripcion = pedido.descripcion, nota = pedido.nota)) }
    }
    var indiceItemActivo by remember(pedido.id) { mutableStateOf(0) }

    // Ciclo automático cada 10 segundos entre los items del pedido
    LaunchedEffect(pedido.id, items.size) {
        if (items.size <= 1) return@LaunchedEffect
        while (true) {
            delay(10_000)
            indiceItemActivo = (indiceItemActivo + 1) % items.size
        }
    }

    val focusCompletar = remember(pedido.id) { FocusRequester() }
    LaunchedEffect(pedido.id) { focusCompletar.requestFocus() }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavArrowButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Mesa anterior",
            habilitado = hayAnterior,
            onClick = onAnterior
        )

        Spacer(Modifier.width(32.dp))

        Row(
            modifier = Modifier.weight(1f).fillMaxHeight(), // NUEVO: fillMaxHeight()
            verticalAlignment = Alignment.Top
        ) {

            Column(modifier = Modifier.width(420.dp).fillMaxHeight()) { // NUEVO: fillMaxHeight()
                Text("Mesa ${pedido.mesa}", fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Normal)
                Spacer(Modifier.height(16.dp))

                AsyncImage(
                    model = pedido.imagenUrl.ifEmpty {
                        "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=420&h=300&auto=format&fit=crop"
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // antes: .height(280.dp) — ahora ocupa solo el espacio disponible
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(20.dp))

                if (pedido.estado == EstadoPedido.EN_PREPARACION) {
                    ActionButton(
                        texto = "Completar pedido",
                        icono = Icons.Default.Check,
                        focusRequester = focusCompletar,
                        onClick = { mostrarDialogoCompletar = true }
                    )
                    Spacer(Modifier.height(14.dp))
                    ActionButton(
                        texto = "Eliminar pedido",
                        icono = Icons.Default.Delete,
                        onClick = { mostrarDialogoEliminar = true }
                    )
                } else {
                    val diff = System.currentTimeMillis() - pedido.timestamp
                    val minutos = TimeUnit.MILLISECONDS.toMinutes(diff)
                    Text("Terminada hace $minutos minutos", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.width(48.dp))

            Column(modifier = Modifier.weight(1f)) {
                items.forEachIndexed { index, item ->
                    Text(
                        text = item.descripcion,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = if (index == indiceItemActivo) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                Spacer(Modifier.weight(1f)) // empuja la nota hacia abajo, como en tu boceto

                Text("Nota:", color = Color.White, fontSize = 22.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    items[indiceItemActivo].nota.ifEmpty { "Sin notas" },
                    color = Color.White,
                    fontSize = 22.sp
                )
            }
        }

        Spacer(Modifier.width(32.dp))

        NavArrowButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Mesa siguiente",
            habilitado = haySiguiente,
            onClick = onSiguiente
        )
    }

    if (mostrarDialogoCompletar) {
        DialogoConfirmacion(
            titulo = "¿Estás seguro que quieres completar el pedido?",
            textoConfirmar = "Aceptar",
            esDestructivo = false,
            onConfirmar = {
                mostrarDialogoCompletar = false
                onCompletar(pedido)
            },
            onCancelar = { mostrarDialogoCompletar = false }
        )
    }

    if (mostrarDialogoEliminar) {
        DialogoConfirmacion(
            titulo = "¿Estás seguro que quieres eliminar el pedido?",
            textoConfirmar = "Eliminar",
            esDestructivo = true,
            onConfirmar = {
                mostrarDialogoEliminar = false
                onEliminar(pedido)
            },
            onCancelar = { mostrarDialogoEliminar = false }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun NavArrowButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    habilitado: Boolean,
    onClick: () -> Unit
) {
    val circleShape = CircleShape

    if (habilitado) {
        Surface(
            onClick = onClick,
            shape = ClickableSurfaceDefaults.shape(circleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                pressedContainerColor = Color.Transparent
            ),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(border = BorderStroke(3.dp, Color.White), shape = circleShape)
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
    } else {
        // Espacio invisible — mantiene el layout simétrico aunque no haya mesa anterior/siguiente
        Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(40.dp))
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ActionButton(
    texto: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(32.dp)

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF616161),
            contentColor = Color.White,
            focusedContainerColor = Color.White,
            focusedContentColor = Color.Black
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(3.dp, Color.White), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(texto, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DialogoConfirmacion(
    titulo: String,
    textoConfirmar: String,
    esDestructivo: Boolean,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    val focusCancelar = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusCancelar.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            colors = SurfaceDefaults.colors(containerColor = Color(0xFF2B2B2B)),
            modifier = Modifier.width(560.dp)
        ) {
            Column(modifier = Modifier.padding(40.dp)) {
                Text(titulo, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(36.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {

                    Surface(
                        onClick = onCancelar,
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color(0xFF616161),
                            contentColor = Color.White,
                            focusedContainerColor = Color.White,
                            focusedContentColor = Color.Black
                        ),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(border = BorderStroke(3.dp, Color.White), shape = RoundedCornerShape(24.dp))
                        ),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .focusRequester(focusCancelar)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Cancelar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        onClick = onConfirmar,
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (esDestructivo) Color(0xFFD32F2F) else Color.White,
                            contentColor = if (esDestructivo) Color.White else Color.Black,
                            focusedContainerColor = if (esDestructivo) Color(0xFFEF5350) else Color.White,
                            focusedContentColor = if (esDestructivo) Color.White else Color.Black
                        ),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(border = BorderStroke(3.dp, Color.White), shape = RoundedCornerShape(24.dp))
                        ),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(textoConfirmar, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.fadingEdgeHorizontal(
    listState: TvLazyListState,
    edgeWidth: Dp = 80.dp
): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val edgePx = edgeWidth.toPx()

        val mostrarFundidoIzquierdo = listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 0
        val mostrarFundidoDerecho = listState.canScrollForward

        if (mostrarFundidoIzquierdo) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startX = 0f,
                    endX = edgePx
                ),
                blendMode = BlendMode.DstIn
            )
        }
        if (mostrarFundidoDerecho) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startX = size.width - edgePx,
                    endX = size.width
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }