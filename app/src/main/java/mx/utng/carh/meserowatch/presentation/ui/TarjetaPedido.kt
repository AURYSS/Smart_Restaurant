package mx.utng.carh.meserowatch.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import mx.utng.carh.meserowatch.domain.model.Pedido

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