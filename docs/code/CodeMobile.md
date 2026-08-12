# Código del módulo móvil

## [Regresar al README principal](/README.md)

Este documento describe el propósito de cada archivo del módulo `mobile`, organizado según la arquitectura por capas (Clean Architecture): `presentation`, `domain` y `data`.

---

## Raíz del módulo

### `MainActivity.kt`
Actividad principal y punto de entrada de la aplicación. Se encarga de instanciar el contenido de Compose, aplicar el tema general (`MaterialTheme`) y arrancar el grafo de navegación (`AppNavigation`) donde inicia el flujo con la pantalla de login.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = Color(0xFF0F172A)) {
                    AppNavigation()
                }
            }
        }
    }
}
```

### `SessionManager.kt`
Objeto singleton que mantiene el estado de la sesión activa en memoria durante la ejecución de la app: el usuario autenticado (`currentUser`) y si tiene rol de administrador (`isAdmin`). Expone funciones para iniciar sesión como admin, iniciar sesión como usuario normal y cerrar sesión.

```kotlin
object SessionManager {
    var currentUser by mutableStateOf<Usuario?>(null)
    var isAdmin by mutableStateOf(false)

    fun loginAsAdmin() {
        currentUser = Usuario(id = "admin", nombre = "Administrador", rol = RolUsuario.ADMIN)
        isAdmin = true
    }

    fun loginAsUser(usuario: Usuario) {
        currentUser = usuario
        isAdmin = usuario.rol == RolUsuario.ADMIN
    }

    fun logout() {
        currentUser = null
        isAdmin = false
    }
}
```

---

## Presentation / Navigation

### `presentation/navigation/AppNavigation.kt`
Define el grafo de navegación de toda la app usando `NavHost` y `NavController`. Declara las rutas (`Screen`) disponibles, controla el flujo entre pantallas (login, registro, dashboard, mesas, pedidos, menú, personal, zonas, turnos, alertas, historial) y muestra la barra de navegación inferior/lateral según el rol del usuario en sesión.

```kotlin
object SessionManager {
    var currentUser by mutableStateOf<Usuario?>(null)
    var isAdmin by mutableStateOf(false)

    fun loginAsAdmin() {
        currentUser = Usuario(id = "admin", nombre = "Administrador", rol = RolUsuario.ADMIN)
        isAdmin = true
    }

    fun loginAsUser(usuario: Usuario) {
        currentUser = usuario
        isAdmin = usuario.rol == RolUsuario.ADMIN
    }

    fun logout() {
        currentUser = null
        isAdmin = false
    }
}
```

---

## Presentation / Dependency Injection

### `presentation/di/AppModule.kt`
Módulo de inyección de dependencias manual. Crea e inicializa (de forma perezosa con `by lazy`) las fuentes de datos (`DataSource`) y las expone envueltas en sus respectivas implementaciones de repositorio (`Repository`), para que los ViewModels accedan a ellas sin acoplarse directamente a Firebase.

```kotlin
object AppModule {
    // Data sources
    private val authDataSource by lazy { AuthDataSource() }
    private val menuDataSource by lazy { MenuDataSource() }
    private val pedidoDataSource by lazy { PedidoDataSource() }
    private val mesaDataSource by lazy { MesaDataSource() }
    private val zonaDataSource by lazy { ZonaDataSource() }
    private val usuarioDataSource by lazy { UsuarioDataSource() }

    // Repositories
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(authDataSource) }
    val menuRepository: MenuRepository by lazy { MenuRepositoryImpl(menuDataSource) }
    val pedidoRepository: PedidoRepository by lazy { PedidoRepositoryImpl(pedidoDataSource) }
    val mesaRepository: MesaRepository by lazy { MesaRepositoryImpl(mesaDataSource) }
    val zonaRepository: ZonaRepository by lazy { ZonaRepositoryImpl(zonaDataSource) }
    val usuarioRepository: UsuarioRepository by lazy { UsuarioRepositoryImpl(usuarioDataSource) }
}
```

---

## Presentation / UI (Pantallas Compose)

### `presentation/ui/login/LoginScreen.kt`
Pantalla de inicio de sesión. Contiene los campos de usuario y contraseña, el botón de acceso, el enlace a registro y el manejo visual de errores de autenticación.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) onLoginSuccess()
    }

    val hasMinLength = state.password.length >= 8
    val hasNumber = state.password.any { it.isDigit() }
    val hasUppercase = state.password.any { it.isUpperCase() }
    val hasSpecialChar = state.password.any { !it.isLetterOrDigit() }
    val allRulesMet = hasMinLength && hasNumber && hasUppercase && hasSpecialChar

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1200&auto=format&fit=crop",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color(0xFF0F172A).copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(40.dp))

            Text(
                "MeseroWatch",
                fontSize = 42.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )
            Text(
                "Gestión inteligente para tu restaurante",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(48.dp))

            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.7f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("BIENVENIDO", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Ingresa tus credenciales", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = state.user,
                        onValueChange = viewModel::onUserChanged,
                        label = { Text("Usuario o Email") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF3B82F6)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF3B82F6)) },
                        trailingIcon = {
                            IconButton(onClick = viewModel::togglePasswordVisibility) {
                                Icon(
                                    imageVector = if (state.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )

                    AnimatedVisibility(visible = state.password.isNotEmpty()) {
                        Column(modifier = Modifier.padding(top = 16.dp, start = 4.dp)) {
                            Text("SEGURIDAD DE CONTRASEÑA", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            ValidationRow("Mínimo 8 caracteres", hasMinLength)
                            ValidationRow("Al menos un número", hasNumber)
                            ValidationRow("Una letra mayúscula", hasUppercase)
                            ValidationRow("Un carácter especial", hasSpecialChar)
                        }
                    }

                    if (state.error != null) {
                        Text(state.error!!, color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = viewModel::login,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (allRulesMet || state.user == "admin") Color(0xFF3B82F6) else Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            TextButton(onClick = onNavigateToRegister, modifier = Modifier.padding(top = 16.dp)) {
                Text("¿No tienes cuenta? Regístrate aquí", color = Color(0xFF3B82F6))
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ValidationRow(text: String, isMet: Boolean) {
    val color by animateColorAsState(
        targetValue = if (isMet) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.5f),
        animationSpec = tween(300)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = color, fontSize = 12.sp)
    }
}
```

