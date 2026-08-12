# Código del Módulo TV

## [Regresar al README principal](/README.md)

Este documento describe el propósito de cada archivo del módulo `tv`, organizado según la arquitectura por capas (Clean Architecture): `presentation`, `domain` y `data`.

---

## Raíz del módulo

### `MainActivity.kt`
Actividad principal y punto de entrada de la app de Android TV. Se encarga de construir manualmente el árbol de dependencias (repositorio → casos de uso → `ViewModel.Factory`) y lanzar `KitchenScreen`, la pantalla única de monitorización de cocina, envuelta en el tema de Compose para TV.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Capa de datos
        val repository = PedidoRepositoryImpl()

        // Casos de uso
        val observarPedidos = ObservarPedidosUseCase(repository)
        val actualizarEstado = ActualizarEstadoPedidoUseCase(repository)
        val eliminarPedido = EliminarPedidoUseCase(repository)

        // Factory del ViewModel
        val viewModelFactory = KitchenViewModel.Factory(
            observarPedidos, actualizarEstado, eliminarPedido
        )

        setContent {
            MaterialTheme {
                KitchenScreen(
                    viewModel = viewModel(factory = viewModelFactory)
                )
            }
        }
    }
}
```

---

## Presentation / Kitchen

### `presentation/kitchen/KitchenScreen.kt`
Pantalla principal del panel de cocina. Muestra la lista de pedidos entrantes en tiempo real, un fondo dinámico con la imagen del pedido enfocado o seleccionado, y coordina la navegación por control remoto (foco) entre las tarjetas de pedidos y el detalle.

```kotlin
@Composable
fun KitchenScreen(
    viewModel: KitchenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var pedidoEnfocado by remember { mutableStateOf<Pedido?>(null) }  // ← estado local para el foco

    val configuration = LocalConfiguration.current
    val margenH = (configuration.screenWidthDp * 0.05f).dp
    val margenV = (configuration.screenHeightDp * 0.027f).dp

    val urlPorDefecto = "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=900&auto=format&fit=crop"

    // Fondo: pedido seleccionado o, si no hay, el último enfocado
    val fondoPedido = uiState.pedidoSeleccionado ?: pedidoEnfocado

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        Crossfade(targetState = fondoPedido, animationSpec = tween(400), label = "fondo") { pedido ->
            if (pedido != null) {
                Box(Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = pedido.imagenUrl.ifEmpty { urlPorDefecto },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(60.dp) else Modifier)
                    )
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)))
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            colors = SurfaceDefaults.colors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            )
        ) {
            if (uiState.cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando pedidos...", color = Color.White, fontSize = 24.sp)
                }
            } else if (uiState.pedidoSeleccionado == null) {
                MainDashboard(
                    pedidos = uiState.pedidos.filter { it.estado == EstadoPedido.EN_PREPARACION },
                    entregados = uiState.pedidos.filter { it.estado == EstadoPedido.LISTO },
                    onSelectPedido = viewModel::seleccionarPedido,
                    onFocusChange = { pedido, focused ->   // ← callback para actualizar el fondo
                        if (focused) pedidoEnfocado = pedido
                    },
                    margenH = margenH,
                    margenV = margenV
                )
            } else {
                OrderDetail(
                    pedido = uiState.pedidoSeleccionado!!,
                    hayAnterior = uiState.indiceSeleccionado > 0,
                    haySiguiente = uiState.indiceSeleccionado < uiState.listaSeleccionada.size - 1,
                    onAnterior = viewModel::irAnterior,
                    onSiguiente = viewModel::irSiguiente,
                    onBack = viewModel::volver,
                    onCompletar = viewModel::completarPedido,
                    onEliminar = viewModel::eliminarPedido
                )
            }
        }
    }
}
```

### `presentation/kitchen/KitchenViewScreen.kt`
`ViewModel` de la pantalla de cocina (`KitchenViewModel`). Observa los pedidos en tiempo real mediante `ObservarPedidosUseCase`, mantiene el estado de la UI (`KitchenUiState`: lista de pedidos, pedido seleccionado, índice de navegación) y expone las acciones de seleccionar, avanzar de estado o eliminar un pedido delegando en los casos de uso correspondientes.

```kotlin
class KitchenViewModel(
    private val observarPedidos: ObservarPedidosUseCase,
    private val actualizarEstado: ActualizarEstadoPedidoUseCase,
    private val eliminarPedido: EliminarPedidoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(KitchenUiState())
    val uiState: StateFlow<KitchenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observarPedidos().collect { pedidos ->
                _uiState.update { it.copy(pedidos = pedidos, cargando = false) }
            }
        }
    }

    fun seleccionarPedido(pedido: Pedido, lista: List<Pedido>) {
        _uiState.update {
            it.copy(
                pedidoSeleccionado = pedido,
                listaSeleccionada = lista,
                indiceSeleccionado = lista.indexOf(pedido)
            )
        }
    }

    fun irAnterior() {
        _uiState.update { state ->
            val nuevoIndice = state.indiceSeleccionado - 1
            if (nuevoIndice >= 0) {
                state.copy(
                    pedidoSeleccionado = state.listaSeleccionada[nuevoIndice],
                    indiceSeleccionado = nuevoIndice
                )
            } else state
        }
    }

    fun irSiguiente() {
        _uiState.update { state ->
            val nuevoIndice = state.indiceSeleccionado + 1
            if (nuevoIndice < state.listaSeleccionada.size) {
                state.copy(
                    pedidoSeleccionado = state.listaSeleccionada[nuevoIndice],
                    indiceSeleccionado = nuevoIndice
                )
            } else state
        }
    }

    fun volver() {
        _uiState.update { it.copy(pedidoSeleccionado = null) }
    }

    fun completarPedido(pedido: Pedido) {
        viewModelScope.launch {
            actualizarEstado(pedido.id, EstadoPedido.LISTO)
            // Al cambiar en Firebase, el Flow se actualizará automáticamente
            // Volvemos a la pantalla principal
            volver()
        }
    }

    fun eliminarPedido(pedido: Pedido) {
        viewModelScope.launch {
            eliminarPedido(pedido.id)
            volver()
        }
    }

    // Factory para poder instanciar el ViewModel pasando los casos de uso
    class Factory(
        private val observarPedidos: ObservarPedidosUseCase,
        private val actualizarEstado: ActualizarEstadoPedidoUseCase,
        private val eliminarPedido: EliminarPedidoUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(KitchenViewModel::class.java)) {
                return KitchenViewModel(observarPedidos, actualizarEstado, eliminarPedido) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class KitchenUiState(
    val pedidos: List<Pedido> = emptyList(),
    val pedidoSeleccionado: Pedido? = null,
    val listaSeleccionada: List<Pedido> = emptyList(),
    val indiceSeleccionado: Int = 0,
    val cargando: Boolean = true
)
```

---

## Presentation / Kitchen / Components

### `presentation/kitchen/components/MainDashboard.kt`
Composable que arma el layout principal del dashboard: organiza las filas/columnas (`TvLazyRow`/`TvLazyColumn`) de pedidos agrupados, típicamente por estado, optimizado para navegación con control remoto.

```kotlin
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
```

### `presentation/kitchen/components/OrderCard.kt`
Composable de la tarjeta individual de un pedido dentro de la lista. Muestra mesa, estado y datos resumidos del pedido, y cambia su apariencia visual al recibir el foco (`onFocusChanged`).

```kotlin
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun OrderCard(
    pedido: Pedido,
    onSelect: (Pedido) -> Unit,
    onFocusChange: (Pedido, Boolean) -> Unit = { _, _ -> },  // ← nuevo parámetro con valor por defecto
    focusRequester: FocusRequester? = null
) {
    val cardShape = RoundedCornerShape(24.dp)

    Surface(
        onClick = { onSelect(pedido) },
        shape = ClickableSurfaceDefaults.shape(cardShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.DarkGray,
            focusedContainerColor = Color.DarkGray,
            pressedContainerColor = Color.DarkGray
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(4.dp, Color.White), shape = cardShape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        modifier = Modifier
            .width(360.dp)
            .height(220.dp)
            .onFocusChanged { onFocusChange(pedido, it.isFocused) }   // ← notificar cambios de foco
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
    ) {
        Box(modifier = Modifier.clip(cardShape)) {
            AsyncImage(
                model = pedido.imagenUrl.ifEmpty {
                    "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=360&h=220&auto=format&fit=crop"
                },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
            )
            Text(
                text = "Mesa ${pedido.mesa}",
                modifier = Modifier.align(Alignment.BottomStart).padding(24.dp),
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp
            )
        }
    }
}
```

### `presentation/kitchen/components/OrderDetail.kt`
Composable del panel de detalle de un pedido. Muestra la información completa (platillos, notas, imagen) y los botones de acción para marcar como listo/entregado o eliminar, incluyendo el manejo del botón "atrás" (`BackHandler`).

```kotlin
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun OrderDetail(
    pedido: Pedido,
    hayAnterior: Boolean,
    haySiguiente: Boolean,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onBack: () -> Unit,
    onCompletar: (Pedido) -> Unit,
    onEliminar: (Pedido) -> Unit
) {
    BackHandler(onBack = onBack)

    var mostrarDialogoCompletar by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    val items = remember(pedido.id) {
        pedido.items.ifEmpty { listOf(ItemPedido(descripcion = pedido.descripcion, nota = pedido.nota)) }
    }
    var indiceItemActivo by remember(pedido.id) { mutableStateOf(0) }

    LaunchedEffect(pedido.id, items.size) {
        if (items.size <= 1) return@LaunchedEffect
        while (true) {
            delay(10_000)
            indiceItemActivo = (indiceItemActivo + 1) % items.size
        }
    }

    val focusCompletar = remember(pedido.id) { FocusRequester() }
    val focusAnterior = remember(pedido.id) { FocusRequester() }
    val focusSiguiente = remember(pedido.id) { FocusRequester() }

    LaunchedEffect(pedido.id, pedido.estado, hayAnterior, haySiguiente) {
        when {
            pedido.estado == EstadoPedido.EN_PREPARACION -> focusCompletar.requestFocus()
            haySiguiente -> focusSiguiente.requestFocus()
            hayAnterior -> focusAnterior.requestFocus()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavArrowButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Mesa anterior",
            habilitado = hayAnterior,
            onClick = onAnterior,
            focusRequester = focusAnterior
        )

        Spacer(Modifier.width(32.dp))

        Row(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalAlignment = Alignment.Top
        ) {

            Column(modifier = Modifier.width(420.dp).fillMaxHeight()) {
                Text("Mesa ${pedido.mesa}", fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Normal)
                Spacer(Modifier.height(16.dp))

                AsyncImage(
                    model = pedido.imagenUrl.ifEmpty {
                        "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=420&h=300&auto=format&fit=crop"
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(20.dp))

                if (pedido.estado == EstadoPedido.EN_PREPARACION) {
                    ActionButton(
                        texto = "Completar pedido",
                        icono = Icons.Default.Check,
                        focusRequester = focusCompletar,
                        onClick = { mostrarDialogoCompletar = true }
                    )
                    Spacer(Modifier.height(14.dp))
                    ActionButton(
                        texto = "Eliminar pedido",
                        icono = Icons.Default.Delete,
                        onClick = { mostrarDialogoEliminar = true }
                    )
                } else {
                    val diff = System.currentTimeMillis() - pedido.timestamp
                    val minutos = TimeUnit.MILLISECONDS.toMinutes(diff)
                    Text(
                        "Terminada hace $minutos minutos",
                        fontSize = 22.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(48.dp))

            Column(modifier = Modifier.weight(1f)) {
                items.forEachIndexed { index, item ->
                    Text(
                        text = item.descripcion,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = if (index == indiceItemActivo) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                Text("Nota:", color = Color.White, fontSize = 22.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    items[indiceItemActivo].nota.ifEmpty { "Sin notas" },
                    color = Color.White,
                    fontSize = 22.sp
                )
            }
        }

        Spacer(Modifier.width(32.dp))

        NavArrowButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Mesa siguiente",
            habilitado = haySiguiente,
            onClick = onSiguiente,
            focusRequester = focusSiguiente
        )
    }

    if (mostrarDialogoCompletar) {
        DialogoConfirmacion(
            titulo = "¿Estás seguro que quieres completar el pedido?",
            textoConfirmar = "Aceptar",
            esDestructivo = false,
            onConfirmar = {
                mostrarDialogoCompletar = false
                onCompletar(pedido)
            },
            onCancelar = { mostrarDialogoCompletar = false }
        )
    }

    if (mostrarDialogoEliminar) {
        DialogoConfirmacion(
            titulo = "¿Estás seguro que quieres eliminar el pedido?",
            textoConfirmar = "Eliminar",
            esDestructivo = true,
            onConfirmar = {
                mostrarDialogoEliminar = false
                onEliminar(pedido)
            },
            onCancelar = { mostrarDialogoEliminar = false }
        )
    }
}
```

### `presentation/kitchen/components/ActionButton.kt`
Composable reutilizable de botón de acción con estilo para TV (borde, ícono, texto) y soporte de foco mediante `FocusRequester`, usado en las acciones sobre un pedido.

```kotlin
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ActionButton(
    texto: String,
    icono: ImageVector,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(32.dp)

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF616161),
            contentColor = Color.White,
            focusedContainerColor = Color.White,
            focusedContentColor = Color.Black
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(3.dp, Color.White), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(texto, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
```

### `presentation/kitchen/components/NavArrowButton.kt`
Composable de botón de flecha de navegación (izquierda/derecha) en forma circular, usado para desplazarse entre pedidos dentro del detalle.

```kotlin
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
```

### `presentation/kitchen/components/DialogoConfirmation.kt`
Composable de diálogo de confirmación en pantalla completa (por ejemplo, para confirmar la eliminación o cancelación de un pedido), con foco automático en su botón principal al aparecer.

```kotlin
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
```

### `presentation/kitchen/components/FadingEdgeModifier.kt`
`Modifier` de Compose personalizado que aplica un degradado (fade) en los bordes de una lista horizontal (`TvLazyListState`), usado para indicar visualmente que hay más contenido desplazable.

```kotlin
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
```

---

## Domain / Model

### `domain/model/Pedido.kt`
Define las entidades del dominio de cocina: el enum `EstadoPedido` (pendiente, en preparación, listo, entregado, cancelado), `PlatilloSeleccionado`, `ItemPedido` (descripción y nota) y la entidad principal `Pedido` con mesa, estado, timestamp, imagen y lista de ítems/platillos.

```kotlin
enum class EstadoPedido {
    PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO
}

data class PlatilloSeleccionado(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1
)

data class ItemPedido(
    val descripcion: String = "",
    val nota: String = ""
)

data class Pedido(
    val id: String = "",
    val mesa: Int = 0,
    val descripcion: String = "",
    val nota: String = "",
    val estado: EstadoPedido = EstadoPedido.EN_PREPARACION,
    val timestamp: Long = 0,
    val imagenUrl: String = "",
    val items: List<ItemPedido> = emptyList(),
    val platillos: List<PlatilloSeleccionado> = emptyList()
)
```

---

## Domain / Repository (interfaz)

### `domain/repository/PedidoRepository.kt`
Contrato del repositorio de pedidos para el módulo de cocina: observar los pedidos en tiempo real, actualizar el estado de un pedido y eliminarlo (cancelarlo).

```kotlin
interface PedidoRepository {
    fun observarPedidos(): Flow<List<Pedido>>
    suspend fun actualizarEstado(id: String, nuevoEstado: EstadoPedido)
    suspend fun eliminarPedido(id: String)
}
```

---

## Domain / Usecase

### `domain/usecase/ObservarPedidosUseCase.kt`
Caso de uso que expone el flujo (`Flow`) de pedidos en tiempo real desde el repositorio hacia el ViewModel.

```kotlin
class ObservarPedidosUseCase(private val repository: PedidoRepository) {
    operator fun invoke(): Flow<List<Pedido>> = repository.observarPedidos()
}
```

### `domain/usecase/ActualizarPedidoUseCase.kt`
Caso de uso que encapsula la acción de cambiar el estado de un pedido (por ejemplo, de "en preparación" a "listo").

```kotlin
class ActualizarEstadoPedidoUseCase(private val repository: PedidoRepository) {
    suspend operator fun invoke(id: String, nuevoEstado: EstadoPedido) {
        repository.actualizarEstado(id, nuevoEstado)
    }
}
```

### `domain/usecase/EliminarPedidoUseCase.kt`
Caso de uso que encapsula la acción de eliminar/cancelar un pedido.

```kotlin
class EliminarPedidoUseCase(private val repository: PedidoRepository) {
    suspend operator fun invoke(id: String) {
        repository.eliminarPedido(id)
    }
}
```

---

## Data / Repository (implementación)

### `data/repository/PedidoRepositoryImpl.kt`
Implementa `PedidoRepository` conectándose directamente al nodo `pedidos` de Firebase Realtime Database. Escucha los cambios con un `ValueEventListener` envuelto en `callbackFlow`, parsea cada pedido (incluyendo su lista de ítems) y expone la lista actualizada como `Flow`. También aplica las actualizaciones de estado (incluyendo el timestamp cuando un pedido pasa a "listo") y el marcado como cancelado al eliminar.

```kotlin
class PedidoRepositoryImpl(
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("pedidos")
) : PedidoRepository {

    override fun observarPedidos(): Flow<List<Pedido>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pedidos = mutableListOf<Pedido>()
                if (snapshot.exists()) {
                    snapshot.children.forEach { child ->
                        try {
                            val id = child.key ?: ""
                            val mesa = child.child("mesa").value?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                            val desc = child.child("descripcion").value?.toString() ?: ""
                            val nota = child.child("nota").value?.toString() ?: ""
                            val estadoStr = child.child("estado").value?.toString() ?: "EN_PREPARACION"
                            val time = child.child("timestamp").value?.toString()?.toLongOrNull() ?: 0L
                            val imagen = child.child("imagenUrl").value?.toString() ?: ""
                            val listaItems = mutableListOf<ItemPedido>()
                            child.child("items").children.forEach { itemSnap ->
                                val descItem = itemSnap.child("descripcion").value?.toString() ?: ""
                                val notaItem = itemSnap.child("nota").value?.toString() ?: ""
                                if (descItem.isNotEmpty()) {
                                    listaItems.add(ItemPedido(descripcion = descItem, nota = notaItem))
                                }
                            }
                            pedidos.add(
                                Pedido(
                                    id = id, mesa = mesa, descripcion = desc, nota = nota,
                                    estado = try { EstadoPedido.valueOf(estadoStr) } catch (e: Exception) { EstadoPedido.EN_PREPARACION },
                                    timestamp = time, imagenUrl = imagen,
                                    items = listaItems
                                )
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("CocinaTV", "Error en pedido ${child.key}: ${e.message}")
                        }
                    }
                }
                trySend(pedidos)
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("CocinaTV", "Error al observar pedidos: ${error.message} (code=${error.code})")
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }.flowOn(Dispatchers.IO)

    override suspend fun actualizarEstado(id: String, nuevoEstado: EstadoPedido) {
        database.child(id).child("estado").setValue(nuevoEstado.name)
        if (nuevoEstado == EstadoPedido.LISTO) {
            database.child(id).child("timestamp").setValue(ServerValue.TIMESTAMP)
        }
    }

    override suspend fun eliminarPedido(id: String) {
        database.child(id).child("estado").setValue("CANCELADO")
    }
}
```


## [Regresar al README principal](/README.md)