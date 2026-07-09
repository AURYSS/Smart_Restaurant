package mx.utng.carh.meserowatch.tv.presentation.kitchen.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyListState

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