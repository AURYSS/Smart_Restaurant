package mx.utng.carh.meserowatch.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text

@Composable
fun PantallaNotificacion(
    pedido: Pedido,
    onVerLista: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text("🔔", fontSize = 28.sp)

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Mesa ${pedido.mesa} lista",
                color = Color(0xFF7B61FF),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Listo para entregar",
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Chip(
                onClick = onVerLista,
                label = {
                    Text(
                        "Ver lista de pedidos",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                },
                colors = ChipDefaults.chipColors(
                    backgroundColor = Color(0xFF2A2A2A)
                )
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF00C853), shape = CircleShape)
                )
                Spacer(Modifier.width(4.dp))
                Text("sensor activo", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun PantallaLista(
    pedidos: List<Pedido>,
    onConfirmar: (Int) -> Unit
) {
    val listos = pedidos.count { it.estado == EstadoPedido.LISTO }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 32.dp)
    ) {
        item {
            ListHeader {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Pedidos listos",
                        color = Color(0xFF7B61FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "$listos mesas",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        items(pedidos) { pedido ->
            TarjetaPedido(pedido = pedido, onConfirmar = onConfirmar)
        }
    }
}

@Composable
fun TarjetaPedido(pedido: Pedido, onConfirmar: (Int) -> Unit) {
    val esListo = pedido.estado == EstadoPedido.LISTO

    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = { if (esListo) onConfirmar(pedido.id) },
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Mesa ${pedido.mesa}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        pedido.descripcion,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
                if (esListo) {
                    Text(
                        "listo",
                        color = Color(0xFF00C853),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = if (esListo) Color(0xFF1A3A1A) else Color(0xFF2A2A2A)
        )
    )
}