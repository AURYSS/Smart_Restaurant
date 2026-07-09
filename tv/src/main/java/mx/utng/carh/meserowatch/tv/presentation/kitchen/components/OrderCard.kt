package mx.utng.carh.meserowatch.tv.presentation.kitchen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import mx.utng.carh.meserowatch.tv.domain.model.Pedido

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun OrderCard(
    pedido: Pedido,
    onSelect: (Pedido) -> Unit,
    onFocusChange: (Pedido, Boolean) -> Unit = { _, _ -> },  // ← nuevo parámetro con valor por defecto
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
            .onFocusChanged { onFocusChange(pedido, it.isFocused) }   // ← notificar cambios de foco
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