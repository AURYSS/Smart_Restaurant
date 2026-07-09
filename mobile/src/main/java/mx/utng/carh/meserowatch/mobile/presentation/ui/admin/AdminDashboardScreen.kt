package mx.utng.carh.meserowatch.mobile.presentation.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.carh.meserowatch.mobile.presentation.viewmodel.AdminDashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: AdminDashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Hola, Admin", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(SimpleDateFormat("EEEE dd MMMM", Locale("es", "MX")).format(Date()), color = Color.Gray)

        Spacer(Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    title = "Ventas hoy",
                    value = "$${state.ventasHoy.toInt()}",
                    subtitle = "+12% vs ayer",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF1E293B)
                )
                DashboardCard(
                    title = "Pedidos",
                    value = state.totalPedidos.toString(),
                    subtitle = "${state.pedidosEnCurso} en curso",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF1E293B)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    title = "Personal activo",
                    value = state.personalActivo.toString(),
                    subtitle = "En turno",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF1E293B)
                )
                DashboardCard(
                    title = "Mesas ocupadas",
                    value = "${state.mesasOcupadas}/${state.mesasTotales}",
                    subtitle = "${((state.mesasOcupadas.toFloat() / state.mesasTotales.toFloat()) * 100).toInt()}% ocupación",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF1E293B)
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("ACCESO RÁPIDO", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAccessButton("Turnos", "turnos", onNavigateTo, Modifier.weight(1f))
                QuickAccessButton("Historial", "historial", onNavigateTo, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAccessButton("Mesas", "mesas", onNavigateTo, Modifier.weight(1f))
                QuickAccessButton("Menú", "menu", onNavigateTo, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAccessButton("Usuarios", "personal", onNavigateTo, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String, subtitle: String, modifier: Modifier, color: Color) {
    Surface(color = color, shape = RoundedCornerShape(16.dp), modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFF10B981), fontSize = 12.sp)
        }
    }
}

@Composable
fun QuickAccessButton(text: String, route: String, onNavigate: (String) -> Unit, modifier: Modifier) {
    Button(
        onClick = { onNavigate(route) },
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, color = Color.White)
    }
}