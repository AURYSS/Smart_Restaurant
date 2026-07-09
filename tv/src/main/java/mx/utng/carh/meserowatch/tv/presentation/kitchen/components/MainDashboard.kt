package mx.utng.carh.meserowatch.tv.presentation.kitchen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import mx.utng.carh.meserowatch.tv.domain.model.Pedido

@Composable
fun MainDashboard(
    pedidos: List<Pedido>,
    entregados: List<Pedido>,
    onSelectPedido: (Pedido, List<Pedido>) -> Unit,
    onFocusChange: (Pedido, Boolean) -> Unit,  // ← nuevo parámetro
    margenH: Dp,
    margenV: Dp
) {
    val primerCardFocusRequester = remember { FocusRequester() }
    var enfocoInicial by remember { mutableStateOf(false) }

    LaunchedEffect(pedidos) {
        if (!enfocoInicial && pedidos.isNotEmpty()) {
            primerCardFocusRequester.requestFocus()
            enfocoInicial = true
        }
    }

    TvLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = margenH, vertical = margenV),
        verticalArrangement = Arrangement.spacedBy(40.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        pivotOffsets = PivotOffsets(parentFraction = 0.15f)
    ) {
        item {
            Column {
                Text("Pedidos", fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Normal)
                Spacer(Modifier.height(12.dp))
                if (pedidos.isEmpty()) {
                    Text("No hay pedidos pendientes", color = Color.Gray, fontSize = 24.sp)
                } else {
                    val estadoPedidosRow = rememberTvLazyListState()
                    TvLazyRow(
                        state = estadoPedidosRow,
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        modifier = Modifier.fadingEdgeHorizontal(estadoPedidosRow, edgeWidth = 80.dp)
                    ) {
                        itemsIndexed(pedidos) { index, pedido ->
                            OrderCard(
                                pedido = pedido,
                                onSelect = { onSelectPedido(it, pedidos) },
                                onFocusChange = onFocusChange,   // ← pasar callback
                                focusRequester = if (index == 0) primerCardFocusRequester else null
                            )
                        }
                    }
                }
            }
        }

        item {
            Column {
                Text("Entregados", fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Normal)
                Spacer(Modifier.height(12.dp))
                if (entregados.isEmpty()) {
                    Text("No hay pedidos listos", color = Color.Gray, fontSize = 24.sp)
                } else {
                    val estadoEntregadosRow = rememberTvLazyListState()
                    TvLazyRow(
                        state = estadoEntregadosRow,
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        modifier = Modifier.fadingEdgeHorizontal(estadoEntregadosRow, edgeWidth = 80.dp)
                    ) {
                        items(entregados) { pedido ->
                            OrderCard(
                                pedido = pedido,
                                onSelect = { onSelectPedido(it, entregados) },
                                onFocusChange = onFocusChange   // ← pasar callback
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}