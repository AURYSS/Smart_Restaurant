# Código del módulo móvil

## [Regresar al README principal](/README.md)

Este documento describe el propósito de cada archivo del módulo `mobile`, organizado según la arquitectura por capas (Clean Architecture): `presentation`, `domain` y `data`.

---

## Raíz del módulo

### `MainActivity.kt`
Actividad principal y punto de entrada de la aplicación. Se encarga de instanciar el contenido de Compose, aplicar el tema general (`MaterialTheme`) y arrancar el grafo de navegación (`AppNavigation`) donde inicia el flujo con la pantalla de login.

```kotlin
/**
 * Actividad principal y punto de entrada de la aplicación móvil de MeseroWatch.
 *
 * Configura la superficie de MaterialTheme y carga el componente raíz de navegación [AppNavigation].
 */
class MainActivity : ComponentActivity() {

    /**
     * Inicializa la actividad e infla la interfaz de usuario basada en Jetpack Compose.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, si existe.
     */
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
/**
 * Objeto Singleton que administra el estado de la sesión activa del usuario en memoria.
 */
object SessionManager {
    /**
     * Usuario autenticado actualmente en la aplicación, o `null` si no hay sesión iniciada.
     */
    var currentUser by mutableStateOf<Usuario?>(null)

    /**
     * Indica si el usuario en sesión posee privilegios de administrador.
     */
    var isAdmin by mutableStateOf(false)

    /**
     * Inicia una sesión con un usuario predeterminado con rol de Administrador.
     */
    fun loginAsAdmin() {
        currentUser = Usuario(id = "admin", nombre = "Administrador", rol = RolUsuario.ADMIN)
        isAdmin = true
    }

    /**
     * Registra la sesión activa a partir de un objeto [Usuario] autenticado.
     *
     * @param usuario Instancia del usuario que inició sesión.
     */
    fun loginAsUser(usuario: Usuario) {
        currentUser = usuario
        isAdmin = usuario.rol == RolUsuario.ADMIN
    }

    /**
     * Limpia la sesión actual y restablece las banderas de autenticación.
     */
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
/**
 * Objeto Singleton que administra el estado de la sesión activa del usuario en memoria.
 */
object SessionManager {
    /**
     * Usuario autenticado actualmente en la aplicación, o `null` si no hay sesión iniciada.
     */
    var currentUser by mutableStateOf<Usuario?>(null)

    /**
     * Indica si el usuario en sesión posee privilegios de administrador.
     */
    var isAdmin by mutableStateOf(false)

    /**
     * Inicia una sesión con un usuario predeterminado con rol de Administrador.
     */
    fun loginAsAdmin() {
        currentUser = Usuario(id = "admin", nombre = "Administrador", rol = RolUsuario.ADMIN)
        isAdmin = true
    }

    /**
     * Registra la sesión activa a partir de un objeto [Usuario] autenticado.
     *
     * @param usuario Instancia del usuario que inició sesión.
     */
    fun loginAsUser(usuario: Usuario) {
        currentUser = usuario
        isAdmin = usuario.rol == RolUsuario.ADMIN
    }

    /**
     * Limpia la sesión actual y restablece las banderas de autenticación.
     */
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
/**
 * Contenedor de inyección de dependencias manual a nivel de aplicación.
 *
 * Provee instancias perezosas (lazy) de las fuentes de datos y repositorios del sistema.
 */
object AppModule {
    // Data sources
    private val authDataSource by lazy { AuthDataSource() }
    private val menuDataSource by lazy { MenuDataSource() }
    private val pedidoDataSource by lazy { PedidoDataSource() }
    private val mesaDataSource by lazy { MesaDataSource() }
    private val zonaDataSource by lazy { ZonaDataSource() }
    private val usuarioDataSource by lazy { UsuarioDataSource() }

    // Repositories
    /** Repositorio para operaciones de autenticación de usuarios. */
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(authDataSource) }

    /** Repositorio para la gestión del catálogo de platillos y menú. */
    val menuRepository: MenuRepository by lazy { MenuRepositoryImpl(menuDataSource) }

    /** Repositorio para la creación y seguimiento de comandas/pedidos. */
    val pedidoRepository: PedidoRepository by lazy { PedidoRepositoryImpl(pedidoDataSource) }

    /** Repositorio para el control y asignación de mesas. */
    val mesaRepository: MesaRepository by lazy { MesaRepositoryImpl(mesaDataSource) }

    /** Repositorio para la configuración de zonas del restaurante. */
    val zonaRepository: ZonaRepository by lazy { ZonaRepositoryImpl(zonaDataSource) }

    /** Repositorio para la gestión de usuarios, roles y personal. */
    val usuarioRepository: UsuarioRepository by lazy { UsuarioRepositoryImpl(usuarioDataSource) }
}
```

---

## Presentation / UI (Pantallas Compose)

### `presentation/ui/login/LoginScreen.kt`
Pantalla de inicio de sesión. Contiene los campos de usuario y contraseña, el botón de acceso, el enlace a registro y el manejo visual de errores de autenticación.

```kotlin
/**
 * Pantalla de inicio de sesión con validación interactiva de credenciales y reglas de contraseña.
 *
 * @param onLoginSuccess Callback ejecutado cuando la autenticación resulta exitosa.
 * @param onNavigateToRegister Callback para redirigir al formulario de registro.
 * @param viewModel ViewModel encargado de la lógica y estado de inicio de sesión.
 */
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
            model = "[https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1200&auto=format&fit=crop](https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1200&auto=format&fit=crop)",
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

/**
 * Fila visual de validación que muestra el estado de cumplimiento de una regla de contraseña.
 *
 * @param text Descripción del requisito a evaluar.
 * @param isMet Indica si la condición se cumple satisfactoriamente.
 */
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
/**
 * Pantalla de registro para nuevos empleados, con validación de requisitos de seguridad en tiempo real.
 *
 * @param onRegisterSuccess Callback invocado tras completar el registro exitosamente.
 * @param onBackToLogin Callback para regresar a la pantalla de login.
 * @param viewModel ViewModel que maneja la lógica y envío de datos de registro.
 */
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
            model = "[https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1200&auto=format&fit=crop](https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1200&auto=format&fit=crop)",
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
                        visualTransformation = if (state.passwordVisible) Visual
```

### `presentation/ui/admin/AdminDashboardScreen.kt`
Panel principal del administrador. Muestra los indicadores clave del restaurante: ventas del día, total de pedidos, pedidos en curso, personal activo y ocupación de mesas.

