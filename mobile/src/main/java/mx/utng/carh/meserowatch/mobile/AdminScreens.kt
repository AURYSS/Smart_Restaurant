package mx.utng.carh.meserowatch.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(onNavigateTo: (String) -> Unit) {
    val databasePedidos = FirebaseDatabase.getInstance().getReference("pedidos")
    val databaseUsuarios = FirebaseDatabase.getInstance().getReference("usuarios")
    val databaseMesas = FirebaseDatabase.getInstance().getReference("mesas_config")

    var ventasHoy by remember { mutableStateOf(0.0) }
    var totalPedidos by remember { mutableStateOf(0) }
    var pedidosEnCurso by remember { mutableStateOf(0) }
    var personalActivo by remember { mutableStateOf(0) }
    var mesasOcupadasCount by remember { mutableStateOf(0) }
    var mesasTotalesCount by remember { mutableStateOf(12) }

    LaunchedEffect(Unit) {
        databasePedidos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var ventas = 0.0
                var enCurso = 0
                var total = 0
                snapshot.children.forEach { child ->
                    val estado = child.child("estado").value?.toString() ?: ""
                    val totalPedido = child.child("total").value.toString().toDoubleOrNull() ?: 0.0
                    total++
                    if (estado != "ENTREGADO" && estado != "CANCELADO") {
                        enCurso++
                    }
                    if (estado == "ENTREGADO") {
                        ventas += totalPedido
                    }
                }
                ventasHoy = ventas
                pedidosEnCurso = enCurso
                totalPedidos = total
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        databaseUsuarios.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var activos = 0
                snapshot.children.forEach { child ->
                    val activo = child.child("activo").value as? Boolean ?: false
                    if (activo) activos++
                }
                personalActivo = activos
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Escuchar mesas ocupadas
        databasePedidos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ocupadas = mutableSetOf<Int>()
                snapshot.children.forEach { child ->
                    val mesaId = child.child("mesa").value.toString().toDoubleOrNull()?.toInt() ?: 0
                    val estado = child.child("estado").value?.toString() ?: ""
                    if (estado != "ENTREGADO" && estado != "CANCELADO") {
                        ocupadas.add(mesaId)
                    }
                }
                mesasOcupadasCount = ocupadas.size
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

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

        // Grid de Resumen
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    title = "Ventas hoy",
                    value = "$${ventasHoy.toInt()}",
                    subtitle = "+12% vs ayer",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF1E293B)
                )
                DashboardCard(
                    title = "Pedidos",
                    value = totalPedidos.toString(),
                    subtitle = "$pedidosEnCurso en curso",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF1E293B)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    title = "Personal activo",
                    value = personalActivo.toString(),
                    subtitle = "En turno",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF1E293B)
                )
                DashboardCard(
                    title = "Mesas ocupadas",
                    value = "$mesasOcupadasCount/$mesasTotalesCount",
                    subtitle = "${((mesasOcupadasCount.toFloat()/mesasTotalesCount.toFloat())*100).toInt()}% ocupación",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF1E293B)
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("ACCESO RÁPIDO", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))

        // Botones de acceso rápido
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAccessButton("Turnos", Screen.Turnos.route, onNavigateTo, Modifier.weight(1f))
                QuickAccessButton("Historial", Screen.Historial.route, onNavigateTo, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAccessButton("Menú", Screen.Menu.route, onNavigateTo, Modifier.weight(1f))
                QuickAccessButton("Usuarios", Screen.Personal.route, onNavigateTo, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String, subtitle: String, modifier: Modifier, color: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
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

@Composable
fun TurnosPersonalScreen() {
    val database = FirebaseDatabase.getInstance().getReference("usuarios")
    var listaUsuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuarios = mutableListOf<Usuario>()
                snapshot.children.forEach { child ->
                    try {
                        val user = Usuario(
                            id = child.key ?: "",
                            nombre = child.child("nombre").value?.toString() ?: "",
                            rol = RolUsuario.valueOf(child.child("rol").value?.toString() ?: "MESERO"),
                            activo = child.child("activo").value as? Boolean ?: false,
                            zonaAsignada = child.child("zonaAsignada").value?.toString() ?: "",
                            fotoEmoji = child.child("fotoEmoji").value?.toString() ?: "👤",
                            estadoUsuario = EstadoUsuario.valueOf(child.child("estadoUsuario").value?.toString() ?: "ACTIVO")
                        )
                        usuarios.add(user)
                    } catch (e: Exception) {}
                }
                listaUsuarios = usuarios
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

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
            Text("Turnos del personal", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(" Nuevo")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(listaUsuarios) { usuario ->
                UsuarioTurnoItem(usuario)
            }
        }
    }
}

@Composable
fun UsuarioTurnoItem(usuario: Usuario) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(Color(0xFF312E81), CircleShape), contentAlignment = Alignment.Center) {
                Text(usuario.fotoEmoji)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(usuario.nombre, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${usuario.rol} · ${usuario.zonaAsignada}", color = Color.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("8:00 - 16:00", color = Color.Gray, fontSize = 12.sp) // Ejemplo estático
                val color = if (usuario.activo) Color(0xFF10B981) else Color(0xFFEF4444)
                Text(if (usuario.activo) "Activo" else "Inactivo", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HistorialPedidosScreen() {
    val database = FirebaseDatabase.getInstance().getReference("pedidos")
    var listaPedidos by remember { mutableStateOf<List<Pedido>>(emptyList()) }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pedidos = mutableListOf<Pedido>()
                snapshot.children.forEach { child ->
                    try {
                        val p = Pedido(
                            id = child.key ?: "",
                            mesa = child.child("mesa").value.toString().toDoubleOrNull()?.toInt() ?: 0,
                            estado = try { EstadoPedido.valueOf(child.child("estado").value.toString()) } catch(e: Exception) { EstadoPedido.PENDIENTE },
                            total = child.child("total").value.toString().toDoubleOrNull() ?: 0.0,
                            descripcion = child.child("descripcion").value?.toString() ?: ""
                        )
                        pedidos.add(p)
                    } catch (e: Exception) {}
                }
                listaPedidos = pedidos.reversed() // Más recientes primero
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp)
    ) {
        Text("Historial de pedidos", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Buscar mesa, platillo...", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) }
        )
        
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(listaPedidos.filter { it.mesa.toString().contains(searchText) || it.descripcion.contains(searchText, ignoreCase = true) }) { pedido ->
                HistorialItem(pedido)
            }
        }
    }
}