### `presentation/ui/register/RegisterScreen.kt`
Pantalla de registro de nuevos usuarios (meseros). Incluye la validación en tiempo real de los requisitos de la contraseña (mayúsculas, números, caracteres especiales) y la confirmación de contraseña.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.registerSuccess) {
        if (state.registerSuccess) onRegisterSuccess()
    }

    val hasMinLength = state.password.length >= 8
    val hasNumber = state.password.any { it.isDigit() }
    val hasUppercase = state.password.any { it.isUpperCase() }
    val hasSpecialChar = state.password.any { !it.isLetterOrDigit() }
    val passwordsMatch = state.password.isNotEmpty() && state.password == state.confirmPassword
    val allRulesMet = hasMinLength && hasNumber && hasUppercase && hasSpecialChar && passwordsMatch

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1200&auto=format&fit=crop",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color(0xFF0F172A).copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Crear Cuenta",
                fontSize = 42.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )
            Text(
                "Únete al equipo de MeseroWatch",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.7f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = state.user,
                        onValueChange = viewModel::onUserChanged,
                        label = { Text("Nombre de Usuario") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF3B82F6)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6)
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    var expandedRol by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedRol,
                        onExpandedChange = { expandedRol = !expandedRol }
                    ) {
                        OutlinedTextField(
                            value = state.rol.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Rol") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedRol,
                            onDismissRequest = { expandedRol = false }
                        ) {
                            RolUsuario.values().filter { it != RolUsuario.ADMIN }.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption.name) },
                                    onClick = {
                                        viewModel.onRolChanged(selectionOption)
                                        expandedRol = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF3B82F6)) },
                        trailingIcon = {
                            IconButton(onClick = viewModel::togglePasswordVisibility) {
                                Icon(
                                    imageVector = if (state.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6)
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChanged,
                        label = { Text("Confirmar Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF3B82F6)) },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6)
                        )
                    )

                    AnimatedVisibility(visible = state.password.isNotEmpty()) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            ValidationRow("Mínimo 8 caracteres", hasMinLength)
                            ValidationRow("Al menos un número", hasNumber)
                            ValidationRow("Una letra mayúscula", hasUppercase)
                            ValidationRow("Un carácter especial", hasSpecialChar)
                            ValidationRow("Las contraseñas coinciden", passwordsMatch)
                        }
                    }

                    if (state.error != null) {
                        Text(state.error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = viewModel::register,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (allRulesMet) Color(0xFF3B82F6) else Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Registrarse", fontWeight = FontWeight.Bold)
                    }
                }
            }

            TextButton(onClick = onBackToLogin, modifier = Modifier.padding(top = 16.dp)) {
                Text("¿Ya tienes cuenta? Inicia Sesión", color = Color(0xFF3B82F6))
            }
        }
    }
}
```

### `presentation/ui/admin/AdminDashboardScreen.kt`
Panel principal del administrador. Muestra los indicadores clave del restaurante: ventas del día, total de pedidos, pedidos en curso, personal activo y ocupación de mesas.

```kotlin
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
```

### `presentation/ui/mesas/EstadoMesasScreen.kt`
Pantalla de control de mesas. Muestra el estado de cada mesa (libre, ocupada, reservada, fuera de servicio) en tiempo real y permite dar de alta nuevas mesas.

```kotlin
@Composable
fun EstadoMesasScreen(viewModel: EstadoMesasViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    if (state.showNuevaMesaDialog) {
        NuevaMesaDialog(
            onDismiss = { viewModel.hideNuevaMesaDialog() },
            onGuardar = { mesa -> viewModel.addMesa(mesa) }
        )
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
                Text("${state.mesas.count { it.estado == EstadoMesa.OCUPADA }} ocupadas ahora", color = Color.Gray)
            }
            Button(
                onClick = { viewModel.showNuevaMesaDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(" Nueva", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusIndicator("Libre", Color(0xFF3B82F6))
            StatusIndicator("Ocupada", Color(0xFF10B981))
        }

        Spacer(Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.mesas) { mesa ->
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
            Text(mesa.estado.name.lowercase(), fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun StatusIndicator(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(Modifier.width(6.dp))
        Text(text, color = Color.Gray, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaMesaDialog(onDismiss: () -> Unit, onGuardar: (Mesa) -> Unit) {
    var numeroMesa by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Nueva mesa", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = numeroMesa, onValueChange = { numeroMesa = it }, label = { Text("Número") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = capacidad, onValueChange = { capacidad = it }, label = { Text("Capacidad") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    val num = numeroMesa.toIntOrNull() ?: 0
                    if (num > 0) {
                        onGuardar(Mesa(id = num, capacidad = capacidad.toIntOrNull() ?: 4))
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Guardar mesa") }
            }
        }
    }
}
```

### `presentation/ui/nuevopedido/NuevoPedidoScreen.kt`
Flujo de creación de comandas. Permite seleccionar la mesa, elegir los platillos del menú, agregar notas especiales y calcular el total del pedido antes de enviarlo a cocina.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoPedidoScreen(
    onNavigateToAlertas: () -> Unit,
    viewModel: NuevoPedidoViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Efecto para navegar cuando se envió el pedido
    LaunchedEffect(state.pedidoEnviado) {
        if (state.pedidoEnviado) onNavigateToAlertas()
    }

    if (state.mesaSeleccionada != null && state.mostrandoResumen) {
        // Pantalla Resumen
        ResumenPedidoScreen(
            mesaId = state.mesaSeleccionada!!,
            viewModel = viewModel,
            state = state
        )
    } else if (state.mesaSeleccionada != null) {
        // Pantalla Elegir platillos
        ElegirPlatillosScreen(
            mesaId = state.mesaSeleccionada!!,
            viewModel = viewModel,
            state = state
        )
    } else {
        // Pantalla Seleccionar mesa
        SeleccionarMesaScreen(viewModel = viewModel, state = state)
    }
}

@Composable
fun SeleccionarMesaScreen(viewModel: NuevoPedidoViewModel, state: mx.utng.carh.meserowatch.mobile.presentation.viewmodel.NuevoPedidoState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp)
    ) {
        Text("Nuevo pedido", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Text("Selecciona una mesa para comenzar", color = Color.Gray, fontSize = 16.sp)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.searchMesa,
            onValueChange = viewModel::onSearchMesaChanged,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            placeholder = { Text("Buscar mesa...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFF1E293B),
                unfocusedIndicatorColor = Color(0xFF1E293B),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip("Todas", state.filterMesa == "Todas") { viewModel.onFilterMesaChanged("Todas") }
            FilterChip("Libres", state.filterMesa == "Libres") { viewModel.onFilterMesaChanged("Libres") }
            FilterChip("Ocupadas", state.filterMesa == "Ocupadas") { viewModel.onFilterMesaChanged("Ocupadas") }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusIndicator("Libre", Color(0xFF3B82F6))
            StatusIndicator("Ocupada", Color(0xFF10B981))
        }

        Spacer(Modifier.height(24.dp))

        Text("Todas las mesas", color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        val mesasFiltradas = state.mesas.filter { mesa ->
            (state.filterMesa == "Todas" ||
                    (state.filterMesa == "Libres" && mesa.estado == EstadoMesa.LIBRE) ||
                    (state.filterMesa == "Ocupadas" && mesa.estado == EstadoMesa.OCUPADA)) &&
                    (state.searchMesa.isEmpty() || mesa.id.toString().contains(state.searchMesa))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(mesasFiltradas) { mesa ->
                val isOcupada = mesa.estado == EstadoMesa.OCUPADA
                MesaItem(mesa = mesa, onClick = {
                    if (!isOcupada) viewModel.seleccionarMesa(mesa.id)
                })
            }
        }
    }
}

@Composable
fun MesaItem(mesa: Mesa, onClick: () -> Unit) {
    val isOcupada = mesa.estado == EstadoMesa.OCUPADA
    val borderColor = if (isOcupada) Color(0xFF10B981) else Color(0xFF3B82F6)
    val backgroundColor = borderColor.copy(alpha = if (isOcupada) 0.05f else 0.1f)

    Box(
        modifier = Modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = !isOcupada) { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                mesa.id.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOcupada) Color.Gray else Color.White
            )
            Text(
                if (isOcupada) "Ocupada" else "Libre",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                "${mesa.capacidad} lug.",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF1E293B),
        modifier = Modifier.height(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text, color = if (isSelected) Color.White else Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun StatusIndicator(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(Modifier.width(6.dp))
        Text(text, color = Color.Gray, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElegirPlatillosScreen(
    mesaId: Int,
    viewModel: NuevoPedidoViewModel,
    state: mx.utng.carh.meserowatch.mobile.presentation.viewmodel.NuevoPedidoState
) {
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
                Text("Elegir platillos", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Mesa $mesaId · 6 lugares", color = Color.Gray, fontSize = 16.sp)
            }
            Box(
                modifier = Modifier.size(36.dp).background(Color(0xFF1E293B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(state.seleccionados.values.sum().toString(), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(24.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val categorias = listOf("Todos", "Entradas", "Platos", "Postres", "Bebidas", "Complementos", "Especiales")
            items(categorias) { cat ->
                FilterChip(cat, state.filterCategoria == cat) { viewModel.onCategoriaChanged(cat) }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.searchPlatillo,
            onValueChange = viewModel::onSearchPlatilloChanged,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            placeholder = { Text("Buscar platillo...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFF1E293B),
                unfocusedIndicatorColor = Color(0xFF1E293B),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(Modifier.height(24.dp))

        val platillosFiltrados = state.platillos.filter {
            (state.filterCategoria == "Todos" || it.categoria == state.filterCategoria) &&
                    (state.searchPlatillo.isEmpty() || it.nombre.contains(state.searchPlatillo, ignoreCase = true))
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(platillosFiltrados) { platillo ->
                val count = state.seleccionados[platillo.id] ?: 0
                val isSelected = count > 0

                Surface(
                    color = Color(0xFF1E293B).copy(alpha = if (platillo.disponible) 1f else 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = platillo.disponible) {
                            viewModel.togglePlatillo(platillo.id)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(platillo.emoji, fontSize = 32.sp)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(platillo.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("$${platillo.precio.toInt()}", color = Color.Gray, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        if (isSelected) Color(0xFF10B981) else Color(0xFF0F172A),
                                        RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (platillo.disponible) "Disponible" else "No disponible",
                                color = if (platillo.disponible) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (state.seleccionados.isNotEmpty()) {
            val totalItems = state.seleccionados.values.sum()
            val totalPrice = state.platillos
                .filter { state.seleccionados.containsKey(it.id) }
                .sumOf { it.precio * (state.seleccionados[it.id] ?: 0) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$totalItems platillo seleccionado", color = Color.Gray, fontSize = 16.sp)
                Text("$${totalPrice.toInt()}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.irAResumen() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Ver pedido", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenPedidoScreen(
    mesaId: Int,
    viewModel: NuevoPedidoViewModel,
    state: mx.utng.carh.meserowatch.mobile.presentation.viewmodel.NuevoPedidoState
) {
    // Convertir seleccionados a lista de PlatilloSeleccionado con cantidades iniciales
    val platillosSeleccionados = state.platillos
        .filter { state.seleccionados.containsKey(it.id) }
        .map { p -> PlatilloSeleccionado(p.id, p.nombre, p.precio, state.seleccionados[p.id]!!) }

    // Mapa de notas por instancia: key = "id#índice", para gestionar notas por cada unidad
    val notasPorInstancia = remember { mutableStateMapOf<String, String>() }

    // Inicializar notas vacías
    LaunchedEffect(platillosSeleccionados) {
        platillosSeleccionados.forEach { item ->
            repeat(item.cantidad) { i ->
                val key = "${item.id}#$i"
                if (!notasPorInstancia.containsKey(key)) {
                    notasPorInstancia[key] = ""
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp)
    ) {
        Text("Resumen del pedido", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Text("Mesa $mesaId · Luis Cervantes", color = Color.Gray, fontSize = 16.sp)

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(platillosSeleccionados.size) { index ->
                val item = platillosSeleccionados[index]
                val notasDeEstePlatillo = (0 until item.cantidad).map { i ->
                    notasPorInstancia["${item.id}#$i"] ?: ""
                }
                CardResumenItem(
                    item = item,
                    notas = notasDeEstePlatillo,
                    onCantidadChange = { nuevaCantidad ->
                        viewModel.setCantidad(item.id, nuevaCantidad)
                    },
                    onNotaChange = { instIndex, nuevaNota ->
                        notasPorInstancia["${item.id}#$instIndex"] = nuevaNota
                    }
                )
            }
        }

        val total = platillosSeleccionados.sumOf { it.precio * it.cantidad }

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total del pedido", color = Color.Gray, fontSize = 16.sp)
            Text("$${total.toInt()}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                // Construir la lista de items para Firebase (cada unidad con su nota)
                val itemsFirebase = mutableListOf<Map<String, String>>()
                platillosSeleccionados.forEach { item ->
                    repeat(item.cantidad) { i ->
                        val nota = notasPorInstancia["${item.id}#$i"] ?: ""
                        itemsFirebase.add(mapOf("descripcion" to item.nombre, "nota" to nota))
                    }
                }
                val descResumen = platillosSeleccionados.joinToString(", ") { "${it.cantidad}x ${it.nombre}" }
                viewModel.enviarPedido(
                    mesaId = mesaId,
                    platillosConNotas = platillosSeleccionados.map { it.copy(nota = "") }, // Las notas van en itemsFirebase
                    itemsFirebase = itemsFirebase,
                    total = total,
                    descripcion = descResumen
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Enviar a cocina", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardResumenItem(
    item: PlatilloSeleccionado,
    notas: List<String>,
    onCantidadChange: (Int) -> Unit,
    onNotaChange: (Int, String) -> Unit
) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(32.dp).background(Color(0xFF0F172A), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Text("🥑", fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text(item.nombre, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("$${(item.precio * item.cantidad).toInt()}", color = Color.White)
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                IconButton(onClick = { onCantidadChange(item.cantidad - 1) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = Color.Gray)
                }
                Text(item.cantidad.toString(), color = Color.White, modifier = Modifier.padding(horizontal = 12.dp))
                IconButton(onClick = { onCantidadChange(item.cantidad + 1) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                }
                Spacer(Modifier.width(8.dp))
                Text("órdenes", color = Color.Gray, fontSize = 12.sp)
            }

            repeat(item.cantidad) { i ->
                var notaTexto by remember(notas.getOrElse(i) { "" }) { mutableStateOf(notas.getOrElse(i) { "" }) }

                Text("Orden ${i + 1}", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                OutlinedTextField(
                    value = notaTexto,
                    onValueChange = {
                        notaTexto = it
                        onNotaChange(i, it)
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    placeholder = { Text("Agregar nota...", color = Color.DarkGray, fontSize = 12.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF3B82F6)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.White)
                )
            }
        }
    }
}
```

### `presentation/ui/menu/MenuAdminScreen.kt`
Pantalla administrativa del menú del restaurante. Permite ver, filtrar por categoría, buscar, agregar, editar y eliminar platillos.

```kotlin
@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF1E293B),
        modifier = Modifier.height(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text, color = if (isSelected) Color.White else Color.Gray, fontSize = 14.sp)
        }
    }
}
// ------------------------------------------------------------

@Composable
fun MenuAdminScreen(viewModel: MenuAdminViewModel = viewModel()) {
    var mostrarNuevoPlatillo by remember { mutableStateOf(false) }
    var platilloAEditar by remember { mutableStateOf<Platillo?>(null) }

    if (mostrarNuevoPlatillo) {
        NuevoPlatilloDialog(onDismiss = { mostrarNuevoPlatillo = false }) { nuevo ->
            viewModel.addPlatillo(nuevo)
        }
    }

    if (platilloAEditar != null) {
        EditarPlatilloDialog(platillo = platilloAEditar!!, onDismiss = { platilloAEditar = null }) { editado ->
            viewModel.updatePlatillo(editado)
        }
    }

    val state by viewModel.state.collectAsState()

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
                Text("${state.platillos.size} platillos registrados", color = Color.Gray)
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

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val categorias = listOf("Todos", "Entradas", "Platos", "Bebidas", "Postres", "Complementos", "Especiales")
            items(categorias) { cat ->
                FilterChip(cat, state.selectedCategory == cat) { viewModel.onCategorySelected(cat) }
            }
        }

        Spacer(Modifier.height(16.dp))

        val platillosFiltrados = if (state.selectedCategory == "Todos") {
            state.platillos
        } else {
            state.platillos.filter { it.categoria == state.selectedCategory }
        }.filter {
            state.searchQuery.isEmpty() || it.nombre.contains(state.searchQuery, ignoreCase = true)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(platillosFiltrados) { platillo ->
                AdminPlatilloItem(
                    platillo = platillo,
                    onEdit = { platilloAEditar = platillo },
                    onDelete = { viewModel.deletePlatillo(platillo.id) }
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
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
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

// Diálogos (adaptados para usar callbacks)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoPlatilloDialog(onDismiss: () -> Unit, onGuardar: (Platillo) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Platos") }
    var expanded by remember { mutableStateOf(false) }
    val categorias = listOf("Entradas", "Platos", "Bebidas", "Postres", "Complementos", "Especiales")

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Nuevo platillo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = categoria, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { categoria = cat; expanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(onClick = {
                        if (nombre.isNotBlank() && precio.isNotBlank()) {
                            onGuardar(Platillo(nombre = nombre, precio = precio.toDoubleOrNull() ?: 0.0, categoria = categoria, emoji = when(categoria) { "Bebidas" -> "🥤"; "Postres" -> "🍰"; "Entradas" -> "🥑"; "Complementos" -> "🍟"; else -> "🍽️" }))
                        }
                    }) { Text("Guardar") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPlatilloDialog(platillo: Platillo, onDismiss: () -> Unit, onGuardar: (Platillo) -> Unit) {
    var nombre by remember { mutableStateOf(platillo.nombre) }
    var precio by remember { mutableStateOf(platillo.precio.toString()) }
    var categoria by remember { mutableStateOf(platillo.categoria) }
    var expanded by remember { mutableStateOf(false) }
    val categorias = listOf("Entradas", "Platos", "Bebidas", "Postres", "Complementos", "Especiales")

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Editar platillo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = categoria, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { categoria = cat; expanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(onClick = {
                        if (nombre.isNotBlank() && precio.isNotBlank()) {
                            onGuardar(platillo.copy(nombre = nombre, precio = precio.toDoubleOrNull() ?: 0.0, categoria = categoria, emoji = when(categoria) { "Bebidas" -> "🥤"; "Postres" -> "🍰"; "Entradas" -> "🥑"; "Complementos" -> "🍟"; else -> "🍽️" }))
                        }
                    }) { Text("Actualizar") }
                }
            }
        }
    }
}
```

### `presentation/ui/personal/PersonalAdminScreen.kt`
Pantalla de administración de personal (usuarios/meseros). Muestra el listado de empleados y permite gestionarlos según su estado y rol.

```kotlin
@Composable
fun PersonalAdminScreen(
    onNavigateToZonas: () -> Unit,
    viewModel: UsuariosViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.showNuevoDialog) {
        NuevoUsuarioDialog(
            onDismiss = { viewModel.hideNuevoUsuarioDialog() },
            onGuardar = { viewModel.addUsuario(it) }
        )
    }
    if (state.usuarioAEditar != null) {
        EditarUsuarioDialog(
            usuario = state.usuarioAEditar!!,
            onDismiss = { viewModel.cancelEditUsuario() },
            onGuardar = { viewModel.updateUsuario(it) }
        )
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
                Text("${state.usuarios.size} registrados", color = Color.Gray)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onNavigateToZonas,
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Zonas", fontSize = 14.sp)
                }
                Button(
                    onClick = { viewModel.showNuevoUsuarioDialog() },
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Nuevo", fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val filtros = listOf("Todos", "Activo", "Inactivo", "En descanso")
            items(filtros) { filtro ->
                FilterChip(filtro, state.filter == filtro) { viewModel.setFilter(filtro) }
            }
        }

        Spacer(Modifier.height(16.dp))

        val usuariosFiltrados = if (state.filter == "Todos") {
            state.usuarios
        } else {
            state.usuarios.filter { it.estadoUsuario.name.equals(state.filter.replace(" ", "_"), ignoreCase = true) }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
            items(usuariosFiltrados) { usuario ->
                UsuarioItem(usuario) { viewModel.editUsuario(usuario) }
            }
        }
    }
}

@Composable
fun UsuarioItem(usuario: Usuario, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp).background(Color(0xFF1E293B), CircleShape), contentAlignment = Alignment.Center) {
            Text(usuario.fotoEmoji, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(usuario.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("${usuario.rol} · ${usuario.zonaAsignada}", color = Color.Gray, fontSize = 14.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            val statusColor = when (usuario.estadoUsuario) {
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
            Text(if (usuario.activo) "En turno" else "Fuera", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

// Diálogos adaptados
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoUsuarioDialog(onDismiss: () -> Unit, onGuardar: (Usuario) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf(RolUsuario.MESERO) }
    var zonaId by remember { mutableStateOf("") }
    var estadoInicial by remember { mutableStateOf(EstadoUsuario.ACTIVO) }
    var expandedRol by remember { mutableStateOf(false) }
    // En un caso real obtendrías las zonas del ViewModel, aquí simplificamos con vacío
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Nuevo usuario", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                // Selección de rol
                ExposedDropdownMenuBox(expanded = expandedRol, onExpandedChange = { expandedRol = !expandedRol }) {
                    OutlinedTextField(value = rol.name, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    ExposedDropdownMenu(expanded = expandedRol, onDismissRequest = { expandedRol = false }) {
                        RolUsuario.values().forEach { r ->
                            DropdownMenuItem(text = { Text(r.name) }, onClick = { rol = r; expandedRol = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Estado inicial (chips)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoUsuario.values().forEach { estado ->
                        FilterChip(selected = estadoInicial == estado, onClick = { estadoInicial = estado }, label = { Text(estado.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    if (nombre.isNotBlank()) {
                        onGuardar(Usuario(nombre = nombre, rol = rol, activo = estadoInicial == EstadoUsuario.ACTIVO, estadoUsuario = estadoInicial, zonaId = zonaId, zonaAsignada = ""))
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Guardar usuario") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarUsuarioDialog(usuario: Usuario, onDismiss: () -> Unit, onGuardar: (Usuario) -> Unit) {
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var rol by remember { mutableStateOf(usuario.rol) }
    var zonaId by remember { mutableStateOf(usuario.zonaId) }
    var estado by remember { mutableStateOf(usuario.estadoUsuario) }
    var expandedRol by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Editar usuario", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expandedRol, onExpandedChange = { expandedRol = !expandedRol }) {
                    OutlinedTextField(value = rol.name, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    ExposedDropdownMenu(expanded = expandedRol, onDismissRequest = { expandedRol = false }) {
                        RolUsuario.values().forEach { r ->
                            DropdownMenuItem(text = { Text(r.name) }, onClick = { rol = r; expandedRol = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoUsuario.values().forEach { e ->
                        FilterChip(selected = estado == e, onClick = { estado = e }, label = { Text(e.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    onGuardar(usuario.copy(nombre = nombre, rol = rol, activo = estado == EstadoUsuario.ACTIVO, estadoUsuario = estado, zonaId = zonaId))
                }, modifier = Modifier.fillMaxWidth()) { Text("Actualizar usuario") }
            }
        }
    }
}
```

### `presentation/ui/zonas/ZonasAdminScreen.kt`
Pantalla de gestión de zonas del restaurante (categorías A, B y C). Permite visualizar el personal asignado a cada zona mediante tarjetas expandibles y editar una zona mediante pulsación larga (long-press).

```kotlin
@Composable
fun ZonasAdminScreen(viewModel: ZonasAdminViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    if (state.showNuevaZonaDialog) {
        NuevaZonaDialog(
            onDismiss = { viewModel.hideNuevaZonaDialog() },
            onGuardar = { viewModel.addZona(it) }
        )
    }
    if (state.zonaAEditar != null) {
        EditarZonaDialog(
            zona = state.zonaAEditar!!,
            onDismiss = { viewModel.cancelEditZona() },
            onGuardar = { viewModel.updateZona(it) },
            onEliminar = { viewModel.deleteZona(state.zonaAEditar!!.id) }
        )
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
            Text("Zonas", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Button(
                onClick = { viewModel.showNuevaZonaDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Nueva", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        val clasificaciones = listOf("Zona A", "Zona B", "Zona C")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(clasificaciones) { clase ->
                Column {
                    Text(clase, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(12.dp))
                    val zonasClase = state.zonas.filter { it.nombreZona.contains(clase, ignoreCase = true) }
                    if (zonasClase.isEmpty()) {
                        Text("Sin zonas asignadas", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
                    } else {
                        zonasClase.forEach { zona ->
                            val personalEnZona = state.usuarios.filter { it.zonaId == zona.id }
                            ZonaItem(zona, personalEnZona) { viewModel.editZona(zona) }
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
            item {
                val otras = state.zonas.filter { zona -> !clasificaciones.any { c -> zona.nombreZona.contains(c, ignoreCase = true) } }
                if (otras.isNotEmpty()) {
                    Text("Otras Zonas", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(12.dp))
                    otras.forEach { zona ->
                        val personalEnZona = state.usuarios.filter { it.zonaId == zona.id }
                        ZonaItem(zona, personalEnZona) { viewModel.editZona(zona) }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZonaItem(zona: Zona, personal: List<mx.utng.carh.meserowatch.mobile.domain.model.Usuario>, onLongClick: () -> Unit) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (personal.isNotEmpty()) expandido = !expandido },
                onLongClick = onLongClick
            )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusColor = if (zona.estadoZona == EstadoZona.DISPONIBLE) Color(0xFF10B981) else Color(0xFFEF4444)
                Box(Modifier.size(12.dp).background(statusColor, CircleShape))
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(zona.nombreZona, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${personal.size} personas asignadas", color = Color.Gray, fontSize = 12.sp)
                }
                if (personal.isNotEmpty()) {
                    Icon(
                        imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }

            AnimatedVisibility(visible = expandido, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    Spacer(Modifier.height(12.dp))
                    personal.forEach { usuario ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(usuario.fotoEmoji, fontSize = 20.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(usuario.nombre, color = Color.White, fontSize = 14.sp)
                            Spacer(Modifier.weight(1f))
                            if (usuario.activo) {
                                Badge(containerColor = Color(0xFF10B981).copy(0.2f), contentColor = Color(0xFF10B981)) {
                                    Text("En turno", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Diálogos para agregar/editar zonas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaZonaDialog(onDismiss: () -> Unit, onGuardar: (Zona) -> Unit) {
    var nombreZona by remember { mutableStateOf("") }
    var estadoInicial by remember { mutableStateOf(EstadoZona.DISPONIBLE) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Nueva zona", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = nombreZona, onValueChange = { nombreZona = it }, label = { Text("Nombre de zona") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoZona.values().forEach { estado ->
                        FilterChip(selected = estadoInicial == estado, onClick = { estadoInicial = estado }, label = { Text(estado.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    if (nombreZona.isNotBlank()) {
                        onGuardar(Zona(nombreZona = nombreZona, estadoZona = estadoInicial))
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Guardar zona") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarZonaDialog(zona: Zona, onDismiss: () -> Unit, onGuardar: (Zona) -> Unit, onEliminar: () -> Unit) {
    var nombreZona by remember { mutableStateOf(zona.nombreZona) }
    var estado by remember { mutableStateOf(zona.estadoZona) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Editar zona", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = nombreZona, onValueChange = { nombreZona = it }, label = { Text("Nombre de zona") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoZona.values().forEach { e ->
                        FilterChip(selected = estado == e, onClick = { estado = e }, label = { Text(e.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEliminar) { Text("Eliminar", color = Color(0xFFEF4444)) }
                    Button(onClick = {
                        onGuardar(zona.copy(nombreZona = nombreZona, estadoZona = estado))
                    }) { Text("Actualizar") }
                }
            }
        }
    }
}
```

### `presentation/ui/turnos/TurnosPersonalScreen.kt`
Pantalla de control de turnos del personal. Registra la hora de inicio y fin de turno de cada usuario.

```kotlin
@Composable
fun TurnosPersonalScreen(viewModel: UsuariosViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    // Diálogo para agregar usuario
    if (state.showNuevoDialog) {
        NuevoUsuarioDialog(
            onDismiss = { viewModel.hideNuevoUsuarioDialog() },
            onGuardar = { usuario -> viewModel.addUsuario(usuario) }
        )
    }
    // Diálogo para editar usuario (cuando corresponda)
    if (state.usuarioAEditar != null) {
        EditarUsuarioDialog(
            usuario = state.usuarioAEditar!!,
            onDismiss = { viewModel.cancelEditUsuario() },
            onGuardar = { viewModel.updateUsuario(it) }
        )
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
            Text("Turnos", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Button(
                onClick = { viewModel.showNuevoUsuarioDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Nuevo", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.usuarios) { usuario ->
                UsuarioTurnoItem(usuario) {
                    viewModel.editUsuario(usuario)
                }
            }
        }
    }
}

@Composable
fun UsuarioTurnoItem(usuario: Usuario, onEdit: () -> Unit) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onEdit() }
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
                Text("Turno rotativo", color = Color.Gray, fontSize = 12.sp)
                val (statusText, statusColor) = when (usuario.estadoUsuario) {
                    EstadoUsuario.ACTIVO -> "Activo" to Color(0xFF10B981)
                    EstadoUsuario.INACTIVO -> "Inactivo" to Color(0xFFEF4444)
                    EstadoUsuario.EN_DESCANSO -> "En descanso" to Color(0xFFF59E0B)
                }
                Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
```

### `presentation/ui/alertas/AlertasScreen.kt`
Pantalla de alertas/notificaciones de pedidos. Muestra los pedidos filtrados por estado y el detalle de un pedido seleccionado.

```kotlin
@Composable
fun AlertasScreen(viewModel: AlertasViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    if (state.pedidoDetalle != null) {
        DetallePedidoDialog(pedido = state.pedidoDetalle!!) {
            viewModel.cerrarDetalle()
        }
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
                Text("Pedidos activos", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Actualizado hace un momento", color = Color.Gray)
            }
            Box(
                modifier = Modifier.background(Color(0xFF1E293B), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("${state.pedidos.size} pedidos", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filtros = listOf("Todos", "Listos", "En cocina", "Cancelados")
            items(filtros) { filtro ->
                FilterChip(filtro, state.filtro == filtro) { viewModel.setFiltro(filtro) }
            }
        }

        Spacer(Modifier.height(24.dp))

        val pedidosFiltrados = when (state.filtro) {
            "Listos" -> state.pedidos.filter { it.estado == EstadoPedido.LISTO }
            "En cocina" -> state.pedidos.filter { it.estado == EstadoPedido.EN_PREPARACION }
            "Cancelados" -> state.pedidos.filter { it.estado == EstadoPedido.CANCELADO }
            else -> state.pedidos
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(pedidosFiltrados) { pedido ->
                AlertaItem(pedido) {
                    viewModel.verDetalle(pedido)
                }
            }
        }
    }
}

@Composable
fun DetallePedidoDialog(pedido: Pedido, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("Detalle Mesa ${pedido.mesa}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                Text("Resumen:", color = Color.Gray, fontSize = 14.sp)
                Text(pedido.descripcion, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))

                Spacer(Modifier.height(16.dp))
                Text("Estado:", color = Color.Gray, fontSize = 14.sp)

                val statusColor = when (pedido.estado) {
                    EstadoPedido.LISTO -> Color(0xFF10B981)
                    EstadoPedido.CANCELADO -> Color(0xFFEF4444)
                    EstadoPedido.EN_PREPARACION -> Color(0xFFF59E0B)
                    else -> Color.White
                }

                Text(
                    pedido.estado.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
fun AlertaItem(pedido: Pedido, onClick: () -> Unit) {
    val (statusText, statusColor) = when (pedido.estado) {
        EstadoPedido.LISTO -> "Listo" to Color(0xFF10B981)
        EstadoPedido.CANCELADO -> "Cancelado" to Color(0xFFEF4444)
        EstadoPedido.EN_PREPARACION -> "Cocina" to Color(0xFFF59E0B)
        else -> pedido.estado.name to Color.Gray
    }

    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, CircleShape)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Mesa ${pedido.mesa}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(pedido.descripcion, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
            }
            Box(
                modifier = Modifier.background(
                    statusColor.copy(0.1f),
                    RoundedCornerShape(8.dp)
                ).padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
```

### `presentation/ui/historial/HistorialPedidosScreen.kt`
Pantalla de historial de pedidos. Permite buscar y consultar pedidos pasados.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialPedidosScreen(viewModel: HistorialPedidosViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp)
    ) {
        Text("Historial de pedidos", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchChanged,
            placeholder = { Text("Buscar mesa, platillo...", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFF1E293B),
                unfocusedIndicatorColor = Color(0xFF1E293B),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.filteredPedidos) { pedido ->
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
                val color = when (pedido.estado) {
                    EstadoPedido.ENTREGADO -> Color(0xFF10B981)
                    EstadoPedido.CANCELADO -> Color(0xFFEF4444)
                    else -> Color(0xFFF59E0B)
                }
                Text(pedido.estado.name, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text(pedido.descripcion, color = Color.Gray, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mesero: ${if(pedido.meseroId.isNotEmpty()) pedido.meseroId else "Sin asignar"}", color = Color.Gray, fontSize = 12.sp)
                Text("$${pedido.total.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}
```

---

## Presentation / ViewModel

### `presentation/viewmodel/LoginViewModel.kt`
Maneja el estado y la lógica de la pantalla de login: usuario, contraseña, visibilidad de contraseña, estado de carga, errores y confirmación de inicio de sesión exitoso.

```kotlin
data class LoginUiState(
    val user: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val passwordVisible: Boolean = false,
    val loginSuccess: Boolean = false
)

class LoginViewModel : ViewModel() {
    private val authRepo = AppModule.authRepository

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onUserChanged(value: String) {
        _uiState.value = _uiState.value.copy(user = value, error = null)
    }
    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }
    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }

    fun login() {
        val state = _uiState.value
        if (state.user == "admin" && state.password == "admin123") {
            viewModelScope.launch {
                authRepo.loginAsAdmin().onSuccess { admin ->
                    SessionManager.loginAsAdmin() // Mantenemos SessionManager
                    _uiState.value = _uiState.value.copy(isLoggedIn = true, loginSuccess = true)
                }.onFailure {
                    _uiState.value = _uiState.value.copy(error = "Error al autenticar admin")
                }
            }
            return
        }

        // Validación de contraseña (misma lógica que antes)
        val hasMinLength = state.password.length >= 8
        val hasNumber = state.password.any { it.isDigit() }
        val hasUppercase = state.password.any { it.isUpperCase() }
        val hasSpecialChar = state.password.any { !it.isLetterOrDigit() }
        if (!hasMinLength || !hasNumber || !hasUppercase || !hasSpecialChar) {
            _uiState.value = _uiState.value.copy(error = "La contraseña no cumple los requisitos")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepo.login(state.user.trim())
                .onSuccess { usuario ->
                    SessionManager.loginAsUser(usuario)
                    _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }
}
```

### `presentation/viewmodel/RegisterViewModel.kt`
Maneja el estado y la lógica de la pantalla de registro: datos del nuevo usuario, rol asignado, validación de contraseñas y llamada al `AuthRepository` para registrar al usuario.

```kotlin
data class RegisterUiState(
    val user: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val rol: RolUsuario = RolUsuario.MESERO,
    val isLoading: Boolean = false,
    val error: String? = null,
    val registerSuccess: Boolean = false,
    val passwordVisible: Boolean = false
)

class RegisterViewModel : ViewModel() {
    private val authRepo = AppModule.authRepository

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onUserChanged(v: String) { _uiState.value = _uiState.value.copy(user = v, error = null) }
    fun onPasswordChanged(v: String) { _uiState.value = _uiState.value.copy(password = v, error = null) }
    fun onConfirmPasswordChanged(v: String) { _uiState.value = _uiState.value.copy(confirmPassword = v, error = null) }
    fun onRolChanged(rol: RolUsuario) { _uiState.value = _uiState.value.copy(rol = rol) }
    fun togglePasswordVisibility() { _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible) }

    fun register() {
        val state = _uiState.value
        if (state.user.isEmpty()) { _uiState.value = state.copy(error = "Ingresa un nombre de usuario"); return }
        val hasMinLength = state.password.length >= 8
        val hasNumber = state.password.any { it.isDigit() }
        val hasUppercase = state.password.any { it.isUpperCase() }
        val hasSpecialChar = state.password.any { !it.isLetterOrDigit() }
        val passwordsMatch = state.password.isNotEmpty() && state.password == state.confirmPassword
        if (!hasMinLength || !hasNumber || !hasUppercase || !hasSpecialChar || !passwordsMatch) {
            _uiState.value = state.copy(error = "Revisa los requisitos de seguridad")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val nuevoUsuario = Usuario(
                nombre = state.user.trim(),
                rol = state.rol,
                activo = true,
                estadoUsuario = EstadoUsuario.ACTIVO
            )
            authRepo.register(nuevoUsuario)
                .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false, registerSuccess = true) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
        }
    }
}
```

### `presentation/viewmodel/AdminDashboardViewModel.kt`
Calcula y expone en tiempo real los indicadores del dashboard (ventas del día, pedidos totales, pedidos en curso, personal activo, mesas ocupadas) combinando los flujos de pedidos y usuarios.

```kotlin
data class AdminDashboardState(
    val ventasHoy: Double = 0.0,
    val totalPedidos: Int = 0,
    val pedidosEnCurso: Int = 0,
    val personalActivo: Int = 0,
    val mesasOcupadas: Int = 0,
    val mesasTotales: Int = 12
)

class AdminDashboardViewModel : ViewModel() {
    private val pedidoRepo = AppModule.pedidoRepository
    private val usuarioRepo = AppModule.usuarioRepository
    private val mesaRepo = AppModule.mesaRepository

    private val _state = MutableStateFlow(AdminDashboardState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                pedidoRepo.getPedidos(),
                usuarioRepo.getUsuarios(),
                mesaRepo.getTodasLasMesas()
            ) { pedidos, usuarios, mesas ->
                val enCurso = pedidos.count { it.estado != mx.utng.carh.meserowatch.mobile.domain.model.EstadoPedido.ENTREGADO && it.estado != mx.utng.carh.meserowatch.mobile.domain.model.EstadoPedido.CANCELADO }
                val entregados = pedidos.filter { it.estado == mx.utng.carh.meserowatch.mobile.domain.model.EstadoPedido.ENTREGADO }
                val ventas = entregados.sumOf { it.total }
                val activos = usuarios.count { it.activo }
                val ocupadas = mesas.count { it.estado == mx.utng.carh.meserowatch.mobile.domain.model.EstadoMesa.OCUPADA }
                AdminDashboardState(
                    ventasHoy = ventas,
                    totalPedidos = pedidos.size,
                    pedidosEnCurso = enCurso,
                    personalActivo = activos,
                    mesasOcupadas = ocupadas,
                    mesasTotales = mesas.size
                )
            }.collect { _state.value = it }
        }
    }
}
```

### `presentation/viewmodel/EstadoMesasViewModel.kt`
Maneja el estado de la pantalla de mesas: la lista de mesas en tiempo real y el diálogo para crear una nueva mesa.

```kotlin
data class EstadoMesasState(
    val mesas: List<Mesa> = emptyList(),
    val showNuevaMesaDialog: Boolean = false
)

class EstadoMesasViewModel : ViewModel() {
    private val repo = AppModule.mesaRepository

    private val _state = MutableStateFlow(EstadoMesasState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getTodasLasMesas().collect { mesas ->
                _state.value = _state.value.copy(mesas = mesas)
            }
        }
    }

    fun showNuevaMesaDialog() {
        _state.value = _state.value.copy(showNuevaMesaDialog = true)
    }

    fun hideNuevaMesaDialog() {
        _state.value = _state.value.copy(showNuevaMesaDialog = false)
    }

    fun addMesa(mesa: Mesa) {
        viewModelScope.launch {
            repo.addMesa(mesa)
            hideNuevaMesaDialog()
        }
    }

    fun updateMesa(mesa: Mesa) {
        viewModelScope.launch { repo.updateMesa(mesa) }
    }

    fun deleteMesa(id: Int) {
        viewModelScope.launch { repo.deleteMesa(id) }
    }
}
```

### `presentation/viewmodel/NuevoPedidoViewModel.kt`
Maneja el estado completo del flujo de creación de pedidos: mesas disponibles, búsqueda/selección de mesa, platillos del menú, búsqueda de platillos y armado del pedido con su cálculo de total.

```kotlin
data class NuevoPedidoState(
    val mesas: List<Mesa> = emptyList(),
    val searchMesa: String = "",
    val filterMesa: String = "Todas",
    val mesaSeleccionada: Int? = null,
    val platillos: List<Platillo> = emptyList(),
    val searchPlatillo: String = "",
    val filterCategoria: String = "Todos",
    val seleccionados: Map<String, Int> = emptyMap(),
    val mostrandoResumen: Boolean = false,
    val pedidoEnviado: Boolean = false
)

class NuevoPedidoViewModel : ViewModel() {
    private val mesaRepo = AppModule.mesaRepository
    private val menuRepo = AppModule.menuRepository
    private val pedidoRepo = AppModule.pedidoRepository

    private val _state = MutableStateFlow(NuevoPedidoState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            mesaRepo.getTodasLasMesas().collect { mesas ->
                _state.update { it.copy(mesas = mesas) }
            }
        }
    }

    fun onSearchMesaChanged(q: String) { _state.update { it.copy(searchMesa = q) } }
    fun onFilterMesaChanged(f: String) { _state.update { it.copy(filterMesa = f) } }

    fun seleccionarMesa(id: Int) {
        _state.update { it.copy(mesaSeleccionada = id) }
        // Cargar platillos cuando se selecciona mesa
        viewModelScope.launch {
            menuRepo.getMenu().collect { lista ->
                _state.update { it.copy(platillos = lista) }
            }
        }
    }

    fun volverAMesas() { _state.update { it.copy(mesaSeleccionada = null, mostrandoResumen = false) } }

    fun onSearchPlatilloChanged(q: String) { _state.update { it.copy(searchPlatillo = q) } }
    fun onCategoriaChanged(cat: String) { _state.update { it.copy(filterCategoria = cat) } }

    fun togglePlatillo(platilloId: String) {
        _state.update { current ->
            val map = current.seleccionados.toMutableMap()
            if (map.containsKey(platilloId)) map.remove(platilloId) else map[platilloId] = 1
            current.copy(seleccionados = map)
        }
    }

    fun setCantidad(platilloId: String, cantidad: Int) {
        if (cantidad <= 0) {
            _state.update { it.copy(seleccionados = it.seleccionados - platilloId) }
        } else {
            _state.update { it.copy(seleccionados = it.seleccionados + (platilloId to cantidad)) }
        }
    }

    fun irAResumen() { _state.update { it.copy(mostrandoResumen = true) } }
    fun volverAPlatillos() { _state.update { it.copy(mostrandoResumen = false) } }

    fun enviarPedido(
        mesaId: Int,
        platillosConNotas: List<PlatilloSeleccionado>,
        itemsFirebase: List<Map<String, String>>,
        total: Double,
        descripcion: String
    ) {
        viewModelScope.launch {
            val pedido = Pedido(
                mesa = mesaId,
                descripcion = descripcion,
                total = total,
                estado = EstadoPedido.EN_PREPARACION,
                platillos = platillosConNotas,
                usuarioId = SessionManager.currentUser?.id ?: ""
            )
            pedidoRepo.addPedido(pedido, itemsFirebase)
            _state.update { it.copy(pedidoEnviado = true) }
        }
    }
}
```

### `presentation/viewmodel/MenuAdminViewModel.kt`
Maneja el estado de la pantalla de menú: lista de platillos, categoría seleccionada y búsqueda, además de las operaciones de agregar, actualizar y eliminar platillos.

```kotlin
data class MenuAdminState(
    val platillos: List<Platillo> = emptyList(),
    val selectedCategory: String = "Todos",
    val searchQuery: String = ""
)

class MenuAdminViewModel : ViewModel() {
    private val repo = AppModule.menuRepository

    private val _state = MutableStateFlow(MenuAdminState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getMenu().collect { lista ->
                _state.value = _state.value.copy(platillos = lista)
            }
        }
    }

    fun onCategorySelected(cat: String) { _state.value = _state.value.copy(selectedCategory = cat) }
    fun onSearchQueryChanged(q: String) { _state.value = _state.value.copy(searchQuery = q) }

    fun addPlatillo(platillo: Platillo) {
        viewModelScope.launch { repo.addPlatillo(platillo) }
    }
    fun updatePlatillo(platillo: Platillo) {
        viewModelScope.launch { repo.updatePlatillo(platillo) }
    }
    fun deletePlatillo(id: String) {
        viewModelScope.launch { repo.deletePlatillo(id) }
    }
}
```

### `presentation/viewmodel/UsuariosViewModel.kt`
Maneja el estado de la pantalla de personal: lista de usuarios, filtro, estado de carga, y los diálogos para crear o editar un usuario.

```kotlin
data class UsuariosState(
    val usuarios: List<Usuario> = emptyList(),
    val filter: String = "Todos",
    val isLoading: Boolean = false,
    val showNuevoDialog: Boolean = false,
    val usuarioAEditar: Usuario? = null
)

class UsuariosViewModel : ViewModel() {
    private val repo = AppModule.usuarioRepository

    private val _state = MutableStateFlow(UsuariosState())
    val state = _state.asStateFlow()

    init {
        loadUsuarios()
    }

    private fun loadUsuarios() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repo.getUsuarios().collect { lista ->
                _state.value = _state.value.copy(usuarios = lista, isLoading = false)
            }
        }
    }

    fun setFilter(filter: String) {
        _state.value = _state.value.copy(filter = filter)
    }

    fun showNuevoUsuarioDialog() {
        _state.value = _state.value.copy(showNuevoDialog = true)
    }

    fun hideNuevoUsuarioDialog() {
        _state.value = _state.value.copy(showNuevoDialog = false)
    }

    fun editUsuario(usuario: Usuario) {
        _state.value = _state.value.copy(usuarioAEditar = usuario)
    }

    fun cancelEditUsuario() {
        _state.value = _state.value.copy(usuarioAEditar = null)
    }

    fun addUsuario(usuario: Usuario) {
        viewModelScope.launch {
            repo.addUsuario(usuario)
            hideNuevoUsuarioDialog()
        }
    }

    fun updateUsuario(usuario: Usuario) {
        viewModelScope.launch {
            repo.updateUsuario(usuario)
            cancelEditUsuario()
        }
    }

    fun deleteUsuario(id: String) {
        viewModelScope.launch {
            repo.deleteUsuario(id)
        }
    }
}
```

### `presentation/viewmodel/ZonasAdminViewModel.kt`
Maneja el estado de la pantalla de zonas: lista de zonas, lista de usuarios asociados y los diálogos para crear o editar una zona.

```kotlin
data class ZonasState(
    val zonas: List<Zona> = emptyList(),
    val usuarios: List<Usuario> = emptyList(),
    val showNuevaZonaDialog: Boolean = false,
    val zonaAEditar: Zona? = null
)

class ZonasAdminViewModel : ViewModel() {
    private val zonaRepo = AppModule.zonaRepository
    private val usuarioRepo = AppModule.usuarioRepository

    private val _state = MutableStateFlow(ZonasState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            zonaRepo.getZonas().collect { zonas ->
                _state.value = _state.value.copy(zonas = zonas)
            }
        }
        viewModelScope.launch {
            usuarioRepo.getUsuarios().collect { usuarios ->
                _state.value = _state.value.copy(usuarios = usuarios)
            }
        }
    }

    fun showNuevaZonaDialog() {
        _state.value = _state.value.copy(showNuevaZonaDialog = true)
    }

    fun hideNuevaZonaDialog() {
        _state.value = _state.value.copy(showNuevaZonaDialog = false)
    }

    fun addZona(zona: Zona) {
        viewModelScope.launch {
            zonaRepo.addZona(zona)
            hideNuevaZonaDialog()
        }
    }

    fun editZona(zona: Zona) {
        _state.value = _state.value.copy(zonaAEditar = zona)
    }

    fun cancelEditZona() {
        _state.value = _state.value.copy(zonaAEditar = null)
    }

    fun updateZona(zona: Zona) {
        viewModelScope.launch {
            zonaRepo.updateZona(zona)
            cancelEditZona()
        }
    }

    fun deleteZona(id: String) {
        viewModelScope.launch {
            zonaRepo.deleteZona(id)
            cancelEditZona()
        }
    }
}
```

### `presentation/viewmodel/HistorialPedidosViewModel.kt`
Maneja el estado de la pantalla de historial: lista completa de pedidos, texto de búsqueda y la lista filtrada resultante.

```kotlin
data class HistorialPedidosState(
    val pedidos: List<Pedido> = emptyList(),
    val searchQuery: String = "",
    val filteredPedidos: List<Pedido> = emptyList()
)