```kotlin
/**
 * Pantalla principal del panel administrativo.
 *
 * Presenta un resumen de los indicadores más importantes del restaurante,
 * como ventas, pedidos, personal activo y ocupación de mesas. Además,
 * proporciona accesos rápidos a los distintos módulos del sistema.
 *
 * @param onNavigateTo Función utilizada para navegar a otra pantalla.
 * @param viewModel ViewModel encargado de proporcionar el estado del dashboard.
 */
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
/**
 * Pantalla principal para la creación de un nuevo pedido.
 *
 * Coordina el flujo de selección de mesa, elección de platillos
 * y resumen del pedido antes de enviarlo a cocina.
 *
 * @param onNavigateToAlertas Acción ejecutada cuando el pedido
 * se envía correctamente.
 * @param viewModel ViewModel encargado de administrar el estado
 * del flujo de creación del pedido.
 */
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
/**
 * Chip de filtro seleccionable, usado para filtrar platillos por categoría.
 *
 * @param text Texto que se muestra dentro del chip.
 * @param isSelected Indica si el chip está actualmente seleccionado, lo que cambia su color de fondo y texto.
 * @param onClick Callback que se ejecuta al pulsar el chip.
 */
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

/**
 * Pantalla administrativa del menú del restaurante.
 *
 * Muestra la lista de platillos registrados, permite filtrarlos por categoría,
 * buscarlos, agregar nuevos platillos y editar o eliminar los existentes.
 * Los diálogos de creación y edición se muestran condicionalmente según el
 * estado local de la pantalla.
 *
 * @param viewModel ViewModel que expone el estado del menú y las operaciones
 * de agregar, actualizar y eliminar platillos.
 */
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

/**
 * Tarjeta que representa un platillo dentro de la lista administrativa del menú.
 *
 * Muestra el emoji, nombre, categoría y precio del platillo, junto con acciones
 * para editarlo o eliminarlo.
 *
 * @param platillo Platillo a mostrar.
 * @param onEdit Callback que se ejecuta al pulsar el botón "Editar".
 * @param onDelete Callback que se ejecuta al pulsar el botón de eliminar.
 */
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

/**
 * Diálogo para registrar un nuevo platillo en el menú.
 *
 * Permite capturar el nombre, la categoría (mediante un menú desplegable) y el
 * precio del platillo. El emoji se asigna automáticamente según la categoría
 * seleccionada. El botón "Guardar" solo confirma la creación si el nombre y el
 * precio no están vacíos.
 *
 * @param onDismiss Callback que se ejecuta al cerrar el diálogo sin guardar.
 * @param onGuardar Callback que se ejecuta con el nuevo [Platillo] al confirmar el guardado.
 */
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

/**
 * Diálogo para editar un platillo existente del menú.
 *
 * Precarga los campos con los valores actuales del [platillo] recibido y
 * permite modificar su nombre, categoría y precio. El emoji se recalcula
 * automáticamente según la categoría seleccionada. El botón "Actualizar" solo
 * confirma los cambios si el nombre y el precio no están vacíos.
 *
 * @param platillo Platillo original que se va a editar.
 * @param onDismiss Callback que se ejecuta al cerrar el diálogo sin guardar cambios.
 * @param onGuardar Callback que se ejecuta con el [Platillo] actualizado al confirmar los cambios.
 */
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
/**
 * Pantalla de administración de personal (usuarios/meseros).
 *
 * Muestra el listado de empleados registrados, permite filtrarlos por estado
 * (activo, inactivo, en descanso), agregar nuevos usuarios, editar los
 * existentes y navegar hacia la pantalla de zonas.
 *
 * @param onNavigateToZonas Callback que se ejecuta al pulsar el botón "Zonas" para navegar a esa pantalla.
 * @param viewModel ViewModel que expone el estado de usuarios y las operaciones de agregar, editar y actualizar.
 */
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

/**
 * Fila que representa a un usuario dentro del listado de personal.
 *
 * Muestra su foto (emoji), nombre, rol, zona asignada y estado actual
 * (activo, inactivo o en descanso) mediante un color distintivo.
 *
 * @param usuario Usuario a mostrar.
 * @param onClick Callback que se ejecuta al pulsar sobre la fila, típicamente para editar al usuario.
 */
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

/**
 * Diálogo para registrar un nuevo usuario (empleado).
 *
 * Permite capturar el nombre completo, seleccionar el rol mediante un menú
 * desplegable y elegir el estado inicial (activo, inactivo, en descanso)
 * mediante chips. El botón "Guardar usuario" solo confirma la creación si el
 * nombre no está vacío.
 *
 * @param onDismiss Callback que se ejecuta al cerrar el diálogo sin guardar.
 * @param onGuardar Callback que se ejecuta con el nuevo [Usuario] al confirmar el guardado.
 */
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

/**
 * Diálogo para editar un usuario existente.
 *
 * Precarga los campos con los valores actuales del [usuario] recibido y
 * permite modificar su nombre, rol, zona y estado. A diferencia del diálogo
 * de creación, "Actualizar usuario" guarda los cambios sin validar que el
 * nombre no esté vacío.
 *
 * @param usuario Usuario original que se va a editar.
 * @param onDismiss Callback que se ejecuta al cerrar el diálogo sin guardar cambios.
 * @param onGuardar Callback que se ejecuta con el [Usuario] actualizado al confirmar los cambios.
 */
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
/**
 * Pantalla de gestión de zonas del restaurante.
 *
 * Agrupa las zonas por clasificación ("Zona A", "Zona B", "Zona C" y "Otras
 * Zonas") y muestra, para cada una, una tarjeta expandible con el personal
 * asignado. Permite crear nuevas zonas y editar una existente mediante
 * pulsación larga (long-press) sobre su tarjeta.
 *
 * @param viewModel ViewModel que expone el estado de zonas y usuarios, y las operaciones de agregar, editar, actualizar y eliminar zonas.
 */
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

/**
 * Tarjeta expandible que representa una zona y el personal asignado a ella.
 *
 * Muestra el nombre de la zona, su estado (disponible/no disponible) y la
 * cantidad de personas asignadas. Si hay personal asignado, al pulsar la
 * tarjeta se expande o colapsa la lista de empleados con su estado de turno;
 * una pulsación larga dispara la edición de la zona.
 *
 * @param zona Zona a mostrar.
 * @param personal Lista de usuarios asignados a esta zona.
 * @param onLongClick Callback que se ejecuta al mantener presionada la tarjeta, típicamente para editar la zona.
 */
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

/**
 * Diálogo para registrar una nueva zona.
 *
 * Permite capturar el nombre de la zona y elegir su estado inicial
 * (disponible/no disponible) mediante chips. El botón "Guardar zona" solo
 * confirma la creación si el nombre no está vacío.
 *
 * @param onDismiss Callback que se ejecuta al cerrar el diálogo sin guardar.
 * @param onGuardar Callback que se ejecuta con la nueva [Zona] al confirmar el guardado.
 */
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

/**
 * Diálogo para editar una zona existente.
 *
 * Precarga los campos con los valores actuales de la [zona] recibida y
 * permite modificar su nombre y estado, además de ofrecer la opción de
 * eliminarla.
 *
 * @param zona Zona original que se va a editar.
 * @param onDismiss Callback que se ejecuta al cerrar el diálogo sin guardar cambios.
 * @param onGuardar Callback que se ejecuta con la [Zona] actualizada al confirmar los cambios.
 * @param onEliminar Callback que se ejecuta al pulsar el botón "Eliminar".
 */
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
/**
 * Pantalla de control de turnos del personal.
 *
 * Muestra el listado completo de usuarios junto con su información de turno,
 * permite registrar un nuevo usuario y editar uno existente a través de los
 * diálogos correspondientes.
 *
 * @param viewModel ViewModel que expone el estado de usuarios y las operaciones de agregar, editar y actualizar.
 */
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

/**
 * Fila que representa a un usuario dentro del listado de turnos.
 *
 * Muestra su foto (emoji), nombre, rol, zona asignada y el estado actual del
 * turno (activo, inactivo o en descanso).
 *
 * @param usuario Usuario a mostrar.
 * @param onEdit Callback que se ejecuta al pulsar sobre la fila, para editar al usuario.
 */
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

```kotlin
/**
 * Pantalla que muestra los pedidos activos del restaurante a modo de alertas/notificaciones.
 *
 * Presenta un listado de pedidos filtrables por estado (Todos, Listos, En cocina, Cancelados)
 * y permite abrir un diálogo con el detalle de un pedido al seleccionarlo.
 *
 * @param viewModel ViewModel que expone el estado de la pantalla ([AlertasState]) y las
 * acciones disponibles (filtrar, ver detalle, cerrar detalle). Por defecto se obtiene mediante
 * el helper `viewModel()` de Compose.
 */
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

/**
 * Diálogo modal que muestra el detalle completo de un [Pedido]: mesa, resumen/descripción
 * y estado actual (con color asociado según el estado).
 *
 * @param pedido Pedido cuyo detalle se va a mostrar.
 * @param onDismiss Callback invocado al cerrar el diálogo (botón "Cerrar" o al tocar fuera de él).
 */
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

/**
 * Ítem de lista que representa un [Pedido] dentro de la pantalla de alertas.
 *
 * Muestra un indicador de color según el estado del pedido, el número de mesa,
 * una vista previa de la descripción y una etiqueta con el estado (Listo, Cocina, Cancelado, etc.).
 * Al tocarlo, invoca [onClick] para abrir el detalle del pedido.
 *
 * @param pedido Pedido a representar en la fila.
 * @param onClick Callback invocado cuando el usuario toca el ítem.
 */
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

```kotlin
/**
 * Pantalla de historial de pedidos.
 *
 * Muestra la lista completa de pedidos ya procesados (entregados, cancelados, etc.) junto con
 * un campo de búsqueda que permite filtrar por número de mesa o por platillo.
 *
 * @param viewModel ViewModel que expone el estado ([HistorialPedidosState]) con la lista filtrada
 * de pedidos y el texto de búsqueda actual.
 */
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

/**
 * Ítem de lista utilizado en la pantalla de historial para representar un [Pedido] pasado.
 *
 * Muestra la mesa, el estado (con color asociado: entregado, cancelado o intermedio),
 * la descripción del pedido, el mesero asignado (o "Sin asignar" si no tiene) y el total cobrado.
 *
 * @param pedido Pedido a mostrar en la fila del historial.
 */
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

```kotlin
/**
 * Estado de la pantalla de inicio de sesión.
 *
 * @property user Nombre de usuario ingresado.
 * @property password Contraseña ingresada.
 * @property isLoading Indica si hay una operación de login en curso (spinner de carga).
 * @property error Mensaje de error a mostrar, o `null` si no hay error.
 * @property isLoggedIn Indica si el usuario logró iniciar sesión (se usa junto a [loginSuccess]).
 * @property passwordVisible Indica si el campo de contraseña se muestra en texto plano.
 * @property loginSuccess Bandera que señala que el login finalizó con éxito, usada para disparar
 * la navegación a la siguiente pantalla.
 */
data class LoginUiState(
    val user: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val passwordVisible: Boolean = false,
    val loginSuccess: Boolean = false
)

/**
 * ViewModel encargado de la lógica de inicio de sesión.
 *
 * Administra el estado del formulario de login ([LoginUiState]), valida la contraseña
 * según las reglas de seguridad del sistema y delega la autenticación real al
 * [AuthRepository] (obtenido desde [AppModule]). También sincroniza la sesión activa
 * con [SessionManager] al autenticar correctamente.
 */
class LoginViewModel : ViewModel() {
    private val authRepo = AppModule.authRepository

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Actualiza el nombre de usuario en el estado y limpia cualquier error previo.
     *
     * @param value Nuevo valor del campo de usuario.
     */
    fun onUserChanged(value: String) {
        _uiState.value = _uiState.value.copy(user = value, error = null)
    }

    /**
     * Actualiza la contraseña en el estado y limpia cualquier error previo.
     *
     * @param value Nuevo valor del campo de contraseña.
     */
    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    /**
     * Alterna la visibilidad de la contraseña entre texto plano y oculto.
     */
    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }

    /**
     * Ejecuta el proceso de inicio de sesión.
     *
     * Si las credenciales corresponden al usuario administrador fijo (`admin`/`admin123`),
     * autentica directamente como administrador mediante [AuthRepository.loginAsAdmin] y
     * actualiza [SessionManager].
     *
     * En caso contrario, valida que la contraseña cumpla los requisitos de seguridad
     * (longitud mínima de 8 caracteres, al menos un número, una mayúscula y un carácter
     * especial). Si la validación falla, se establece un mensaje de error y se detiene el flujo.
     * Si es válida, intenta autenticar al usuario contra [AuthRepository.login] y, en caso de
     * éxito, inicia sesión mediante [SessionManager.loginAsUser].
     */
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