@Composable
fun HistorialItem(pedido: Pedido) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mesa ${pedido.mesa}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                val color = when(pedido.estado) {
                    EstadoPedido.ENTREGADO -> Color(0xFF10B981)
                    EstadoPedido.CANCELADO -> Color(0xFFEF4444)
                    else -> Color(0xFFF59E0B)
                }
                Text(pedido.estado.name, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text(pedido.descripcion, color = Color.Gray, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mesero: Luis C.", color = Color.Gray, fontSize = 12.sp)
                Text("$${pedido.total.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun MenuAdminScreen() {
    var mostrarNuevoPlatillo by remember { mutableStateOf(false) }
    var platilloAEditar by remember { mutableStateOf<Platillo?>(null) }
    var selectedCategory by remember { mutableStateOf("Todos") }
    val database = FirebaseDatabase.getInstance().getReference("menu")
    var listaPlatillos by remember { mutableStateOf<List<Platillo>>(emptyList()) }

    if (mostrarNuevoPlatillo) {
        NuevoPlatilloDialog(onDismiss = { mostrarNuevoPlatillo = false })
    }

    if (platilloAEditar != null) {
        EditarPlatilloDialog(platillo = platilloAEditar!!, onDismiss = { platilloAEditar = null })
    }

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val platillos = mutableListOf<Platillo>()
                snapshot.children.forEach { child ->
                    try {
                        val p = Platillo(
                            id = child.key ?: "",
                            nombre = child.child("nombre").value?.toString() ?: "",
                            precio = child.child("precio").value.toString().toDoubleOrNull() ?: 0.0,
                            categoria = child.child("categoria").value?.toString() ?: "Platos",
                            emoji = child.child("emoji").value?.toString() ?: "🍽️"
                        )
                        platillos.add(p)
                    } catch (e: Exception) {}
                }
                // Si la DB está vacía, mostrar los default para no ver pantalla en blanco
                if (platillos.isEmpty()) {
                    listaPlatillos = listOf(
                        Platillo("1", "Carne asada", 185.0, "Platos", true, emoji = "🥩"),
                        Platillo("2", "Tacos de pastor", 65.0, "Entradas", true, emoji = "🌮"),
                        Platillo("3", "Pozole rojo", 120.0, "Platos", true, emoji = "🍲"),
                        Platillo("4", "Guacamole", 55.0, "Entradas", true, emoji = "🥑")
                    )
                } else {
                    listaPlatillos = platillos
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

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
                Text("${listaPlatillos.size} platillos registrados", color = Color.Gray)
            }
            Button(
                onClick = { mostrarNuevoPlatillo = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(" Agregar")
            }
        }

        Spacer(Modifier.height(24.dp))

        // Categorías funcionales con SCROLL horizontal
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val categorias = listOf("Todos", "Entradas", "Platos", "Bebidas", "Postres", "Complementos", "Especiales")
            items(categorias) { cat ->
                FilterChip(cat, selectedCategory == cat) { selectedCategory = cat }
            }
        }

        Spacer(Modifier.height(16.dp))

        val platillosFiltrados = if (selectedCategory == "Todos") {
            listaPlatillos
        } else {
            listaPlatillos.filter { it.categoria == selectedCategory }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(platillosFiltrados) { platillo ->
                AdminPlatilloItem(
                    platillo = platillo,
                    onEdit = { platilloAEditar = platillo },
                    onDelete = { database.child(platillo.id).removeValue() }
                )
            }
        }
    }
}