class HistorialPedidosViewModel : ViewModel() {
    private val repo = AppModule.pedidoRepository

    private val _state = MutableStateFlow(HistorialPedidosState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.getPedidos(),
                _state.map { it.searchQuery }
            ) { pedidos, query ->
                val reversed = pedidos.reversed()
                if (query.isEmpty()) reversed
                else reversed.filter {
                    it.mesa.toString().contains(query) ||
                            it.descripcion.contains(query, ignoreCase = true)
                }
            }.collect { filtered ->
                _state.value = _state.value.copy(filteredPedidos = filtered)
            }
        }
    }

    fun onSearchChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }
}
```

### `presentation/viewmodel/AlertasViewModel.kt`
Maneja el estado de la pantalla de alertas: lista de pedidos, filtro por estado y el pedido seleccionado para ver su detalle.

```kotlin
data class AlertasState(
    val pedidos: List<Pedido> = emptyList(),
    val filtro: String = "Todos",
    val pedidoDetalle: Pedido? = null
)

class AlertasViewModel : ViewModel() {
    private val repo = AppModule.pedidoRepository

    private val _state = MutableStateFlow(AlertasState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getPedidos().collect { lista ->
                _state.update { it.copy(pedidos = lista) }
            }
        }
    }

    fun setFiltro(filtro: String) { _state.update { it.copy(filtro = filtro) } }
    fun verDetalle(pedido: Pedido) { _state.update { it.copy(pedidoDetalle = pedido) } }
    fun cerrarDetalle() { _state.update { it.copy(pedidoDetalle = null) } }
}
```

---

## Domain / Model

### `domain/model/Enums.kt`
Define las enumeraciones centrales del sistema: `EstadoPedido` (pendiente, en preparación, listo, entregado, cancelado), `EstadoMesa` (libre, ocupada, reservada, fuera de servicio), `EstadoZona` y `RolUsuario`.

```kotlin
enum class EstadoPedido {
    PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO
}