```kotlin
/**
 * Estado de la pantalla de registro de un nuevo usuario.
 *
 * @property user Nombre de usuario a registrar.
 * @property password Contraseña ingresada.
 * @property confirmPassword Confirmación de la contraseña, debe coincidir con [password].
 * @property rol Rol asignado al nuevo usuario (por defecto [RolUsuario.MESERO]).
 * @property isLoading Indica si el registro está en proceso.
 * @property error Mensaje de error a mostrar, o `null` si no hay error.
 * @property registerSuccess Bandera que indica que el registro finalizó con éxito.
 * @property passwordVisible Indica si el campo de contraseña se muestra en texto plano.
 */
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

/**
 * ViewModel encargado de la lógica de registro de nuevos usuarios.
 *
 * Administra el estado del formulario ([RegisterUiState]), valida los datos ingresados
 * (usuario no vacío, contraseña segura y coincidente con su confirmación) y delega la
 * creación del usuario al [AuthRepository].
 */
class RegisterViewModel : ViewModel() {
    private val authRepo = AppModule.authRepository

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Actualiza el nombre de usuario en el estado y limpia el error actual.
     *
     * @param v Nuevo valor del campo de usuario.
     */
    fun onUserChanged(v: String) { _uiState.value = _uiState.value.copy(user = v, error = null) }

    /**
     * Actualiza la contraseña en el estado y limpia el error actual.
     *
     * @param v Nuevo valor del campo de contraseña.
     */
    fun onPasswordChanged(v: String) { _uiState.value = _uiState.value.copy(password = v, error = null) }

    /**
     * Actualiza la confirmación de contraseña en el estado y limpia el error actual.
     *
     * @param v Nuevo valor del campo de confirmación de contraseña.
     */
    fun onConfirmPasswordChanged(v: String) { _uiState.value = _uiState.value.copy(confirmPassword = v, error = null) }

    /**
     * Actualiza el rol seleccionado para el nuevo usuario.
     *
     * @param rol Nuevo rol asignado ([RolUsuario]).
     */
    fun onRolChanged(rol: RolUsuario) { _uiState.value = _uiState.value.copy(rol = rol) }

    /**
     * Alterna la visibilidad de la contraseña entre texto plano y oculto.
     */
    fun togglePasswordVisibility() { _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible) }

    /**
     * Ejecuta el proceso de registro de un nuevo usuario.
     *
     * Valida que el nombre de usuario no esté vacío y que la contraseña cumpla los
     * requisitos de seguridad (mínimo 8 caracteres, al menos un número, una mayúscula
     * y un carácter especial) y coincida con [RegisterUiState.confirmPassword]. Si alguna
     * validación falla, establece un mensaje de error y detiene el flujo.
     *
     * Si todo es válido, construye un nuevo [Usuario] (activo y con estado
     * [EstadoUsuario.ACTIVO]) y lo registra mediante [AuthRepository.register],
     * actualizando el estado según el resultado.
     */
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

```kotlin
/**
 * Estado del dashboard administrativo con los indicadores clave del restaurante.
 *
 * @property ventasHoy Suma del total de todos los pedidos ya entregados.
 * @property totalPedidos Cantidad total de pedidos registrados (en cualquier estado).
 * @property pedidosEnCurso Cantidad de pedidos que aún no han sido entregados ni cancelados.
 * @property personalActivo Cantidad de usuarios marcados como activos.
 * @property mesasOcupadas Cantidad de mesas actualmente en estado ocupado.
 * @property mesasTotales Cantidad total de mesas registradas en el sistema (por defecto 12).
 */
data class AdminDashboardState(
    val ventasHoy: Double = 0.0,
    val totalPedidos: Int = 0,
    val pedidosEnCurso: Int = 0,
    val personalActivo: Int = 0,
    val mesasOcupadas: Int = 0,
    val mesasTotales: Int = 12
)

/**
 * ViewModel que calcula y expone en tiempo real los indicadores del dashboard administrativo.
 *
 * Combina los flujos de pedidos ([PedidoRepository]), usuarios ([UsuarioRepository]) y mesas
 * ([MesaRepository]) para derivar métricas agregadas (ventas del día, pedidos en curso,
 * personal activo y ocupación de mesas), actualizándolas automáticamente cada vez que
 * cambia alguna de las fuentes.
 */
class AdminDashboardViewModel : ViewModel() {
    private val pedidoRepo = AppModule.pedidoRepository
    private val usuarioRepo = AppModule.usuarioRepository
    private val mesaRepo = AppModule.mesaRepository

    private val _state = MutableStateFlow(AdminDashboardState())
    val state = _state.asStateFlow()

    /**
     * Al inicializarse, se suscribe a los flujos combinados de pedidos, usuarios y mesas,
     * recalculando el [AdminDashboardState] completo cada vez que cualquiera de ellos emite
     * un nuevo valor:
     * - `pedidosEnCurso`: pedidos que no están en estado ENTREGADO ni CANCELADO.
     * - `ventasHoy`: suma de los totales de los pedidos entregados.
     * - `personalActivo`: cantidad de usuarios con `activo = true`.
     * - `mesasOcupadas`: cantidad de mesas con estado OCUPADA.
     */
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

```kotlin
/**
 * Estado de la pantalla de gestión de mesas.
 *
 * @property mesas Lista de mesas obtenidas en tiempo real desde el repositorio.
 * @property showNuevaMesaDialog Indica si el diálogo para crear una nueva mesa está visible.
 */
data class EstadoMesasState(
    val mesas: List<Mesa> = emptyList(),
    val showNuevaMesaDialog: Boolean = false
)

/**
 * ViewModel que maneja el estado y las operaciones CRUD de la pantalla de mesas.
 *
 * Observa la lista de mesas en tiempo real mediante [MesaRepository] y expone acciones
 * para mostrar/ocultar el diálogo de creación, así como para agregar, actualizar y
 * eliminar mesas.
 */
class EstadoMesasViewModel : ViewModel() {
    private val repo = AppModule.mesaRepository

    private val _state = MutableStateFlow(EstadoMesasState())
    val state = _state.asStateFlow()

    /**
     * Al inicializarse, se suscribe al flujo de mesas del repositorio y actualiza
     * el estado cada vez que la lista cambia.
     */
    init {
        viewModelScope.launch {
            repo.getTodasLasMesas().collect { mesas ->
                _state.value = _state.value.copy(mesas = mesas)
            }
        }
    }

    /**
     * Muestra el diálogo para crear una nueva mesa.
     */
    fun showNuevaMesaDialog() {
        _state.value = _state.value.copy(showNuevaMesaDialog = true)
    }

    /**
     * Oculta el diálogo de creación de nueva mesa.
     */
    fun hideNuevaMesaDialog() {
        _state.value = _state.value.copy(showNuevaMesaDialog = false)
    }

    /**
     * Agrega una nueva mesa a través del repositorio y cierra el diálogo de creación
     * al finalizar la operación.
     *
     * @param mesa Mesa a registrar.
     */
    fun addMesa(mesa: Mesa) {
        viewModelScope.launch {
            repo.addMesa(mesa)
            hideNuevaMesaDialog()
        }
    }

    /**
     * Actualiza los datos de una mesa existente.
     *
     * @param mesa Mesa con los datos actualizados.
     */
    fun updateMesa(mesa: Mesa) {
        viewModelScope.launch { repo.updateMesa(mesa) }
    }

    /**
     * Elimina una mesa del sistema.
     *
     * @param id Identificador de la mesa a eliminar.
     */
    fun deleteMesa(id: Int) {
        viewModelScope.launch { repo.deleteMesa(id) }
    }
}
```

### `presentation/viewmodel/NuevoPedidoViewModel.kt`

```kotlin
/**
 * Estado del flujo de creación de un nuevo pedido.
 *
 * @property mesas Lista de mesas disponibles obtenidas en tiempo real.
 * @property searchMesa Texto de búsqueda usado para filtrar mesas.
 * @property filterMesa Filtro aplicado sobre las mesas (por ejemplo "Todas", libres, ocupadas).
 * @property mesaSeleccionada Id de la mesa seleccionada para armar el pedido, o `null` si aún
 * no se ha elegido ninguna.
 * @property platillos Lista de platillos del menú disponibles para agregar al pedido.
 * @property searchPlatillo Texto de búsqueda usado para filtrar platillos.
 * @property filterCategoria Categoría seleccionada para filtrar los platillos (por defecto "Todos").
 * @property seleccionados Mapa de id de platillo a cantidad seleccionada dentro del pedido en curso.
 * @property mostrandoResumen Indica si la pantalla está mostrando el resumen del pedido antes de enviarlo.
 * @property pedidoEnviado Bandera que indica que el pedido fue enviado exitosamente.
 */
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

/**
 * ViewModel que orquesta todo el flujo de creación de un pedido: selección de mesa,
 * selección de platillos del menú (con búsqueda y filtro por categoría), armado del
 * resumen y envío final del pedido.
 */
class NuevoPedidoViewModel : ViewModel() {
    private val mesaRepo = AppModule.mesaRepository
    private val menuRepo = AppModule.menuRepository
    private val pedidoRepo = AppModule.pedidoRepository

    private val _state = MutableStateFlow(NuevoPedidoState())
    val state = _state.asStateFlow()

    /**
     * Al inicializarse, se suscribe al flujo de mesas para mantener la lista actualizada
     * en tiempo real.
     */
    init {
        viewModelScope.launch {
            mesaRepo.getTodasLasMesas().collect { mesas ->
                _state.update { it.copy(mesas = mesas) }
            }
        }
    }

    /**
     * Actualiza el texto de búsqueda usado para filtrar mesas.
     *
     * @param q Nuevo texto de búsqueda.
     */
    fun onSearchMesaChanged(q: String) { _state.update { it.copy(searchMesa = q) } }

    /**
     * Actualiza el filtro aplicado sobre la lista de mesas.
     *
     * @param f Nuevo valor del filtro.
     */
    fun onFilterMesaChanged(f: String) { _state.update { it.copy(filterMesa = f) } }

    /**
     * Selecciona la mesa para la cual se armará el pedido y carga el menú de platillos
     * disponibles desde [MenuRepository].
     *
     * @param id Identificador de la mesa seleccionada.
     */
    fun seleccionarMesa(id: Int) {
        _state.update { it.copy(mesaSeleccionada = id) }
        // Cargar platillos cuando se selecciona mesa
        viewModelScope.launch {
            menuRepo.getMenu().collect { lista ->
                _state.update { it.copy(platillos = lista) }
            }
        }
    }

    /**
     * Regresa al paso de selección de mesa, deseleccionando la mesa actual y
     * ocultando el resumen del pedido si estaba visible.
     */
    fun volverAMesas() { _state.update { it.copy(mesaSeleccionada = null, mostrandoResumen = false) } }

    /**
     * Actualiza el texto de búsqueda usado para filtrar platillos del menú.
     *
     * @param q Nuevo texto de búsqueda.
     */
    fun onSearchPlatilloChanged(q: String) { _state.update { it.copy(searchPlatillo = q) } }

    /**
     * Actualiza la categoría seleccionada para filtrar los platillos del menú.
     *
     * @param cat Nueva categoría seleccionada.
     */
    fun onCategoriaChanged(cat: String) { _state.update { it.copy(filterCategoria = cat) } }

    /**
     * Alterna la selección de un platillo dentro del pedido en curso: si ya estaba
     * seleccionado lo quita, si no lo estaba lo agrega con cantidad inicial 1.
     *
     * @param platilloId Identificador del platillo a alternar.
     */
    fun togglePlatillo(platilloId: String) {
        _state.update { current ->
            val map = current.seleccionados.toMutableMap()
            if (map.containsKey(platilloId)) map.remove(platilloId) else map[platilloId] = 1
            current.copy(seleccionados = map)
        }
    }

    /**
     * Establece la cantidad de un platillo seleccionado dentro del pedido.
     *
     * Si la cantidad indicada es menor o igual a 0, el platillo se elimina de la
     * selección; en caso contrario se actualiza (o agrega) su cantidad.
     *
     * @param platilloId Identificador del platillo.
     * @param cantidad Nueva cantidad deseada para ese platillo.
     */
    fun setCantidad(platilloId: String, cantidad: Int) {
        if (cantidad <= 0) {
            _state.update { it.copy(seleccionados = it.seleccionados - platilloId) }
        } else {
            _state.update { it.copy(seleccionados = it.seleccionados + (platilloId to cantidad)) }
        }
    }

    /**
     * Avanza a la pantalla de resumen del pedido, mostrando los platillos seleccionados
     * antes de confirmarlo.
     */
    fun irAResumen() { _state.update { it.copy(mostrandoResumen = true) } }

    /**
     * Regresa desde el resumen a la selección de platillos, permitiendo seguir editando
     * el pedido.
     */
    fun volverAPlatillos() { _state.update { it.copy(mostrandoResumen = false) } }

    /**
     * Construye y envía el pedido final al repositorio.
     *
     * Crea un nuevo [Pedido] en estado [EstadoPedido.EN_PREPARACION], asociado al usuario
     * actualmente autenticado (obtenido de [SessionManager]), y lo persiste mediante
     * [PedidoRepository.addPedido]. Al finalizar, marca el estado como enviado
     * ([NuevoPedidoState.pedidoEnviado]).
     *
     * @param mesaId Identificador de la mesa asociada al pedido.
     * @param platillosConNotas Lista de platillos seleccionados junto con sus notas y cantidades.
     * @param itemsFirebase Representación de los ítems del pedido en el formato requerido
     * por Firebase (lista de mapas clave-valor).
     * @param total Monto total del pedido.
     * @param descripcion Descripción/resumen textual del pedido.
     */
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

```kotlin
/**
 * Estado de la pantalla de administración del menú.
 *
 * @property platillos Lista de platillos obtenidos en tiempo real desde el repositorio.
 * @property selectedCategory Categoría actualmente seleccionada para filtrar (por defecto "Todos").
 * @property searchQuery Texto de búsqueda usado para filtrar platillos por nombre u otro criterio.
 */
data class MenuAdminState(
    val platillos: List<Platillo> = emptyList(),
    val selectedCategory: String = "Todos",
    val searchQuery: String = ""
)

/**
 * ViewModel que administra el CRUD del menú de platillos.
 *
 * Observa la lista de platillos en tiempo real mediante [MenuRepository] y expone
 * acciones para filtrar por categoría/búsqueda, así como para agregar, actualizar
 * y eliminar platillos.
 */
class MenuAdminViewModel : ViewModel() {
    private val repo = AppModule.menuRepository

