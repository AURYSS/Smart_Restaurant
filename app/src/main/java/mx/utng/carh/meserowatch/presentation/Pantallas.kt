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
fun PantallaInicio(
    cantidadListos: Int,
    onVerLista: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2C2C)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "MeseroWatch",
                color = Color.Cyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                if (cantidadListos > 0) "¡Tienes pedidos!" else "Sin pedidos",
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (cantidadListos > 0) Color.Green else Color.White
            )

            if (cantidadListos > 0) {
                Text(
                    "$cantidadListos mesas listas",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }

            Spacer(Modifier.height(12.dp))

            Chip(
                onClick = onVerLista,
                label = { Text("Ver Mesas", color = Color.White) },
                colors = ChipDefaults.chipColors(
                    backgroundColor = Color(0xFF444444)
                )
            )
        }
    }
}

@Composable
fun PantallaNotificacion(
    pedido: Pedido,
    onVerLista: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2C2C)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text("🔔", fontSize = 32.sp)

            Spacer(Modifier.height(6.dp))

            Text(
                text = "MESA ${pedido.mesa}",
                color = Color(0xFFBB86FC),
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "¡LISTO AHORA!",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Chip(
                onClick = onVerLista,
                label = {
                    Text("Ver Pedidos", fontSize = 12.sp)
                },
                colors = ChipDefaults.chipColors(
                    backgroundColor = Color(0xFF444444)
                )
            )
        }
    }
}

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

@Composable
fun TarjetaPedido(pedido: Pedido, onConfirmar: (String) -> Unit) {
    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = { onConfirmar(pedido.id) },
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
                        fontSize = 14.sp
                    )
                    Text(
                        pedido.descripcion.take(15) + "...",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
                Text(
                    "OK",
                    color = Color.Green,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = Color(0xFF1E3A1E)
        )
    )
}