@Composable
fun AdminPlatilloItem(platillo: Platillo, onEdit: () -> Unit, onDelete: () -> Unit) {
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
                Text(platillo.categoria, color = Color.Gray, fontSize = 14.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${platillo.precio.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                    AssistChip(
                        onClick = onEdit,
                        label = { Text("Editar", color = Color(0xFF6366F1)) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6366F1)) },
                        border = null,
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF312E81).copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalAdminScreen() {
    var mostrarNuevoUsuario by remember { mutableStateOf(false) }
    var mostrarNuevaZona by remember { mutableStateOf(false) }
    val database = FirebaseDatabase.getInstance().getReference("usuarios")
    var listaUsuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }

    if (mostrarNuevoUsuario) {
        NuevoUsuarioDialog(onDismiss = { mostrarNuevoUsuario = false })
    }
    
    if (mostrarNuevaZona) {
        NuevaZonaDialog(onDismiss = { mostrarNuevaZona = false })
    }

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuarios = mutableListOf<Usuario>()
                snapshot.children.forEach { child ->
                    try {
                        val user = Usuario(
                            id = child.key ?: "",
                            nombre = child.child("nombre").value?.toString() ?: "Sin nombre",
                            rol = RolUsuario.valueOf(child.child("rol").value?.toString() ?: "MESERO"),
                            activo = child.child("activo").value as? Boolean ?: true,
                            zonaAsignada = child.child("zonaAsignada").value?.toString() ?: "Sin zona",
                            fotoEmoji = child.child("fotoEmoji").value?.toString() ?: "👤",
                            estadoUsuario = EstadoUsuario.valueOf(child.child("estadoUsuario").value?.toString() ?: "ACTIVO")
                        )
                        usuarios.add(user)
                    } catch (e: Exception) {}
                }
                listaUsuarios = usuarios
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

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
                Text("${listaUsuarios.size} usuarios registrados", color = Color.Gray)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { mostrarNuevaZona = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Zonas")
                }
                Button(
                    onClick = { mostrarNuevoUsuario = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(" Nuevo")
                }
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

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(listaUsuarios) { usuario ->
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
            val statusColor = when(usuario.estadoUsuario) {
                EstadoUsuario.ACTIVO -> Color(0xFF10B981)
                EstadoUsuario.INACTIVO -> Color(0xFFEF4444)
                EstadoUsuario.EN_DESCANSO -> Color(0xFFF59E0B)
            }
            Text(
                usuario.estadoUsuario.name.lowercase().replaceFirstChar { it.uppercase() },
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
    var mostrarNuevaMesa by remember { mutableStateOf(false) }
    val databasePedidos = FirebaseDatabase.getInstance().getReference("pedidos")
    val databaseMesas = FirebaseDatabase.getInstance().getReference("mesas_config")
    
    var mesasOcupadas by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var listaMesasConfig by remember { mutableStateOf<List<Mesa>>(emptyList()) }

    if (mostrarNuevaMesa) {
        NuevaMesaDialog(onDismiss = { mostrarNuevaMesa = false })
    }

    LaunchedEffect(Unit) {
        // Escuchar pedidos para saber qué mesas están ocupadas
        databasePedidos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ocupadas = mutableSetOf<Int>()
                snapshot.children.forEach { child ->
                    val mesaId = child.child("mesa").value.toString().toDoubleOrNull()?.toInt() ?: 0
                    val estado = child.child("estado").value?.toString() ?: ""
                    if (estado != "ENTREGADO") {
                        ocupadas.add(mesaId)
                    }
                }
                mesasOcupadas = ocupadas
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Escuchar configuración de mesas para ver las nuevas agregadas
        databaseMesas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mesas = mutableListOf<Mesa>()
                snapshot.children.forEach { child ->
                    try {
                        val m = Mesa(
                            id = child.child("id").value.toString().toDoubleOrNull()?.toInt() ?: 0,
                            capacidad = child.child("capacidad").value.toString().toDoubleOrNull()?.toInt() ?: 4,
                            estado = EstadoMesa.LIBRE // El estado real se calcula abajo
                        )
                        if (m.id > 0) mesas.add(m)
                    } catch (e: Exception) {}
                }
                listaMesasConfig = mesas
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

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
                Text("${mesasOcupadas.size} ocupadas ahora", color = Color.Gray)
            }
            // Botón corregido
            Button(
                onClick = { mostrarNuevaMesa = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(" Nueva", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Leyenda Mesas
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusIndicator("Libre", Color(0xFF3B82F6))
            StatusIndicator("Ocupada", Color(0xFF10B981))
        }

        Spacer(Modifier.height(24.dp))

        // Combinar mesas por defecto (1-12) con las de la base de datos
        val idsMesasConfig = listaMesasConfig.map { it.id }.toSet()
        val mesasBase = (1..12).filter { !idsMesasConfig.contains(it) }.map { 
            Mesa(id = it, numero = it, estado = if (mesasOcupadas.contains(it)) EstadoMesa.OCUPADA else EstadoMesa.LIBRE)
        }
        val mesasNuevas = listaMesasConfig.map { 
            it.copy(estado = if (mesasOcupadas.contains(it.id)) EstadoMesa.OCUPADA else EstadoMesa.LIBRE)
        }
        
        val todasLasMesas = (mesasBase + mesasNuevas).sortedBy { it.id }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(todasLasMesas) { mesa ->
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
