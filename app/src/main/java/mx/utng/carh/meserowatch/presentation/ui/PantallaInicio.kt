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