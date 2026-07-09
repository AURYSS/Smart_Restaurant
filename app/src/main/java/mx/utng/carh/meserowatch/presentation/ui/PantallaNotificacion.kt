package mx.utng.carh.meserowatch.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import mx.utng.carh.meserowatch.domain.model.Pedido

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
                label = { Text("Ver Pedidos", fontSize = 12.sp) },
                colors = ChipDefaults.chipColors(
                    backgroundColor = Color(0xFF444444)
                )
            )
        }
    }
}