    private val _state = MutableStateFlow(MenuAdminState())
    val state = _state.asStateFlow()

    /**
     * Al inicializarse, se suscribe al flujo del menú y actualiza el estado cada vez
     * que la lista de platillos cambia.
     */
    init {
        viewModelScope.launch {
            repo.getMenu().collect { lista ->
                _state.value = _state.value.copy(platillos = lista)
            }
        }
    }

    /**
     * Actualiza la categoría seleccionada para filtrar el menú.
     *
     * @param cat Nueva categoría seleccionada.
     */
    fun onCategorySelected(cat: String) { _state.value = _state.value.copy(selectedCategory = cat) }

    /**
     * Actualiza el texto de búsqueda usado para filtrar platillos.
     *
     * @param q Nuevo texto de búsqueda.
     */
    fun onSearchQueryChanged(q: String) { _state.value = _state.value.copy(searchQuery = q) }

    /**
     * Agrega un nuevo platillo al menú a través del repositorio.
     *
     * @param platillo Platillo a registrar.
     */
    fun addPlatillo(platillo: Platillo) {
        viewModelScope.launch { repo.addPlatillo(platillo) }
    }

    /**
     * Actualiza los datos de un platillo existente en el menú.
     *
     * @param platillo Platillo con los datos actualizados.
     */
    fun updatePlatillo(platillo: Platillo) {
        viewModelScope.launch { repo.updatePlatillo(platillo) }
    }

    /**
     * Elimina un platillo del menú.
     *
     * @param id Identificador del platillo a eliminar.
     */
    fun deletePlatillo(id: String) {
        viewModelScope.launch { repo.deletePlatillo(id) }
    }
}
```

### `presentation/viewmodel/UsuariosViewModel.kt`

```kotlin
/**
 * Estado de la pantalla de administración de personal (usuarios).
 *
 * @property usuarios Lista de usuarios obtenidos en tiempo real desde el repositorio.
 * @property filter Filtro actualmente aplicado sobre la lista de usuarios (por ejemplo por rol o estado).
 * @property isLoading Indica si la carga inicial de usuarios está en curso.
 * @property showNuevoDialog Indica si el diálogo para crear un nuevo usuario está visible.
 * @property usuarioAEditar Usuario actualmente seleccionado para edición, o `null` si no hay
 * ninguno en edición.
 */
data class UsuariosState(
    val usuarios: List<Usuario> = emptyList(),
    val filter: String = "Todos",
    val isLoading: Boolean = false,
    val showNuevoDialog: Boolean = false,
    val usuarioAEditar: Usuario? = null
)

/**
 * ViewModel que administra el CRUD de usuarios (personal) del restaurante.
 *
 * Observa la lista de usuarios en tiempo real mediante [UsuarioRepository] y expone
 * acciones para filtrar, mostrar/ocultar el diálogo de creación, editar (o cancelar
 * la edición) y realizar las operaciones de agregar, actualizar y eliminar usuarios.
 */
class UsuariosViewModel : ViewModel() {
    private val repo = AppModule.usuarioRepository

    private val _state = MutableStateFlow(UsuariosState())
    val state = _state.asStateFlow()

    /**
     * Al inicializarse, dispara la carga de usuarios mediante [loadUsuarios].
     */
    init {
        loadUsuarios()
    }

    /**
     * Marca el estado como cargando y se suscribe al flujo de usuarios del repositorio,
     * actualizando la lista y desactivando el indicador de carga en cada emisión.
     */
    private fun loadUsuarios() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repo.getUsuarios().collect { lista ->
                _state.value = _state.value.copy(usuarios = lista, isLoading = false)
            }
        }
    }

    /**
     * Actualiza el filtro aplicado sobre la lista de usuarios.
     *
     * @param filter Nuevo valor del filtro.
     */
    fun setFilter(filter: String) {
        _state.value = _state.value.copy(filter = filter)
    }

    /**
     * Muestra el diálogo para crear un nuevo usuario.
     */
    fun showNuevoUsuarioDialog() {
        _state.value = _state.value.copy(showNuevoDialog = true)
    }

    /**
     * Oculta el diálogo de creación de nuevo usuario.
     */
    fun hideNuevoUsuarioDialog() {
        _state.value = _state.value.copy(showNuevoDialog = false)
    }

    /**
     * Establece el usuario que se va a editar, mostrando el formulario de edición
     * correspondiente en la UI.
     *
     * @param usuario Usuario seleccionado para edición.
     */
    fun editUsuario(usuario: Usuario) {
        _state.value = _state.value.copy(usuarioAEditar = usuario)
    }

    /**
     * Cancela la edición en curso, limpiando el usuario seleccionado para editar.
     */
    fun cancelEditUsuario() {
        _state.value = _state.value.copy(usuarioAEditar = null)
    }

    /**
     * Agrega un nuevo usuario a través del repositorio y cierra el diálogo de creación
     * al finalizar.
     *
     * @param usuario Usuario a registrar.
     */
    fun addUsuario(usuario: Usuario) {
        viewModelScope.launch {
            repo.addUsuario(usuario)
            hideNuevoUsuarioDialog()
        }
    }

    /**
     * Actualiza los datos de un usuario existente y cancela el modo edición al finalizar.
     *
     * @param usuario Usuario con los datos actualizados.
     */
    fun updateUsuario(usuario: Usuario) {
        viewModelScope.launch {
            repo.updateUsuario(usuario)
            cancelEditUsuario()
        }
    }

    /**
     * Elimina un usuario del sistema.
     *
     * @param id Identificador del usuario a eliminar.
     */
    fun deleteUsuario(id: String) {
        viewModelScope.launch {
            repo.deleteUsuario(id)
        }
    }
}
```

### `presentation/viewmodel/ZonasAdminViewModel.kt`

```kotlin
/**
 * Estado de la pantalla de administración de zonas del restaurante.
 *
 * @property zonas Lista de zonas obtenidas en tiempo real desde el repositorio.
 * @property usuarios Lista de usuarios disponibles, usada para asociar meseros a una zona.
 * @property showNuevaZonaDialog Indica si el diálogo para crear una nueva zona está visible.
 * @property zonaAEditar Zona actualmente seleccionada para edición, o `null` si no hay ninguna.
 */