enum class EstadoMesa {
    LIBRE, OCUPADA, RESERVADA, FUERA_DE_SERVICIO
}

enum class EstadoZona {
    DISPONIBLE, NO_DISPONIBLE
}

enum class RolUsuario {
    MESERO, CHEF, CAJERO, ADMIN
}

enum class EstadoUsuario {
    ACTIVO, INACTIVO, EN_DESCANSO
}
```

### `domain/model/Usuario.kt`
Entidad que representa a un usuario del sistema (mesero o administrador): id, nombre, rol, estado, zona asignada y avatar.

```kotlin
data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val rol: RolUsuario = RolUsuario.MESERO,
    val activo: Boolean = true,
    val estadoUsuario: EstadoUsuario = EstadoUsuario.ACTIVO,
    val zonaId: String = "",
    val zonaAsignada: String = "",
    val fotoEmoji: String = "👤"
)
```

### `domain/model/Mesa.kt`
Entidad que representa una mesa del restaurante: número, estado, capacidad, mesero asignado y zona a la que pertenece.

```kotlin
data class Mesa(
    val id: Int = 0,
    val numero: Int = 0,
    val estado: EstadoMesa = EstadoMesa.LIBRE,
    val capacidad: Int = 4,
    val meseroAsignado: String = "",
    val zonaId: String = ""
)
```

### `domain/model/Zona.kt`
Entidad que representa una zona del restaurante (categoría A, B o C) y su estado de disponibilidad.

```kotlin
data class Zona(
    val id: String = "",
    val nombreZona: String = "",
    val estadoZona: EstadoZona = EstadoZona.DISPONIBLE
)
```

### `domain/model/Platillo.kt`
Entidad que representa un platillo del menú: nombre, precio, categoría, disponibilidad e ingredientes.

```kotlin
data class Platillo(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val disponible: Boolean = true,
    val ingredientes: List<String> = emptyList(),
    val emoji: String = "🍽️"
)
```

### `domain/model/Pedido.kt`
Entidad que representa una comanda: mesa, mesero, platillos seleccionados, nota, estado, total y timestamp.

```kotlin
data class Pedido(
    val id: String = "",
    val mesa: Int = 0,
    val mesaId: Int = 0,
    val meseroId: String = "",
    val descripcion: String = "",
    val nota: String = "",
    val estado: EstadoPedido = EstadoPedido.PENDIENTE,
    val total: Double = 0.0,
    val timestamp: Long = 0,
    val platillos: List<PlatilloSeleccionado> = emptyList(),
    val usuarioId: String = ""
)

