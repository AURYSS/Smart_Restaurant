package mx.utng.carh.meserowatch.tv.presentation.kitchen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun NavArrowButton(
    icon: ImageVector,
    contentDescription: String,
    habilitado: Boolean,
    onClick: () -> Unit,
    focusRequester: FocusRequester? = null
) {
    val circleShape = CircleShape

    if (habilitado) {
        Surface(
            onClick = onClick,
            shape = ClickableSurfaceDefaults.shape(circleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                pressedContainerColor = Color.Transparent
            ),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(border = BorderStroke(3.dp, Color.White), shape = circleShape)
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
            modifier = Modifier
                .size(72.dp)
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
    } else {
        Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(40.dp))
        }
    }
}