data class ZonasState(
    val zonas: List<Zona> = emptyList(),
    val usuarios: List<Usuario> = emptyList(),
    val showNuevaZonaDialog: Boolean = false,
    val zonaAEditar: Zona? = null
)

/**
 * ViewModel que administra el CRUD de zonas del restaurante.
 *
 * Observa en paralelo los flujos de zonas ([ZonaRepository]) y usuarios ([UsuarioRepository])
 * para mantener actualizado el estado, y expone acciones para mostrar/ocultar el diálogo
 * de creación, editar (o cancelar edición) y realizar las operaciones de agregar,
 * actualizar y eliminar zonas.
 */
class ZonasAdminViewModel : ViewModel() {
    private val zonaRepo = AppModule.zonaRepository
    private val usuarioRepo = AppModule.usuarioRepository

    private val _state = MutableStateFlow(ZonasState())
    val state = _state.asStateFlow()

    /**
     * Al inicializarse, se suscribe de forma independiente al flujo de zonas y al flujo
     * de usuarios, actualizando cada parte del estado según corresponda.
     */
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

    /**
     * Muestra el diálogo para crear una nueva zona.
     */
    fun showNuevaZonaDialog() {
        _state.value = _state.value.copy(showNuevaZonaDialog = true)
    }

    /**
     * Oculta el diálogo de creación de nueva zona.
     */
    fun hideNuevaZonaDialog() {
        _state.value = _state.value.copy(showNuevaZonaDialog = false)
    }

    /**
     * Agrega una nueva zona a través del repositorio y cierra el diálogo de creación
     * al finalizar.
     *
     * @param zona Zona a registrar.
     */
    fun addZona(zona: Zona) {
        viewModelScope.launch {
            zonaRepo.addZona(zona)
            hideNuevaZonaDialog()
        }
    }

    /**
     * Establece la zona que se va a editar.
     *
     * @param zona Zona seleccionada para edición.
     */
    fun editZona(zona: Zona) {
        _state.value = _state.value.copy(zonaAEditar = zona)
    }

    /**
     * Cancela la edición en curso, limpiando la zona seleccionada para editar.
     */
    fun cancelEditZona() {
        _state.value = _state.value.copy(zonaAEditar = null)
    }

    /**
     * Actualiza los datos de una zona existente y cancela el modo edición al finalizar.
     *
     * @param zona Zona con los datos actualizados.
     */
    fun updateZona(zona: Zona) {
        viewModelScope.launch {
            zonaRepo.updateZona(zona)
            cancelEditZona()
        }
    }

    /**
     * Elimina una zona del sistema y cancela el modo edición al finalizar.
     *
     * @param id Identificador de la zona a eliminar.
     */
    fun deleteZona(id: String) {
        viewModelScope.launch {
            zonaRepo.deleteZona(id)
            cancelEditZona()
        }
    }
}
```

### `presentation/viewmodel/HistorialPedidosViewModel.kt`

```kotlin
/**
 * Estado de la pantalla de historial de pedidos.
 *
 * @property pedidos Lista completa de pedidos obtenidos en tiempo real desde el repositorio.
 * @property searchQuery Texto de búsqueda ingresado por el usuario.
 * @property filteredPedidos Lista de pedidos resultante tras aplicar [searchQuery], en orden
 * inverso (más recientes primero).
 */
data class HistorialPedidosState(
    val pedidos: List<Pedido> = emptyList(),
    val searchQuery: String = "",
    val filteredPedidos: List<Pedido> = emptyList()
)

/**
 * ViewModel que administra el estado de la pantalla de historial de pedidos.
 *
 * Combina el flujo de pedidos del repositorio con el texto de búsqueda actual del
 * estado para producir, de forma reactiva, la lista filtrada que se muestra en pantalla.
 */
class HistorialPedidosViewModel : ViewModel() {
    private val repo = AppModule.pedidoRepository

    private val _state = MutableStateFlow(HistorialPedidosState())
    val state = _state.asStateFlow()

    /**
     * Al inicializarse, combina el flujo de pedidos con el flujo derivado del texto de
     * búsqueda ([HistorialPedidosState.searchQuery]).
     *
     * La lista de pedidos se invierte (más recientes primero) y, si hay texto de búsqueda,
     * se filtra por número de mesa o por coincidencia (sin distinguir mayúsculas/minúsculas)
     * en la descripción del pedido. El resultado se guarda en
     * [HistorialPedidosState.filteredPedidos].
     */
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

    /**
     * Actualiza el texto de búsqueda del historial, lo que recalcula automáticamente
     * la lista filtrada gracias a la combinación de flujos definida en [init].
     *
     * @param query Nuevo texto de búsqueda.
     */
    fun onSearchChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }
}
```

### `presentation/viewmodel/AlertasViewModel.kt`

```kotlin
/**
 * Estado de la pantalla de alertas de pedidos.
 *
 * @property pedidos Lista de pedidos obtenidos en tiempo real desde el repositorio.
 * @property filtro Filtro por estado actualmente aplicado (por defecto "Todos").
 * @property pedidoDetalle Pedido seleccionado para mostrar en el diálogo de detalle,
 * o `null` si no hay ninguno seleccionado.
 */
data class AlertasState(
    val pedidos: List<Pedido> = emptyList(),
    val filtro: String = "Todos",
    val pedidoDetalle: Pedido? = null
)

/**
 * ViewModel que administra el estado de la pantalla de alertas.
 *
 * Observa la lista de pedidos en tiempo real mediante [PedidoRepository] y expone
 * acciones para filtrar por estado y para mostrar/cerrar el detalle de un pedido
 * seleccionado.
 */
class AlertasViewModel : ViewModel() {
    private val repo = AppModule.pedidoRepository

    private val _state = MutableStateFlow(AlertasState())
    val state = _state.asStateFlow()

    /**
     * Al inicializarse, se suscribe al flujo de pedidos del repositorio y actualiza
     * la lista en el estado cada vez que cambia.
     */
    init {
        viewModelScope.launch {
            repo.getPedidos().collect { lista ->
                _state.update { it.copy(pedidos = lista) }
            }
        }
    }

    /**
     * Actualiza el filtro por estado aplicado sobre la lista de pedidos.
     *
     * @param filtro Nuevo valor del filtro (por ejemplo "Todos", "Listos", "En cocina", "Cancelados").
     */
    fun setFiltro(filtro: String) { _state.update { it.copy(filtro = filtro) } }

    /**
     * Selecciona un pedido para mostrar su detalle en el diálogo correspondiente.
     *
     * @param pedido Pedido cuyo detalle se desea visualizar.
     */
    fun verDetalle(pedido: Pedido) { _state.update { it.copy(pedidoDetalle = pedido) } }

    /**
     * Cierra el diálogo de detalle, limpiando el pedido seleccionado.
     */
    fun cerrarDetalle() { _state.update { it.copy(pedidoDetalle = null) } }
}
```

---

## Domain / Model

### `domain/model/Enums.kt`

```kotlin
/**
 * Representa los posibles estados en el ciclo de vida de un [Pedido], desde que se crea
 * hasta que se entrega o se cancela.
 */
enum class EstadoPedido {
    /** El pedido fue creado pero aún no se ha comenzado a preparar. */
    PENDIENTE,
    /** El pedido está siendo preparado en cocina. */
    EN_PREPARACION,
    /** El pedido está listo para ser entregado al cliente. */
    LISTO,
    /** El pedido ya fue entregado al cliente. */
    ENTREGADO,
    /** El pedido fue cancelado y no se completará. */
    CANCELADO
}

/**
 * Representa el estado de disponibilidad de una [Mesa] del restaurante.
 */
enum class EstadoMesa {
    /** La mesa está disponible para asignar clientes. */
    LIBRE,
    /** La mesa está actualmente en uso (tiene un pedido activo). */
    OCUPADA,
    /** La mesa está reservada para un uso futuro. */
    RESERVADA,
    /** La mesa no está disponible para su uso (por ejemplo, en mantenimiento). */
    FUERA_DE_SERVICIO
}

/**
 * Representa el estado de disponibilidad de una [Zona] del restaurante.
 */
enum class EstadoZona {
    /** La zona está disponible para operar con normalidad. */
    DISPONIBLE,
    /** La zona no está disponible temporalmente. */
    NO_DISPONIBLE
}

/**
 * Representa los roles posibles que puede tener un [Usuario] dentro del sistema,
 * y que determinan sus permisos y las pantallas a las que puede acceder.
 */
enum class RolUsuario {
    /** Mesero encargado de tomar y entregar pedidos. */
    MESERO,
    /** Chef encargado de la preparación de los pedidos en cocina. */
    CHEF,
    /** Cajero encargado de los cobros. */
    CAJERO,
    /** Administrador con acceso completo al sistema. */
    ADMIN
}

/**
 * Representa el estado laboral actual de un [Usuario].
 */
enum class EstadoUsuario {
    /** El usuario está activo y disponible para trabajar. */
    ACTIVO,
    /** El usuario está inactivo (por ejemplo, dado de baja temporalmente). */
    INACTIVO,
    /** El usuario está en su periodo de descanso. */
    EN_DESCANSO
}
```

### `domain/model/Usuario.kt`

```kotlin
/**
 * Entidad de dominio que representa a un usuario del sistema (mesero, chef, cajero o
 * administrador).
 *
 * @property id Identificador único del usuario (clave en Firebase).
 * @property nombre Nombre del usuario, también utilizado como credencial de login.
 * @property rol Rol asignado al usuario dentro del sistema ([RolUsuario]).
 * @property activo Indica si el usuario se encuentra actualmente activo en el sistema.
 * @property estadoUsuario Estado laboral actual del usuario ([EstadoUsuario]).
 * @property zonaId Identificador de la zona asignada al usuario, si aplica.
 * @property zonaAsignada Nombre descriptivo de la zona asignada al usuario.
 * @property fotoEmoji Emoji utilizado como avatar/foto de perfil del usuario.
 */
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