data class PlatilloSeleccionado(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1,
    val nota: String = ""
)
```

### `domain/model/Notificacion.kt`
Entidad que representa una notificación asociada a un pedido, usada para el flujo de alertas hacia el mesero.

```kotlin
data class Notificacion(
    val id: String = "",
    val pedidoId: String = "",
    val usuarioId: String = "",
    val mensaje: String = "",
    val confirmada: Boolean = false
)
```

### `domain/model/Turno.kt`
Entidad que representa el turno de trabajo de un usuario: hora de inicio y hora de fin.

```kotlin
data class Turno(
    val id: String = "",
    val horaInicio: Long = 0,
    val horaFin: Long? = null,
    val usuarioId: String = ""
)
```

---

## Domain / Repository (interfaces)

### `domain/repository/AuthRepository.kt`
Contrato de autenticación: login como administrador, login de usuario por nombre y registro de nuevos usuarios.

```kotlin
interface AuthRepository {
    suspend fun loginAsAdmin(): Result<Usuario>
    suspend fun login(nombreUsuario: String): Result<Usuario>
    suspend fun register(usuario: Usuario): Result<Unit>
}
```

### `domain/repository/UsuarioRepository.kt`
Contrato para la gestión de usuarios: observar la lista en tiempo real, agregar, actualizar y eliminar.

```kotlin
interface UsuarioRepository {
    fun getUsuarios(): Flow<List<Usuario>>
    suspend fun addUsuario(usuario: Usuario)
    suspend fun updateUsuario(usuario: Usuario)
    suspend fun deleteUsuario(id: String)
}
```

### `domain/repository/MesaRepository.kt`
Contrato para la gestión de mesas: observar todas las mesas en tiempo real, agregar, actualizar y eliminar.

```kotlin
interface MesaRepository {
    fun getTodasLasMesas(): Flow<List<Mesa>>
    suspend fun addMesa(mesa: Mesa)
    suspend fun updateMesa(mesa: Mesa)
    suspend fun deleteMesa(id: Int)
}
```

### `domain/repository/ZonaRepository.kt`
Contrato para la gestión de zonas: observar la lista en tiempo real, agregar, actualizar y eliminar.

```kotlin
interface ZonaRepository {
    fun getZonas(): Flow<List<Zona>>
    suspend fun addZona(zona: Zona)
    suspend fun updateZona(zona: Zona)
    suspend fun deleteZona(id: String)
}
```

### `domain/repository/MenuRepository.kt`
Contrato para la gestión del menú: observar los platillos en tiempo real, agregar, actualizar y eliminar.

```kotlin
interface MenuRepository {
    fun getMenu(): Flow<List<Platillo>>
    suspend fun addPlatillo(platillo: Platillo)
    suspend fun updatePlatillo(platillo: Platillo)
    suspend fun deletePlatillo(id: String)
}
```

### `domain/repository/PedidoRepository.kt`
Contrato para la gestión de pedidos: observar los pedidos en tiempo real, agregar un pedido con sus platillos, actualizar su estado y eliminarlo.

```kotlin
interface PedidoRepository {
    fun getPedidos(): Flow<List<Pedido>>
    suspend fun addPedido(pedido: Pedido, items: List<Map<String, String>>)
    suspend fun updateEstado(pedidoId: String, nuevoEstado: EstadoPedido)
    suspend fun deletePedido(pedidoId: String)
}
```

---

## Data / Source

### `data/source/AuthDataSource.kt`
Acceso directo a Firebase Realtime Database para las operaciones de autenticación (nodo `usuarios`): validar credenciales y registrar nuevos usuarios.

```kotlin
class AuthDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("usuarios")

    suspend fun loginAsAdmin(): Usuario? {
        return Usuario(id = "admin", nombre = "Administrador", rol = RolUsuario.ADMIN)
    }

    suspend fun login(nombreUsuario: String): Usuario? {
        val snapshot = database.orderByChild("nombre")
            .equalTo(nombreUsuario.trim())
            .get()
            .await()
        if (snapshot.exists()) {
            val child = snapshot.children.first()
            return mapToUsuario(child)
        }
        return null
    }

    suspend fun register(usuario: Usuario) {
        val key = database.push().key ?: ""
        database.child(key).setValue(mapFromUsuario(usuario.copy(id = key))).await()
    }

    private fun mapToUsuario(snapshot: DataSnapshot): Usuario {
        return Usuario(
            id = snapshot.key ?: "",
            nombre = snapshot.child("nombre").value?.toString() ?: "",
            rol = try {
                RolUsuario.valueOf(snapshot.child("rol").value?.toString() ?: "MESERO")
            } catch (e: Exception) {
                RolUsuario.MESERO
            },
            activo = snapshot.child("activo").value as? Boolean ?: false,
            estadoUsuario = try {
                EstadoUsuario.valueOf(snapshot.child("estadoUsuario").value?.toString() ?: "ACTIVO")
            } catch (e: Exception) {
                EstadoUsuario.ACTIVO
            },
            zonaId = snapshot.child("zonaId").value?.toString() ?: "",
            zonaAsignada = snapshot.child("zonaAsignada").value?.toString() ?: "",
            fotoEmoji = snapshot.child("fotoEmoji").value?.toString() ?: "👤"
        )
    }

    private fun mapFromUsuario(usuario: Usuario): Map<String, Any?> {
        return mapOf(
            "id" to usuario.id,
            "nombre" to usuario.nombre,
            "rol" to usuario.rol.name,
            "activo" to usuario.activo,
            "estadoUsuario" to usuario.estadoUsuario.name,
            "zonaId" to usuario.zonaId,
            "zonaAsignada" to usuario.zonaAsignada,
            "fotoEmoji" to usuario.fotoEmoji
        )
    }
}
```

### `data/source/UsuarioDataSource.kt`
Acceso directo a Firebase para el CRUD del nodo `usuarios`, exponiendo los cambios como `Flow` mediante `callbackFlow`.

```kotlin
class UsuarioDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("usuarios")

    fun getUsuarios(): Flow<List<Usuario>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuarios = mutableListOf<Usuario>()
                snapshot.children.forEach { child ->
                    try {
                        usuarios.add(
                            Usuario(
                                id = child.key ?: "",
                                nombre = child.child("nombre").value?.toString() ?: "",
                                rol = try { RolUsuario.valueOf(child.child("rol").value?.toString() ?: "MESERO") } catch(e: Exception) { RolUsuario.MESERO },
                                activo = child.child("activo").value as? Boolean ?: false,
                                estadoUsuario = try { EstadoUsuario.valueOf(child.child("estadoUsuario").value?.toString() ?: "ACTIVO") } catch(e: Exception) { EstadoUsuario.ACTIVO },
                                zonaId = child.child("zonaId").value?.toString() ?: "",
                                zonaAsignada = child.child("zonaAsignada").value?.toString() ?: "",
                                fotoEmoji = child.child("fotoEmoji").value?.toString() ?: "👤"
                            )
                        )
                    } catch (_: Exception) {}
                }
                trySend(usuarios)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    suspend fun addUsuario(usuario: Usuario) {
        val key = database.push().key ?: ""
        database.child(key).setValue(mapFromUsuario(usuario.copy(id = key))).await()
    }

    suspend fun updateUsuario(usuario: Usuario) {
        database.child(usuario.id).updateChildren(mapFromUsuario(usuario)).await()
    }

    suspend fun deleteUsuario(id: String) {
        database.child(id).removeValue().await()
    }

    private fun mapFromUsuario(u: Usuario): Map<String, Any?> = mapOf(
        "id" to u.id,
        "nombre" to u.nombre,
        "rol" to u.rol.name,
        "activo" to u.activo,
        "estadoUsuario" to u.estadoUsuario.name,
        "zonaId" to u.zonaId,
        "zonaAsignada" to u.zonaAsignada,
        "fotoEmoji" to u.fotoEmoji
    )
}
```

### `data/source/MesaDataSource.kt`
Acceso directo a Firebase para el CRUD del nodo `mesas_config` y la observación en tiempo real del estado de cada mesa.

```kotlin
class MesaDataSource {
    private val mesasConfigRef = FirebaseDatabase.getInstance().getReference("mesas_config")
    private val pedidosRef = FirebaseDatabase.getInstance().getReference("pedidos")

