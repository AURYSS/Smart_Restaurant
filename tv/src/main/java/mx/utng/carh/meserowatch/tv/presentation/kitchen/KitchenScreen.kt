package mx.utng.carh.meserowatch.tv.presentation.kitchen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.*
import coil.compose.AsyncImage
import mx.utng.carh.meserowatch.tv.domain.model.EstadoPedido
import mx.utng.carh.meserowatch.tv.domain.model.Pedido
import mx.utng.carh.meserowatch.tv.presentation.kitchen.components.MainDashboard
import mx.utng.carh.meserowatch.tv.presentation.kitchen.components.OrderDetail
import android.os.Build
import androidx.compose.ui.draw.blur

@Composable
fun KitchenScreen(
    viewModel: KitchenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var pedidoEnfocado by remember { mutableStateOf<Pedido?>(null) }  // ← estado local para el foco

    val configuration = LocalConfiguration.current
    val margenH = (configuration.screenWidthDp * 0.05f).dp
    val margenV = (configuration.screenHeightDp * 0.027f).dp

    val urlPorDefecto = "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=900&auto=format&fit=crop"

    // Fondo: pedido seleccionado o, si no hay, el último enfocado
    val fondoPedido = uiState.pedidoSeleccionado ?: pedidoEnfocado

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        Crossfade(targetState = fondoPedido, animationSpec = tween(400), label = "fondo") { pedido ->
            if (pedido != null) {
                Box(Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = pedido.imagenUrl.ifEmpty { urlPorDefecto },
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
                contentColor = Color.White
            )
        ) {
            if (uiState.cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando pedidos...", color = Color.White, fontSize = 24.sp)
                }
            } else if (uiState.pedidoSeleccionado == null) {
                MainDashboard(
                    pedidos = uiState.pedidos.filter { it.estado == EstadoPedido.EN_PREPARACION },
                    entregados = uiState.pedidos.filter { it.estado == EstadoPedido.LISTO },
                    onSelectPedido = viewModel::seleccionarPedido,
                    onFocusChange = { pedido, focused ->   // ← callback para actualizar el fondo
                        if (focused) pedidoEnfocado = pedido
                    },
                    margenH = margenH,
                    margenV = margenV
                )
            } else {
                OrderDetail(
                    pedido = uiState.pedidoSeleccionado!!,
                    hayAnterior = uiState.indiceSeleccionado > 0,
                    haySiguiente = uiState.indiceSeleccionado < uiState.listaSeleccionada.size - 1,
                    onAnterior = viewModel::irAnterior,
                    onSiguiente = viewModel::irSiguiente,
                    onBack = viewModel::volver,
                    onCompletar = viewModel::completarPedido,
                    onEliminar = viewModel::eliminarPedido
                )
            }
        }
    }
}