package mx.utng.carh.meserowatch.tv.presentation.kitchen.components

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import mx.utng.carh.meserowatch.tv.domain.model.EstadoPedido
import mx.utng.carh.meserowatch.tv.domain.model.ItemPedido
import mx.utng.carh.meserowatch.tv.domain.model.Pedido
import java.util.concurrent.TimeUnit

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

    LaunchedEffect(pedido.id, items.size) {
        if (items.size <= 1) return@LaunchedEffect
        while (true) {
            delay(10_000)
            indiceItemActivo = (indiceItemActivo + 1) % items.size
        }
    }

    val focusCompletar = remember(pedido.id) { FocusRequester() }
    val focusAnterior = remember(pedido.id) { FocusRequester() }
    val focusSiguiente = remember(pedido.id) { FocusRequester() }

    LaunchedEffect(pedido.id, pedido.estado, hayAnterior, haySiguiente) {
        when {
            pedido.estado == EstadoPedido.EN_PREPARACION -> focusCompletar.requestFocus()
            haySiguiente -> focusSiguiente.requestFocus()
            hayAnterior -> focusAnterior.requestFocus()
        }
    }

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
            onClick = onAnterior,
            focusRequester = focusAnterior
        )

        Spacer(Modifier.width(32.dp))

        Row(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalAlignment = Alignment.Top
        ) {

            Column(modifier = Modifier.width(420.dp).fillMaxHeight()) {
                Text("Mesa ${pedido.mesa}", fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Normal)
                Spacer(Modifier.height(16.dp))

                AsyncImage(
                    model = pedido.imagenUrl.ifEmpty {
                        "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=420&h=300&auto=format&fit=crop"
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
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
                    Text(
                        "Terminada hace $minutos minutos",
                        fontSize = 22.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
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

                Spacer(Modifier.weight(1f))

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
            onClick = onSiguiente,
            focusRequester = focusSiguiente
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