    /**
     * Obtiene la configuración de mesas personalizada.
     */
    fun getMesasConfig(): Flow<List<Mesa>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mesas = mutableListOf<Mesa>()
                snapshot.children.forEach { child ->
                    val id = child.child("id").value.toString().toDoubleOrNull()?.toInt() ?: 0
                    if (id > 0) {
                        mesas.add(
                            Mesa(
                                id = id,
                                capacidad = child.child("capacidad").value.toString().toDoubleOrNull()?.toInt() ?: 4,
                                estado = EstadoMesa.LIBRE // se calculará después
                            )
                        )
                    }
                }
                trySend(mesas)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        mesasConfigRef.addValueEventListener(listener)
        awaitClose { mesasConfigRef.removeEventListener(listener) }
    }

    /**
     * Escucha los pedidos activos para saber qué mesas están ocupadas.
     */
    fun getMesasOcupadas(): Flow<Set<Int>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ocupadas = mutableSetOf<Int>()
                snapshot.children.forEach { child ->
                    val mesa = child.child("mesa").value.toString().toDoubleOrNull()?.toInt() ?: 0
                    val estado = child.child("estado").value?.toString() ?: ""
                    if (estado != "ENTREGADO" && estado != "CANCELADO") {
                        ocupadas.add(mesa)
                    }
                }
                trySend(ocupadas)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        pedidosRef.addValueEventListener(listener)
        awaitClose { pedidosRef.removeEventListener(listener) }
    }

    suspend fun addMesa(mesa: Mesa) {
        mesasConfigRef.child(mesa.id.toString()).setValue(
            mapOf("id" to mesa.id, "capacidad" to mesa.capacidad, "estado" to "LIBRE")
        ).await()
    }

    suspend fun updateMesa(mesa: Mesa) {
        mesasConfigRef.child(mesa.id.toString()).updateChildren(
            mapOf("capacidad" to mesa.capacidad)
        ).await()
    }

    suspend fun deleteMesa(id: Int) {
        mesasConfigRef.child(id.toString()).removeValue().await()
    }
}
```

### `data/source/ZonaDataSource.kt`
Acceso directo a Firebase para el CRUD del nodo `zonas`.

```kotlin
class ZonaDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("zonas")

    fun getZonas(): Flow<List<Zona>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val zonas = mutableListOf<Zona>()
                snapshot.children.forEach { child ->
                    zonas.add(
                        Zona(
                            id = child.key ?: "",
                            nombreZona = child.child("nombreZona").value?.toString() ?: "",
                            estadoZona = try {
                                EstadoZona.valueOf(child.child("estadoZona").value?.toString() ?: "DISPONIBLE")
                            } catch (e: Exception) {
                                EstadoZona.DISPONIBLE
                            }
                        )
                    )
                }
                trySend(zonas)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    suspend fun addZona(zona: Zona) {
        val key = database.push().key ?: ""
        database.child(key).setValue(
            mapOf(
                "id" to key,
                "nombreZona" to zona.nombreZona,
                "estadoZona" to zona.estadoZona.name
            )
        ).await()
    }

    suspend fun updateZona(zona: Zona) {
        database.child(zona.id).updateChildren(
            mapOf(
                "nombreZona" to zona.nombreZona,
                "estadoZona" to zona.estadoZona.name
            )
        ).await()
    }

    suspend fun deleteZona(id: String) {
        database.child(id).removeValue().await()
    }
}
```

### `data/source/MenuDataSource.kt`
Acceso directo a Firebase para el CRUD del nodo `menu` (platillos).

```kotlin
class MenuDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("menu")

    fun getMenu(): Flow<List<Platillo>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val menu = mutableListOf<Platillo>()
                snapshot.children.forEach { child ->
                    try {
                        val p = Platillo(
                            id = child.key ?: "",
                            nombre = child.child("nombre").value?.toString() ?: "",
                            precio = child.child("precio").value.toString().toDoubleOrNull() ?: 0.0,
                            categoria = child.child("categoria").value?.toString() ?: "Platos",
                            disponible = child.child("disponible").value as? Boolean ?: true,
                            emoji = child.child("emoji").value?.toString() ?: "🍽️"
                        )
                        menu.add(p)
                    } catch (_: Exception) {}
                }
                trySend(menu)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    suspend fun addPlatillo(platillo: Platillo) {
        val key = database.push().key ?: ""
        database.child(key).setValue(mapPlatillo(platillo.copy(id = key))).await()
    }

    suspend fun updatePlatillo(platillo: Platillo) {
        database.child(platillo.id).updateChildren(mapPlatillo(platillo)).await()
    }

    suspend fun deletePlatillo(id: String) {
        database.child(id).removeValue().await()
    }

    private fun mapPlatillo(p: Platillo): Map<String, Any?> = mapOf(
        "id" to p.id,
        "nombre" to p.nombre,
        "precio" to p.precio,
        "categoria" to p.categoria,
        "disponible" to p.disponible,
        "emoji" to p.emoji
    )
}
```

### `data/source/PedidoDataSource.kt`
Acceso directo a Firebase para el CRUD del nodo de pedidos, incluyendo el registro de los platillos seleccionados por comanda y la actualización de su estado.

```kotlin
class PedidoDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("pedidos")

    fun getPedidos(): Flow<List<Pedido>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pedidos = mutableListOf<Pedido>()
                snapshot.children.forEach { child ->
                    try {
                        val estadoStr = child.child("estado").value?.toString() ?: "PENDIENTE"
                        val items = mutableListOf<PlatilloSeleccionado>()
                        child.child("items").children.forEach { itemSnap ->
                            val desc = itemSnap.child("descripcion").value?.toString() ?: ""
                            val nota = itemSnap.child("nota").value?.toString() ?: ""
                            items.add(PlatilloSeleccionado(nombre = desc, nota = nota))
                        }
                        val p = Pedido(
                            id = child.key ?: "",
                            mesa = child.child("mesa").value.toString().toDoubleOrNull()?.toInt() ?: 0,
                            descripcion = child.child("descripcion").value?.toString() ?: "",
                            estado = try { EstadoPedido.valueOf(estadoStr) } catch(e: Exception) { EstadoPedido.PENDIENTE },
                            total = child.child("total").value.toString().toDoubleOrNull() ?: 0.0,
                            timestamp = child.child("timestamp").value.toString().toLongOrNull() ?: 0L,
                            platillos = items,
                            nota = child.child("nota").value?.toString() ?: "",
                            meseroId = child.child("meseroId").value?.toString() ?: "",
                            usuarioId = child.child("usuarioId").value?.toString() ?: ""
                        )
                        pedidos.add(p)
                    } catch (_: Exception) {}
                }
                trySend(pedidos)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    suspend fun addPedido(pedido: Pedido, items: List<Map<String, String>>) {
        val key = database.push().key ?: "p"
        val nuevo = hashMapOf(
            "id" to key,
            "mesa" to pedido.mesa.toLong(),
            "descripcion" to pedido.descripcion,
            "nota" to pedido.nota,
            "estado" to pedido.estado.name,
            "total" to pedido.total,
            "timestamp" to ServerValue.TIMESTAMP,
            "items" to items,
            "meseroId" to pedido.meseroId,
            "usuarioId" to pedido.usuarioId
        )
        database.child(key).setValue(nuevo).await()
    }

    suspend fun updateEstado(pedidoId: String, nuevoEstado: EstadoPedido) {
        database.child(pedidoId).child("estado").setValue(nuevoEstado.name).await()
    }

    suspend fun deletePedido(pedidoId: String) {
        database.child(pedidoId).removeValue().await()
    }
}
```

---

## Data / Repository (implementaciones)

### `data/repository/AuthRepositoryImpl.kt`
Implementa `AuthRepository` delegando las operaciones a `AuthDataSource`, actuando como puente entre la capa de dominio y Firebase.

```kotlin
class AuthRepositoryImpl(private val dataSource: AuthDataSource) : AuthRepository {

