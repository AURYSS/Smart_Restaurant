package mx.utng.carh.meserowatch.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuAdminScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Menú", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("24 platillos · 4 categorías", color = Color.Gray)
            }
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(" Agregar")
            }
        }

        Spacer(Modifier.height(24.dp))

        // Categorías
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip("Todos", true) { }
            FilterChip("Entradas", false) { }
            FilterChip("Platos", false) { }
            FilterChip("Bebidas", false) { }
        }

        Spacer(Modifier.height(16.dp))

        val platillos = listOf(
            Platillo("1", "Carne asada", 185.0, "Platos", true, emoji = "🥩"),
            Platillo("2", "Tacos de pastor", 65.0, "Entradas", true, emoji = "🌮"),
            Platillo("3", "Pozole rojo", 120.0, "Platos", true, emoji = "🍲"),
            Platillo("4", "Guacamole", 55.0, "Entradas", true, emoji = "🥑"),
            Platillo("5", "Agua de jamaica", 35.0, "Bebidas", true, emoji = "🥤")
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(platillos) { platillo ->
                AdminPlatilloItem(platillo)
            }
        }
    }
}

@Composable
fun AdminPlatilloItem(platillo: Platillo) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(platillo.emoji, fontSize = 28.sp)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(platillo.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Ingredientes, detalles...", color = Color.Gray, fontSize = 14.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${platillo.precio.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                AssistChip(
                    onClick = { },
                    label = { Text("Editar", color = Color(0xFF6366F1)) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6366F1)) },
                    border = null,
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF312E81).copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
fun PersonalAdminScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Usuarios", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("9 usuarios · 6 activos ahora", color = Color.Gray)
            }
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(" Nuevo")
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = "",
            onValueChange = { },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            placeholder = { Text("Buscar usuario...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) }
        )

        Spacer(Modifier.height(24.dp))

        val usuarios = listOf(
            Usuario("1", "Luis Cervantes", RolUsuario.MESERO, true, "Zona A", "LC"),
            Usuario("2", "María Ruiz", RolUsuario.CHEF, true, "Cocina", "MR"),
            Usuario("3", "Jorge Peña", RolUsuario.MESERO, false, "Zona B", "JP"),
            Usuario("4", "Sofía Alvarado", RolUsuario.CAJERO, true, "Caja", "SA")
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(usuarios) { usuario ->
                UsuarioItem(usuario)
            }
        }
    }
}

@Composable
fun UsuarioItem(usuario: Usuario) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(Color(0xFF1E293B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(usuario.fotoEmoji, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(usuario.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("${usuario.rol} · ${usuario.zonaAsignada}", color = Color.Gray, fontSize = 14.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            val statusColor = if (usuario.activo) Color(0xFF10B981) else Color(0xFFF59E0B)
            Text(
                if (usuario.activo) "Activo" else "Inactivo",
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(if (usuario.activo) "En turno" else "Fuera de turno", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun EstadoMesasScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Mesas", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("12 mesas · 7 ocupadas · 2 reservadas", color = Color.Gray)
            }
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(" Nueva")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Leyenda Mesas
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusIndicator("Libre", Color(0xFF3B82F6))
            StatusIndicator("Ocupada", Color(0xFF10B981))
            StatusIndicator("Reservada", Color(0xFFF59E0B))
        }

        Spacer(Modifier.height(24.dp))

        val mesas = (1..12).map { 
            Mesa(it, if (it % 3 == 0) EstadoMesa.LIBRE else if (it % 4 == 0) EstadoMesa.RESERVADA else EstadoMesa.OCUPADA) 
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mesas) { mesa ->
                MesaAdminItem(mesa)
            }
        }
    }
}

@Composable
fun MesaAdminItem(mesa: Mesa) {
    val color = when (mesa.estado) {
        EstadoMesa.LIBRE -> Color(0xFF3B82F6)
        EstadoMesa.OCUPADA -> Color(0xFF10B981)
        EstadoMesa.RESERVADA -> Color(0xFFF59E0B)
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .border(2.dp, color, RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(mesa.id.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(mesa.estado.toString().lowercase(), fontSize = 12.sp, color = Color.Gray)
        }
    }
}