```kotlin
/**
 * Entidad de dominio que representa una mesa física del restaurante.
 *
 * @property id Identificador único de la mesa.
 * @property numero Número visible de la mesa dentro del restaurante.
 * @property estado Estado actual de disponibilidad de la mesa ([EstadoMesa]).
 * @property capacidad Cantidad máxima de comensales que puede recibir la mesa.
 * @property meseroAsignado Identificador o nombre del mesero asignado a la mesa, si aplica.
 * @property zonaId Identificador de la zona del restaurante a la que pertenece la mesa.
 */
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

```kotlin
/**
 * Entidad de dominio que representa una zona del restaurante (por ejemplo, sección A, B o C),
 * agrupando un conjunto de mesas y, opcionalmente, un mesero responsable.
 *
 * @property id Identificador único de la zona.
 * @property nombreZona Nombre descriptivo de la zona.
 * @property estadoZona Estado de disponibilidad de la zona ([EstadoZona]).
 */
data class Zona(
    val id: String = "",
    val nombreZona: String = "",
    val estadoZona: EstadoZona = EstadoZona.DISPONIBLE
)
```

### `domain/model/Platillo.kt`

```kotlin
/**
 * Entidad de dominio que representa un platillo disponible en el menú del restaurante.
 *
 * @property id Identificador único del platillo.
 * @property nombre Nombre del platillo.
 * @property precio Precio unitario del platillo.
 * @property categoria Categoría a la que pertenece el platillo (por ejemplo, entradas, platos fuertes, bebidas).
 * @property disponible Indica si el platillo está disponible actualmente para ordenarse.
 * @property ingredientes Lista de ingredientes que componen el platillo.
 * @property emoji Emoji utilizado como representación visual del platillo.
 */
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

```kotlin
/**
 * Entidad de dominio que representa una comanda (pedido) realizada en una mesa.
 *
 * @property id Identificador único del pedido.
 * @property mesa Número de la mesa asociada al pedido.
 * @property mesaId Identificador interno de la mesa asociada al pedido.
 * @property meseroId Identificador del mesero que atendió el pedido.
 * @property descripcion Descripción o resumen textual del contenido del pedido.
 * @property nota Nota adicional asociada al pedido completo (por ejemplo, indicaciones especiales).
 * @property estado Estado actual del pedido dentro de su ciclo de vida ([EstadoPedido]).
 * @property total Monto total a cobrar por el pedido.
 * @property timestamp Marca de tiempo (en milisegundos) en que se registró el pedido.
 * @property platillos Lista de platillos seleccionados que componen el pedido ([PlatilloSeleccionado]).
 * @property usuarioId Identificador del usuario (mesero) que generó el pedido.
 */
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

/**
 * Representa un platillo específico dentro de un [Pedido], junto con la cantidad
 * solicitada y una nota particular para su preparación.
 *
 * @property id Identificador del platillo original en el menú.
 * @property nombre Nombre del platillo en el momento de realizar el pedido.
 * @property precio Precio unitario del platillo en el momento de realizar el pedido.
 * @property cantidad Cantidad solicitada de este platillo dentro del pedido.
 * @property nota Nota o indicación especial para la preparación de este platillo
 * (por ejemplo, "sin cebolla").
 */
data class PlatilloSeleccionado(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1,
    val nota: String = ""
)
```

### `domain/model/Notificacion.kt`

```kotlin
/**
 * Entidad de dominio que representa una notificación generada a partir de un [Pedido],
 * utilizada para alertar a un usuario (por ejemplo, un mesero) sobre un cambio de estado.
 *
 * @property id Identificador único de la notificación.
 * @property pedidoId Identificador del pedido al que hace referencia la notificación.
 * @property usuarioId Identificador del usuario destinatario de la notificación.
 * @property mensaje Texto descriptivo de la notificación.
 * @property confirmada Indica si el usuario ya confirmó o revisó la notificación.
 */
data class Notificacion(
    val id: String = "",
    val pedidoId: String = "",
    val usuarioId: String = "",
    val mensaje: String = "",
    val confirmada: Boolean = false
)
```

### `domain/model/Turno.kt`

```kotlin
/**
 * Entidad de dominio que representa el turno de trabajo de un [Usuario].
 *
 * @property id Identificador único del turno.
 * @property horaInicio Marca de tiempo (en milisegundos) en que inició el turno.
 * @property horaFin Marca de tiempo (en milisegundos) en que finalizó el turno, o `null`
 * si el turno sigue en curso.
 * @property usuarioId Identificador del usuario al que pertenece el turno.
 */
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

```kotlin
/**
 * Contrato de la capa de dominio para las operaciones de autenticación del sistema.
 *
 * Define las operaciones de login (como administrador o como usuario regular) y de
 * registro de nuevos usuarios, sin exponer detalles de la fuente de datos subyacente
 * (por ejemplo, Firebase).
 */
interface AuthRepository {

    /**
     * Autentica al usuario administrador predeterminado del sistema.
     *
     * @return [Result] exitoso con el [Usuario] administrador, o fallido si no fue
     * posible autenticar.
     */
    suspend fun loginAsAdmin(): Result<Usuario>

    /**
     * Autentica a un usuario regular a partir de su nombre de usuario.
     *
     * @param nombreUsuario Nombre de usuario con el que se intenta iniciar sesión.
     * @return [Result] exitoso con el [Usuario] encontrado, o fallido si no existe
     * o no se pudo autenticar.
     */
    suspend fun login(nombreUsuario: String): Result<Usuario>

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param usuario Datos del usuario a registrar.
     * @return [Result] exitoso si el registro se completó correctamente, o fallido
     * en caso de error.
     */
    suspend fun register(usuario: Usuario): Result<Unit>
}
```

### `domain/repository/UsuarioRepository.kt`

```kotlin
/**
 * Contrato de la capa de dominio para la gestión de usuarios del sistema.
 *
 * Define las operaciones de lectura en tiempo real y las operaciones CRUD sobre
 * la entidad [Usuario], sin exponer detalles de la fuente de datos subyacente.
 */
interface UsuarioRepository {

    /**
     * Observa la lista completa de usuarios en tiempo real.
     *
     * @return [Flow] que emite la lista actualizada de [Usuario] cada vez que cambia
     * en la fuente de datos.
     */
    fun getUsuarios(): Flow<List<Usuario>>

    /**
     * Agrega un nuevo usuario al sistema.
     *
     * @param usuario Usuario a registrar.
     */
    suspend fun addUsuario(usuario: Usuario)

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param usuario Usuario con los datos actualizados.
     */
    suspend fun updateUsuario(usuario: Usuario)

    /**
     * Elimina un usuario del sistema.
     *
     * @param id Identificador del usuario a eliminar.
     */
    suspend fun deleteUsuario(id: String)
}
```

### `domain/repository/MesaRepository.kt`

```kotlin
/**
 * Contrato de la capa de dominio para la gestión de mesas del restaurante.
 *
 * Define las operaciones de lectura en tiempo real y las operaciones CRUD sobre
 * la entidad [Mesa].
 */
interface MesaRepository {

    /**
     * Observa el estado de todas las mesas del restaurante en tiempo real, incluyendo
     * su disponibilidad (libre/ocupada).
     *
     * @return [Flow] que emite la lista actualizada de [Mesa] cada vez que cambia
     * en la fuente de datos.
     */
    fun getTodasLasMesas(): Flow<List<Mesa>>

    /**
     * Agrega una nueva mesa al sistema.
     *
     * @param mesa Mesa a registrar.
     */
    suspend fun addMesa(mesa: Mesa)

    /**
     * Actualiza los datos de configuración de una mesa existente.
     *
     * @param mesa Mesa con los datos actualizados.
     */
    suspend fun updateMesa(mesa: Mesa)

    /**
     * Elimina una mesa del sistema.
     *
     * @param id Identificador de la mesa a eliminar.
     */
    suspend fun deleteMesa(id: Int)
}
```

### `domain/repository/ZonaRepository.kt`

```kotlin
/**
 * Contrato de la capa de dominio para la gestión de zonas del restaurante.
 *
 * Define las operaciones de lectura en tiempo real y las operaciones CRUD sobre
 * la entidad [Zona].
 */
interface ZonaRepository {

    /**
     * Observa la lista completa de zonas en tiempo real.
     *
     * @return [Flow] que emite la lista actualizada de [Zona] cada vez que cambia
     * en la fuente de datos.
     */
    fun getZonas(): Flow<List<Zona>>

    /**
     * Agrega una nueva zona al sistema.
     *
     * @param zona Zona a registrar.
     */
    suspend fun addZona(zona: Zona)

    /**
     * Actualiza los datos de una zona existente.
     *
     * @param zona Zona con los datos actualizados.
     */
    suspend fun updateZona(zona: Zona)

    /**
     * Elimina una zona del sistema.
     *
     * @param id Identificador de la zona a eliminar.
     */
    suspend fun deleteZona(id: String)
}
```

### `domain/repository/MenuRepository.kt`

```kotlin
/**
 * Contrato de la capa de dominio para la gestión del menú de platillos.
 *
 * Define las operaciones de lectura en tiempo real y las operaciones CRUD sobre
 * la entidad [Platillo].
 */
interface MenuRepository {

    /**
     * Observa la lista completa de platillos del menú en tiempo real.
     *
     * @return [Flow] que emite la lista actualizada de [Platillo] cada vez que cambia
     * en la fuente de datos.
     */
    fun getMenu(): Flow<List<Platillo>>

    /**
     * Agrega un nuevo platillo al menú.
     *
     * @param platillo Platillo a registrar.
     */
    suspend fun addPlatillo(platillo: Platillo)

    /**
     * Actualiza los datos de un platillo existente en el menú.
     *
     * @param platillo Platillo con los datos actualizados.
     */
    suspend fun updatePlatillo(platillo: Platillo)

    /**
     * Elimina un platillo del menú.
     *
     * @param id Identificador del platillo a eliminar.
     */
    suspend fun deletePlatillo(id: String)
}
```

### `domain/repository/PedidoRepository.kt`

```kotlin
/**
 * Contrato de la capa de dominio para la gestión de pedidos (comandas).
 *
 * Define las operaciones de lectura en tiempo real y las operaciones sobre
 * la entidad [Pedido], incluyendo su registro con los ítems asociados, la
 * actualización de su estado y su eliminación.
 */