    override suspend fun loginAsAdmin(): Result<Usuario> {
        return try {
            val admin = dataSource.loginAsAdmin()
            if (admin != null) Result.success(admin)
            else Result.failure(Exception("No se pudo autenticar como admin"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(nombreUsuario: String): Result<Usuario> {
        return try {
            val user = dataSource.login(nombreUsuario)
            if (user != null) Result.success(user)
            else Result.failure(Exception("Usuario no encontrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(usuario: Usuario): Result<Unit> {
        return try {
            dataSource.register(usuario)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### `data/repository/UsuarioRepositoryImpl.kt`
Implementa `UsuarioRepository` delegando las operaciones a `UsuarioDataSource`.

```kotlin
class UsuarioRepositoryImpl(private val dataSource: UsuarioDataSource) : UsuarioRepository {
    override fun getUsuarios(): Flow<List<Usuario>> = dataSource.getUsuarios()
    override suspend fun addUsuario(usuario: Usuario) = dataSource.addUsuario(usuario)
    override suspend fun updateUsuario(usuario: Usuario) = dataSource.updateUsuario(usuario)
    override suspend fun deleteUsuario(id: String) = dataSource.deleteUsuario(id)
}
```

### `data/repository/MesaRepositoryImpl.kt`
Implementa `MesaRepository` delegando las operaciones a `MesaDataSource`.

```kotlin
class MesaRepositoryImpl(private val dataSource: MesaDataSource) : MesaRepository {

    override fun getTodasLasMesas(): Flow<List<Mesa>> {
        return combine(
            dataSource.getMesasConfig(),
            dataSource.getMesasOcupadas()
        ) { mesasConfig, ocupadas ->
            // Mesas base del 1 al 12
            val idsConfig = mesasConfig.map { it.id }.toSet()
            val mesasBase = (1..12)
                .filter { it !in idsConfig }
                .map { Mesa(id = it, numero = it, capacidad = 4) }

            // Combinamos
            val todas = (mesasBase + mesasConfig).sortedBy { it.id }.map { mesa ->
                val ocupada = mesa.id in ocupadas
                mesa.copy(estado = if (ocupada) EstadoMesa.OCUPADA else EstadoMesa.LIBRE)
            }
            todas
        }
    }

    override suspend fun addMesa(mesa: Mesa) = dataSource.addMesa(mesa)
    override suspend fun updateMesa(mesa: Mesa) = dataSource.updateMesa(mesa)
    override suspend fun deleteMesa(id: Int) = dataSource.deleteMesa(id)
}
```

### `data/repository/ZonaRepositoryImpl.kt`
Implementa `ZonaRepository` delegando las operaciones a `ZonaDataSource`.

```kotlin
class ZonaRepositoryImpl(private val dataSource: ZonaDataSource) : ZonaRepository {
    override fun getZonas(): Flow<List<Zona>> = dataSource.getZonas()
    override suspend fun addZona(zona: Zona) = dataSource.addZona(zona)
    override suspend fun updateZona(zona: Zona) = dataSource.updateZona(zona)
    override suspend fun deleteZona(id: String) = dataSource.deleteZona(id)
}
```

### `data/repository/MenuRepositoryImpl.kt`
Implementa `MenuRepository` delegando las operaciones a `MenuDataSource`.

```kotlin
class MenuRepositoryImpl(private val dataSource: MenuDataSource) : MenuRepository {
    override fun getMenu(): Flow<List<Platillo>> = dataSource.getMenu()

    override suspend fun addPlatillo(platillo: Platillo) = dataSource.addPlatillo(platillo)
    override suspend fun updatePlatillo(platillo: Platillo) = dataSource.updatePlatillo(platillo)
    override suspend fun deletePlatillo(id: String) = dataSource.deletePlatillo(id)
}
```

### `data/repository/PedidoRepositoryImpl.kt`
Implementa `PedidoRepository` delegando las operaciones a `PedidoDataSource`.

```kotlin
class PedidoRepositoryImpl(private val dataSource: PedidoDataSource) : PedidoRepository {
    override fun getPedidos(): Flow<List<Pedido>> = dataSource.getPedidos()

    override suspend fun addPedido(pedido: Pedido, items: List<Map<String, String>>) =
        dataSource.addPedido(pedido, items)

    override suspend fun updateEstado(pedidoId: String, nuevoEstado: EstadoPedido) =
        dataSource.updateEstado(pedidoId, nuevoEstado)

    override suspend fun deletePedido(pedidoId: String) = dataSource.deletePedido(pedidoId)
}
```



## [Regresar al README principal](/README.md)