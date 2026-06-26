package mx.utng.carh.meserowatch.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoPlatilloDialog(onDismiss: () -> Unit) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(16.dp).fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFFF59E0B))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Nuevo platillo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Agregar al menú con ingredientes y precio", color = Color.Gray, fontSize = 14.sp)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text("ID (asignado automáticamente)", color = Color.Gray, fontSize = 12.sp)
            Box(modifier = Modifier.fillMaxWidth().height(48.dp).background(Color(0xFF0F172A), RoundedCornerShape(8.dp)).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                Text("🔒 Se genera al guardar", color = Color.Gray)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text("Nombre del platillo *", color = Color.White, fontSize = 14.sp)
            OutlinedTextField(
                value = "", onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej. Tacos de pastor", color = Color.Gray) }
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
                Spacer(Modifier.width(16.dp))
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("✓ Guardar platillo")
                }
            }
        }
    }
}