interface PedidoRepository {

    /**
     * Observa la lista completa de pedidos en tiempo real.
     *
     * @return [Flow] que emite la lista actualizada de [Pedido] cada vez que cambia
     * en la fuente de datos.
     */
    fun getPedidos(): Flow<List<Pedido>>

    /**
     * Registra un nuevo pedido junto con el detalle de sus ítems.
     *
     * @param pedido Datos generales del pedido a registrar.
     * @param items Lista de ítems del pedido representados como mapas clave-valor,
     * en el formato requerido por la fuente de datos.
     */
    suspend fun addPedido(pedido: Pedido, items: List<Map<String, String>>)

    /**
     * Actualiza el estado de un pedido existente.
     *
     * @param pedidoId Identificador del pedido a actualizar.
     * @param nuevoEstado Nuevo estado a asignar ([EstadoPedido]).
     */
    suspend fun updateEstado(pedidoId: String, nuevoEstado: EstadoPedido)

    /**
     * Elimina un pedido del sistema.
     *
     * @param pedidoId Identificador del pedido a eliminar.
     */
    suspend fun deletePedido(pedidoId: String)
}
```

---

## Data / Source

### `data/source/AuthDataSource.kt`

```kotlin
/**
 * Fuente de datos encargada de las operaciones de autenticación contra Firebase
 * Realtime Database, sobre el nodo `usuarios`.
 */
class AuthDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("usuarios")

    /**
     * Devuelve un [Usuario] administrador fijo, sin necesidad de consultar la base de datos.
     *
     * @return Instancia de [Usuario] con rol [RolUsuario.ADMIN].
     */
    suspend fun loginAsAdmin(): Usuario? {
        return Usuario(id = "admin", nombre = "Administrador", rol = RolUsuario.ADMIN)
    }

    /**
     * Busca un usuario por su nombre en el nodo `usuarios` de Firebase.
     *
     * @param nombreUsuario Nombre de usuario a buscar (se recorta espacios en blanco).
     * @return El [Usuario] encontrado y mapeado desde Firebase, o `null` si no existe
     * ningún usuario con ese nombre.
     */
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

    /**
     * Registra un nuevo usuario en Firebase, generando una clave única mediante `push()`.
     *
     * @param usuario Datos del usuario a registrar (el `id` final se asigna automáticamente).
     */
    suspend fun register(usuario: Usuario) {
        val key = database.push().key ?: ""
        database.child(key).setValue(mapFromUsuario(usuario.copy(id = key))).await()
    }

    /**
     * Convierte un [DataSnapshot] de Firebase en una instancia de [Usuario], aplicando
     * valores por defecto seguros ante campos ausentes o con formato inválido
     * (por ejemplo, un rol o estado que no coincide con los valores del enum).
     *
     * @param snapshot Nodo de Firebase correspondiente a un usuario.
     * @return [Usuario] mapeado a partir del snapshot.
     */
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

    /**
     * Convierte un [Usuario] en un mapa clave-valor listo para persistirse en Firebase.
     *
     * @param usuario Usuario a serializar.
     * @return Mapa con los campos del usuario en el formato esperado por Firebase.
     */
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

```kotlin
/**
 * Fuente de datos encargada del CRUD de usuarios contra el nodo `usuarios` de
 * Firebase Realtime Database, exponiendo los cambios en tiempo real como [Flow].
 */
class UsuarioDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("usuarios")

    /**
     * Observa en tiempo real la lista completa de usuarios registrados en Firebase.
     *
     * Utiliza `callbackFlow` junto con un [ValueEventListener] para emitir una nueva
     * lista cada vez que cambian los datos en el nodo `usuarios`. Los registros que
     * fallan al mapearse (por ejemplo, por datos corruptos) se omiten silenciosamente.
     * El listener se remueve automáticamente al cerrarse el flujo.
     *
     * @return [Flow] que emite la lista actualizada de [Usuario].
     */
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

    /**
     * Agrega un nuevo usuario a Firebase, generando una clave única mediante `push()`.
     *
     * @param usuario Datos del usuario a registrar (el `id` final se asigna automáticamente).
     */
    suspend fun addUsuario(usuario: Usuario) {
        val key = database.push().key ?: ""
        database.child(key).setValue(mapFromUsuario(usuario.copy(id = key))).await()
    }

    /**
     * Actualiza parcialmente los campos de un usuario existente en Firebase.
     *
     * @param usuario Usuario con los datos actualizados (se usa su `id` para ubicar el nodo).
     */
    suspend fun updateUsuario(usuario: Usuario) {
        database.child(usuario.id).updateChildren(mapFromUsuario(usuario)).await()
    }

    /**
     * Elimina el nodo correspondiente a un usuario en Firebase.
     *
     * @param id Identificador del usuario a eliminar.
     */
    suspend fun deleteUsuario(id: String) {
        database.child(id).removeValue().await()
    }

    /**
     * Convierte un [Usuario] en un mapa clave-valor listo para persistirse en Firebase.
     *
     * @param u Usuario a serializar.
     * @return Mapa con los campos del usuario en el formato esperado por Firebase.
     */
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

```kotlin
/**
 * Fuente de datos encargada de la gestión de mesas contra Firebase Realtime Database.
 *
 * Combina dos nodos: `mesas_config` (configuración personalizada de mesas, como su
 * capacidad) y `pedidos` (usado para inferir qué mesas están ocupadas según los
 * pedidos activos).
 */
class MesaDataSource {
    private val mesasConfigRef = FirebaseDatabase.getInstance().getReference("mesas_config")
    private val pedidosRef = FirebaseDatabase.getInstance().getReference("pedidos")

    /**
     * Observa en tiempo real la configuración personalizada de mesas almacenada en el
     * nodo `mesas_config` (por ejemplo, capacidad de cada mesa). El estado de cada mesa
     * se inicializa como [EstadoMesa.LIBRE] y se recalcula posteriormente combinándolo
     * con [getMesasOcupadas].
     *
     * @return [Flow] que emite la lista de [Mesa] configuradas, sin el estado de
     * ocupación aplicado todavía.
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
     * Observa en tiempo real el nodo `pedidos` para determinar qué números de mesa
     * tienen actualmente un pedido activo (es decir, cuyo estado no es `ENTREGADO`
     * ni `CANCELADO`).
     *
     * @return [Flow] que emite el conjunto de números de mesa actualmente ocupados.
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

    /**
     * Agrega una nueva mesa al nodo `mesas_config` en Firebase, con estado inicial "LIBRE".
     *
     * @param mesa Mesa a registrar (se usa su `id` y `capacidad`).
     */
    suspend fun addMesa(mesa: Mesa) {
        mesasConfigRef.child(mesa.id.toString()).setValue(
            mapOf("id" to mesa.id, "capacidad" to mesa.capacidad, "estado" to "LIBRE")
        ).await()
    }

    /**
     * Actualiza la capacidad de una mesa existente en el nodo `mesas_config`.
     *
     * @param mesa Mesa con la capacidad actualizada (se usa su `id` para ubicar el nodo).
     */
    suspend fun updateMesa(mesa: Mesa) {
        mesasConfigRef.child(mesa.id.toString()).updateChildren(
            mapOf("capacidad" to mesa.capacidad)
        ).await()
    }

    /**
     * Elimina la configuración de una mesa del nodo `mesas_config`.
     *
     * @param id Identificador de la mesa a eliminar.
     */
    suspend fun deleteMesa(id: Int) {
        mesasConfigRef.child(id.toString()).removeValue().await()
    }
}
```

### `data/source/ZonaDataSource.kt`

```kotlin
/**
 * Fuente de datos encargada del CRUD de zonas contra el nodo `zonas` de
 * Firebase Realtime Database, exponiendo los cambios en tiempo real como [Flow].
 */
class ZonaDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("zonas")

    /**
     * Observa en tiempo real la lista completa de zonas registradas en Firebase.
     *
     * Utiliza `callbackFlow` junto con un [ValueEventListener], aplicando un valor
     * por defecto ([EstadoZona.DISPONIBLE]) cuando el estado almacenado no es válido.
     *
     * @return [Flow] que emite la lista actualizada de [Zona].
     */
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

    /**
     * Agrega una nueva zona a Firebase, generando una clave única mediante `push()`.
     *
     * @param zona Datos de la zona a registrar (el `id` final se asigna automáticamente).
     */
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

    /**
     * Actualiza el nombre y el estado de una zona existente en Firebase.
     *
     * @param zona Zona con los datos actualizados (se usa su `id` para ubicar el nodo).
     */
    suspend fun updateZona(zona: Zona) {
        database.child(zona.id).updateChildren(
            mapOf(
                "nombreZona" to zona.nombreZona,
                "estadoZona" to zona.estadoZona.name
            )
        ).await()
    }

    /**
     * Elimina el nodo correspondiente a una zona en Firebase.
     *
     * @param id Identificador de la zona a eliminar.
     */
    suspend fun deleteZona(id: String) {
        database.child(id).removeValue().await()
    }
}
```

### `data/source/MenuDataSource.kt`

```kotlin
/**
 * Fuente de datos encargada del CRUD del menú (platillos) contra el nodo `menu` de
 * Firebase Realtime Database, exponiendo los cambios en tiempo real como [Flow].
 */
class MenuDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("menu")

    /**
     * Observa en tiempo real la lista completa de platillos del menú registrados en
     * Firebase. Los registros que fallan al mapearse se omiten silenciosamente.
     *
     * @return [Flow] que emite la lista actualizada de [Platillo].
     */
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

    /**
     * Agrega un nuevo platillo al menú en Firebase, generando una clave única
     * mediante `push()`.
     *
     * @param platillo Datos del platillo a registrar (el `id` final se asigna automáticamente).
     */
    suspend fun addPlatillo(platillo: Platillo) {
        val key = database.push().key ?: ""
        database.child(key).setValue(mapPlatillo(platillo.copy(id = key))).await()
    }

    /**
     * Actualiza parcialmente los campos de un platillo existente en Firebase.
     *
     * @param platillo Platillo con los datos actualizados (se usa su `id` para ubicar el nodo).
     */
    suspend fun updatePlatillo(platillo: Platillo) {
        database.child(platillo.id).updateChildren(mapPlatillo(platillo)).await()
    }

    /**
     * Elimina el nodo correspondiente a un platillo en Firebase.
     *
     * @param id Identificador del platillo a eliminar.
     */
    suspend fun deletePlatillo(id: String) {
        database.child(id).removeValue().await()
    }

    /**
     * Convierte un [Platillo] en un mapa clave-valor listo para persistirse en Firebase.
     *
     * @param p Platillo a serializar.
     * @return Mapa con los campos del platillo en el formato esperado por Firebase.
     */
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

```kotlin
/**
 * Fuente de datos encargada del CRUD de pedidos contra el nodo `pedidos` de
 * Firebase Realtime Database, incluyendo el registro de sus ítems y la
 * actualización de su estado, exponiendo los cambios en tiempo real como [Flow].
 */
class PedidoDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("pedidos")

    /**
     * Observa en tiempo real la lista completa de pedidos registrados en Firebase,
     * incluyendo el detalle de los ítems (`items`) asociados a cada uno, mapeados
     * a [PlatilloSeleccionado]. Los registros que fallan al mapearse se omiten
     * silenciosamente y el estado se resuelve a [EstadoPedido.PENDIENTE] por defecto
     * si el valor almacenado no es válido.
     *
     * @return [Flow] que emite la lista actualizada de [Pedido].
     */
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

    /**
     * Registra un nuevo pedido en Firebase junto con sus ítems, generando una clave
     * única mediante `push()` y asignando la marca de tiempo del servidor
     * ([ServerValue.TIMESTAMP]) automáticamente.
     *
     * @param pedido Datos generales del pedido a registrar.
     * @param items Lista de ítems del pedido en el formato de mapas clave-valor
     * esperado por Firebase.
     */
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

    /**
     * Actualiza únicamente el campo de estado de un pedido existente en Firebase.
     *
     * @param pedidoId Identificador del pedido a actualizar.
     * @param nuevoEstado Nuevo estado a asignar ([EstadoPedido]).
     */
    suspend fun updateEstado(pedidoId: String, nuevoEstado: EstadoPedido) {
        database.child(pedidoId).child("estado").setValue(nuevoEstado.name).await()
    }

    /**
     * Elimina el nodo correspondiente a un pedido en Firebase.
     *
     * @param pedidoId Identificador del pedido a eliminar.
     */
    suspend fun deletePedido(pedidoId: String) {
        database.child(pedidoId).removeValue().await()
    }
}
```

---

## Data / Repository (implementaciones)

### `data/repository/AuthRepositoryImpl.kt`

```kotlin
/**
 * Implementación de [AuthRepository] que delega las operaciones de autenticación
 * a [AuthDataSource] y traduce los resultados (o excepciones) al tipo [Result]
 * esperado por la capa de dominio.
 *
 * @property dataSource Fuente de datos usada para realizar las operaciones contra Firebase.
 */
class AuthRepositoryImpl(private val dataSource: AuthDataSource) : AuthRepository {

    /**
     * @see AuthRepository.loginAsAdmin
     *
     * Delega en [AuthDataSource.loginAsAdmin] y envuelve el resultado en [Result],
     * devolviendo fallo si el usuario administrador resulta `null` o si ocurre una excepción.
     */
    override suspend fun loginAsAdmin(): Result<Usuario> {
        return try {
            val admin = dataSource.loginAsAdmin()
            if (admin != null) Result.success(admin)
            else Result.failure(Exception("No se pudo autenticar como admin"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * @see AuthRepository.login
     *
     * Delega en [AuthDataSource.login] y envuelve el resultado en [Result],
     * devolviendo fallo si no se encuentra el usuario o si ocurre una excepción.
     */
    override suspend fun login(nombreUsuario: String): Result<Usuario> {
        return try {
            val user = dataSource.login(nombreUsuario)
            if (user != null) Result.success(user)
            else Result.failure(Exception("Usuario no encontrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * @see AuthRepository.register
     *
     * Delega en [AuthDataSource.register] y envuelve el resultado en [Result],
     * capturando cualquier excepción como fallo.
     */
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

```kotlin
/**
 * Implementación de [UsuarioRepository] que delega directamente todas sus
 * operaciones a [UsuarioDataSource], actuando como puente entre la capa de
 * dominio y la fuente de datos de Firebase.
 *
 * @property dataSource Fuente de datos usada para realizar las operaciones contra Firebase.
 */
class UsuarioRepositoryImpl(private val dataSource: UsuarioDataSource) : UsuarioRepository {

    /** @see UsuarioRepository.getUsuarios */
    override fun getUsuarios(): Flow<List<Usuario>> = dataSource.getUsuarios()

    /** @see UsuarioRepository.addUsuario */
    override suspend fun addUsuario(usuario: Usuario) = dataSource.addUsuario(usuario)

    /** @see UsuarioRepository.updateUsuario */
    override suspend fun updateUsuario(usuario: Usuario) = dataSource.updateUsuario(usuario)

    /** @see UsuarioRepository.deleteUsuario */
    override suspend fun deleteUsuario(id: String) = dataSource.deleteUsuario(id)
}
```

### `data/repository/MesaRepositoryImpl.kt`

```kotlin
/**
 * Implementación de [MesaRepository] que combina la configuración de mesas con
 * la información de ocupación en tiempo real, delegando la persistencia a
 * [MesaDataSource].
 *
 * @property dataSource Fuente de datos usada para realizar las operaciones contra Firebase.
 */
class MesaRepositoryImpl(private val dataSource: MesaDataSource) : MesaRepository {

    /**
     * @see MesaRepository.getTodasLasMesas
     *
     * Combina [MesaDataSource.getMesasConfig] (configuración personalizada de mesas)
     * con [MesaDataSource.getMesasOcupadas] (números de mesa ocupados) para construir
     * la lista completa de mesas:
     *
     * 1. Genera un conjunto base de mesas del 1 al 12 para aquellas que no tengan
     *    configuración personalizada registrada.
     * 2. Une ese conjunto base con las mesas configuradas y las ordena por `id`.
     * 3. Actualiza el [EstadoMesa] de cada mesa a `OCUPADA` o `LIBRE` según si su
     *    `id` está presente en el conjunto de mesas ocupadas.
     *
     * @return [Flow] que emite la lista final de [Mesa] con su estado de ocupación
     * calculado en tiempo real.
     */
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

    /** @see MesaRepository.addMesa */
    override suspend fun addMesa(mesa: Mesa) = dataSource.addMesa(mesa)

    /** @see MesaRepository.updateMesa */
    override suspend fun updateMesa(mesa: Mesa) = dataSource.updateMesa(mesa)

    /** @see MesaRepository.deleteMesa */
    override suspend fun deleteMesa(id: Int) = dataSource.deleteMesa(id)
}
```

### `data/repository/ZonaRepositoryImpl.kt`

```kotlin
/**
 * Implementación de [ZonaRepository] que delega directamente todas sus
 * operaciones a [ZonaDataSource].
 *
 * @property dataSource Fuente de datos usada para realizar las operaciones contra Firebase.
 */
class ZonaRepositoryImpl(private val dataSource: ZonaDataSource) : ZonaRepository {

    /** @see ZonaRepository.getZonas */
    override fun getZonas(): Flow<List<Zona>> = dataSource.getZonas()

    /** @see ZonaRepository.addZona */
    override suspend fun addZona(zona: Zona) = dataSource.addZona(zona)

    /** @see ZonaRepository.updateZona */
    override suspend fun updateZona(zona: Zona) = dataSource.updateZona(zona)

    /** @see ZonaRepository.deleteZona */
    override suspend fun deleteZona(id: String) = dataSource.deleteZona(id)
}
```

### `data/repository/MenuRepositoryImpl.kt`

```kotlin
/**
 * Implementación de [MenuRepository] que delega directamente todas sus
 * operaciones a [MenuDataSource].
 *
 * @property dataSource Fuente de datos usada para realizar las operaciones contra Firebase.
 */
class MenuRepositoryImpl(private val dataSource: MenuDataSource) : MenuRepository {

    /** @see MenuRepository.getMenu */
    override fun getMenu(): Flow<List<Platillo>> = dataSource.getMenu()

    /** @see MenuRepository.addPlatillo */
    override suspend fun addPlatillo(platillo: Platillo) = dataSource.addPlatillo(platillo)

    /** @see MenuRepository.updatePlatillo */
    override suspend fun updatePlatillo(platillo: Platillo) = dataSource.updatePlatillo(platillo)

    /** @see MenuRepository.deletePlatillo */
    override suspend fun deletePlatillo(id: String) = dataSource.deletePlatillo(id)
}
```

### `data/repository/PedidoRepositoryImpl.kt`

```kotlin
/**
 * Implementación de [PedidoRepository] que delega directamente todas sus
 * operaciones a [PedidoDataSource].
 *
 * @property dataSource Fuente de datos usada para realizar las operaciones contra Firebase.
 */
class PedidoRepositoryImpl(private val dataSource: PedidoDataSource) : PedidoRepository {

    /** @see PedidoRepository.getPedidos */
    override fun getPedidos(): Flow<List<Pedido>> = dataSource.getPedidos()

    /** @see PedidoRepository.addPedido */
    override suspend fun addPedido(pedido: Pedido, items: List<Map<String, String>>) =
        dataSource.addPedido(pedido, items)

    /** @see PedidoRepository.updateEstado */
    override suspend fun updateEstado(pedidoId: String, nuevoEstado: EstadoPedido) =
        dataSource.updateEstado(pedidoId, nuevoEstado)

    /** @see PedidoRepository.deletePedido */
    override suspend fun deletePedido(pedidoId: String) = dataSource.deletePedido(pedidoId)
}
```


## [Regresar al README principal](/README.md)
