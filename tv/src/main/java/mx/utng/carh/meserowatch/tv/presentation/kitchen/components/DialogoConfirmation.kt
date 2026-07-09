package mx.utng.carh.meserowatch.tv.presentation.kitchen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DialogoConfirmacion(
    titulo: String,
    textoConfirmar: String,
    esDestructivo: Boolean,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    val focusCancelar = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusCancelar.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            colors = SurfaceDefaults.colors(containerColor = Color(0xFF2B2B2B)),
            modifier = Modifier.width(560.dp)
        ) {
            Column(modifier = Modifier.padding(40.dp)) {
                Text(titulo, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(36.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {

                    Surface(
                        onClick = onCancelar,
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color(0xFF616161),
                            contentColor = Color.White,
                            focusedContainerColor = Color.White,
                            focusedContentColor = Color.Black
                        ),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(border = BorderStroke(3.dp, Color.White), shape = RoundedCornerShape(24.dp))
                        ),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .focusRequester(focusCancelar)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Cancelar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        onClick = onConfirmar,
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (esDestructivo) Color(0xFFD32F2F) else Color.White,
                            contentColor = if (esDestructivo) Color.White else Color.Black,
                            focusedContainerColor = if (esDestructivo) Color(0xFFEF5350) else Color.White,
                            focusedContentColor = if (esDestructivo) Color.White else Color.Black
                        ),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(border = BorderStroke(3.dp, Color.White), shape = RoundedCornerShape(24.dp))
                        ),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(textoConfirmar, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}