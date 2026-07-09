package mx.utng.carh.meserowatch.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import mx.utng.carh.meserowatch.domain.model.EstadoPedido
import mx.utng.carh.meserowatch.domain.model.Pedido

@Composable
fun PantallaLista(
    pedidos: List<Pedido>,
    onConfirmar: (String) -> Unit
) {
    val listos = pedidos.count { it.estado == EstadoPedido.LISTO }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 32.dp)
    ) {
        item {
            ListHeader {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "PEDIDOS LISTOS",
                        color = Color.Cyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "$listos mesas esperando",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        items(pedidos.filter { it.estado == EstadoPedido.LISTO }) { pedido ->
            TarjetaPedido(pedido = pedido, onConfirmar = onConfirmar)
        }
    }
}