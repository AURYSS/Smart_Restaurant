# Código del Módulo TV

## [Regresar al README principal](/README.md)

Este documento describe el propósito de cada archivo del módulo `tv`, organizado según la arquitectura por capas (Clean Architecture): `presentation`, `domain` y `data`. Se agregó documentación KDoc a cada clase y función.

---

## Raíz del módulo

### `MainActivity.kt`

```kotlin
/**
 * Actividad principal y punto de entrada de la app de Android TV.
 *
 * Se encarga de construir manualmente el árbol de dependencias
 * (repositorio → casos de uso → [KitchenViewModel.Factory]) siguiendo
 * un patrón de inyección de dependencias manual, y de lanzar
 * [KitchenScreen], la única pantalla de la app dedicada a la
 * monitorización de cocina, envuelta en el tema de Compose para TV.
 */
class MainActivity : ComponentActivity() {

    /**
     * Callback del ciclo de vida invocado al crear la actividad.
     *
     * Construye la cadena de dependencias en orden:
     * 1. [PedidoRepositoryImpl] como implementación concreta de acceso a datos.
     * 2. Los casos de uso ([ObservarPedidosUseCase], [ActualizarEstadoPedidoUseCase],
     *    [EliminarPedidoUseCase]) que envuelven al repositorio.
     * 3. [KitchenViewModel.Factory], necesaria porque el ViewModel no tiene
     *    un constructor vacío.
     *
     * Finalmente define el contenido de Compose con [setContent], mostrando
     * [KitchenScreen] dentro de un `MaterialTheme`.
     *
     * @param savedInstanceState estado previamente guardado de la actividad,
     * o `null` si es una creación nueva.
     */
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

```kotlin
/**
 * Pantalla principal del panel de cocina.
 *
 * Muestra la lista de pedidos entrantes en tiempo real (a través de
 * [KitchenViewModel]), un fondo dinámico con la imagen del pedido enfocado
 * o seleccionado (usando [Crossfade] para animar la transición), y coordina
 * la navegación por control remoto (foco) entre las tarjetas de pedidos
 * ([MainDashboard]) y la vista de detalle ([OrderDetail]).
 *
 * El fondo se calcula con prioridad: primero el pedido seleccionado
 * (`uiState.pedidoSeleccionado`); si no hay ninguno seleccionado, se usa
 * el último pedido que recibió el foco (`pedidoEnfocado`), permitiendo que
 * el fondo reaccione a la navegación del usuario incluso antes de que
 * confirme una selección.
 *
 * @param viewModel instancia de [KitchenViewModel] que provee el estado de
 * la UI y expone las acciones sobre los pedidos. Por defecto se obtiene
 * mediante `viewModel()`.
 */
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

```kotlin
/**
 * `ViewModel` de la pantalla de cocina.
 *
 * Observa los pedidos en tiempo real mediante [ObservarPedidosUseCase],
 * mantiene el estado de la UI ([KitchenUiState]: lista de pedidos, pedido
 * seleccionado, índice de navegación) y expone las acciones de seleccionar,
 * avanzar de estado o eliminar un pedido, delegando en los casos de uso
 * correspondientes.
 *
 * @property observarPedidos caso de uso que expone el [Flow] de pedidos en tiempo real.
 * @property actualizarEstado caso de uso que cambia el estado de un pedido.
 * @property eliminarPedido caso de uso que elimina/cancela un pedido.
 */
class KitchenViewModel(
    private val observarPedidos: ObservarPedidosUseCase,
    private val actualizarEstado: ActualizarEstadoPedidoUseCase,
    private val eliminarPedido: EliminarPedidoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(KitchenUiState())

    /** Estado observable de la UI de cocina, expuesto de forma inmutable a la vista. */
    val uiState: StateFlow<KitchenUiState> = _uiState.asStateFlow()

    /**
     * Bloque de inicialización: suscribe el `ViewModel` al flujo de pedidos
     * expuesto por [observarPedidos] y actualiza [uiState] cada vez que
     * llega una nueva lista, marcando `cargando = false` tras la primera emisión.
     */
    init {
        viewModelScope.launch {
            observarPedidos().collect { pedidos ->
                _uiState.update { it.copy(pedidos = pedidos, cargando = false) }
            }
        }
    }

    /**
     * Marca un pedido como seleccionado para mostrar su vista de detalle.
     *
     * Guarda también la lista de origen (`lista`) desde la que se seleccionó,
     * para poder navegar con [irAnterior]/[irSiguiente] dentro de ese mismo
     * grupo (por ejemplo, solo entre pedidos "en preparación").
     *
     * @param pedido pedido que pasa a estar seleccionado.
     * @param lista lista completa a la que pertenece [pedido], usada como
     * contexto de navegación.
     */
    fun seleccionarPedido(pedido: Pedido, lista: List<Pedido>) {
        _uiState.update {
            it.copy(
                pedidoSeleccionado = pedido,
                listaSeleccionada = lista,
                indiceSeleccionado = lista.indexOf(pedido)
            )
        }
    }

    /**
     * Navega al pedido anterior dentro de `listaSeleccionada`.
     *
     * Si el índice actual ya es el primero (0), el estado no cambia.
     */
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

    /**
     * Navega al pedido siguiente dentro de `listaSeleccionada`.
     *
     * Si el índice actual ya es el último, el estado no cambia.
     */
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

    /**
     * Vuelve del detalle a la pantalla principal, limpiando el pedido
     * seleccionado en [uiState] (`pedidoSeleccionado = null`).
     */
    fun volver() {
        _uiState.update { it.copy(pedidoSeleccionado = null) }
    }

    /**
     * Marca un pedido como completado (estado [EstadoPedido.LISTO]) a través
     * de [actualizarEstado] y regresa a la pantalla principal con [volver].
     *
     * No es necesario actualizar manualmente la lista local: al cambiar el
     * valor en Firebase, el [Flow] observado en [init] se actualiza
     * automáticamente y refresca [uiState].
     *
     * @param pedido pedido a marcar como completado.
     */
    fun completarPedido(pedido: Pedido) {
        viewModelScope.launch {
            actualizarEstado(pedido.id, EstadoPedido.LISTO)
            // Al cambiar en Firebase, el Flow se actualizará automáticamente
            // Volvemos a la pantalla principal
            volver()
        }
    }

    /**
     * Elimina (cancela) un pedido a través de [eliminarPedido] y regresa a
     * la pantalla principal con [volver].
     *
     * @param pedido pedido a eliminar/cancelar.
     */
    fun eliminarPedido(pedido: Pedido) {
        viewModelScope.launch {
            eliminarPedido(pedido.id)
            volver()
        }
    }

    /**
     * Fábrica de [KitchenViewModel].
     *
     * Necesaria porque el `ViewModel` requiere parámetros en su constructor
     * (los tres casos de uso) y por lo tanto no puede instanciarse con el
     * mecanismo por defecto de `ViewModelProvider`.
     *
     * @property observarPedidos caso de uso inyectado al `ViewModel` creado.
     * @property actualizarEstado caso de uso inyectado al `ViewModel` creado.
     * @property eliminarPedido caso de uso inyectado al `ViewModel` creado.
     */
    class Factory(
        private val observarPedidos: ObservarPedidosUseCase,
        private val actualizarEstado: ActualizarEstadoPedidoUseCase,
        private val eliminarPedido: EliminarPedidoUseCase
    ) : ViewModelProvider.Factory {

        /**
         * Crea una instancia de [KitchenViewModel] si [modelClass] es compatible.
         *
         * @param modelClass clase del `ViewModel` solicitado por el framework.
         * @return instancia de [KitchenViewModel] casteada al tipo genérico [T].
         * @throws IllegalArgumentException si [modelClass] no es [KitchenViewModel].
         */
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(KitchenViewModel::class.java)) {
                return KitchenViewModel(observarPedidos, actualizarEstado, eliminarPedido) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Estado inmutable de la UI de la pantalla de cocina.
 *
 * @property pedidos lista completa de pedidos observados en tiempo real.
 * @property pedidoSeleccionado pedido actualmente mostrado en detalle, o
 * `null` si se está mostrando el dashboard principal.
 * @property listaSeleccionada lista de origen (por ejemplo, "en preparación"
 * o "entregados") desde la que se navega en el detalle con anterior/siguiente.
 * @property indiceSeleccionado posición de [pedidoSeleccionado] dentro de
 * [listaSeleccionada], usada para calcular si hay pedido anterior/siguiente.
 * @property cargando indica si aún no se ha recibido la primera emisión de
 * pedidos desde Firebase.
 */
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

```kotlin
/**
 * Composable que arma el layout principal del dashboard de cocina.
 *
 * Organiza dos secciones desplazables verticalmente ([TvLazyColumn]):
 * "Pedidos" (en preparación) y "Entregados" (listos), cada una mostrada
 * como una fila horizontal ([TvLazyRow]) de [OrderCard], optimizadas para
 * navegación con control remoto mediante `pivotOffsets` y solicitud de foco
 * automática en la primera tarjeta al cargar los datos.
 *
 * @param pedidos lista de pedidos en preparación, mostrada en la sección "Pedidos".
 * @param entregados lista de pedidos listos, mostrada en la sección "Entregados".
 * @param onSelectPedido callback invocado al confirmar la selección de un
 * pedido, recibiendo el pedido y la lista de origen (para navegación en el detalle).
 * @param onFocusChange callback invocado cuando una tarjeta gana o pierde el
 * foco del control remoto, usado por la pantalla contenedora para actualizar
 * el fondo dinámico.
 * @param margenH margen horizontal aplicado al contenido del dashboard.
 * @param margenV margen vertical aplicado al contenido del dashboard.
 */
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

```kotlin
/**
 * Composable de la tarjeta individual de un pedido dentro de la lista.
 *
 * Muestra la mesa y una imagen representativa del pedido (o una imagen por
 * defecto si `imagenUrl` está vacío), y cambia su apariencia visual
 * (borde, escala) al recibir el foco del control remoto.
 *
 * @param pedido pedido representado por esta tarjeta.
 * @param onSelect callback invocado cuando el usuario confirma la selección
 * (clic/OK) sobre la tarjeta.
 * @param onFocusChange callback invocado en cada cambio de foco de la
 * tarjeta, reportando el pedido y si quedó enfocada (`true`) o no (`false`).
 * @param focusRequester solicitante de foco opcional; se usa típicamente en
 * la primera tarjeta de la lista para recibir el foco inicial automáticamente.
 */
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

```kotlin
/**
 * Composable del panel de detalle de un pedido.
 *
 * Muestra la información completa del pedido (mesa, imagen, lista de ítems
 * con rotación automática cada 10 segundos cuando hay más de uno, y nota
 * del ítem activo) junto con los botones de acción para marcar como
 * listo/entregado o eliminar (cada uno con su diálogo de confirmación).
 * También maneja la navegación entre pedidos con flechas izquierda/derecha
 * y el botón "atrás" del control remoto mediante [BackHandler].
 *
 * El foco inicial se asigna automáticamente según el contexto: al botón
 * "Completar pedido" si el pedido sigue en preparación, o a la flecha de
 * navegación disponible (siguiente o anterior) en caso contrario.
 *
 * @param pedido pedido mostrado en el detalle.
 * @param hayAnterior indica si existe un pedido anterior en la lista de
 * navegación (habilita la flecha izquierda).
 * @param haySiguiente indica si existe un pedido siguiente en la lista de
 * navegación (habilita la flecha derecha).
 * @param onAnterior callback invocado al navegar al pedido anterior.
 * @param onSiguiente callback invocado al navegar al pedido siguiente.
 * @param onBack callback invocado al presionar "atrás" para volver al dashboard.
 * @param onCompletar callback invocado al confirmar que el pedido fue completado.
 * @param onEliminar callback invocado al confirmar la eliminación del pedido.
 */
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

```kotlin
/**
 * Composable reutilizable de botón de acción con estilo para TV.
 *
 * Muestra un ícono junto a un texto dentro de una superficie clickeable
 * ([Surface]) con borde y escala resaltados al recibir el foco del control
 * remoto. Se usa para las acciones principales sobre un pedido (por
 * ejemplo, "Completar pedido" o "Eliminar pedido").
 *
 * @param texto etiqueta mostrada junto al ícono.
 * @param icono ícono mostrado a la izquierda del texto.
 * @param focusRequester solicitante de foco opcional, usado para asignar
 * el foco inicial de la pantalla a este botón cuando corresponde.
 * @param onClick callback invocado al confirmar la acción (clic/OK).
 */
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

```kotlin
/**
 * Composable de botón de flecha de navegación (izquierda/derecha) en forma
 * circular, usado para desplazarse entre pedidos dentro de la vista de detalle.
 *
 * Cuando [habilitado] es `false`, el botón se muestra como un ícono atenuado
 * y no interactivo (sin `Surface` clickeable), evitando que el control
 * remoto pueda enfocarlo o activarlo.
 *
 * @param icon ícono de la flecha a mostrar.
 * @param contentDescription descripción de accesibilidad del botón.
 * @param habilitado indica si hay un pedido disponible en esa dirección;
 * controla si el botón es interactivo o solo decorativo.
 * @param onClick callback invocado al activar el botón (solo si [habilitado] es `true`).
 * @param focusRequester solicitante de foco opcional, usado para dirigir el
 * foco inicial de la pantalla hacia esta flecha cuando corresponde.
 */
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

```kotlin
/**
 * Composable de diálogo de confirmación en pantalla completa.
 *
 * Se usa, por ejemplo, para confirmar la finalización o eliminación de un
 * pedido antes de ejecutar la acción. Muestra un título, y dos botones
 * ("Cancelar" y uno de confirmación configurable), con foco automático en
 * el botón "Cancelar" al aparecer, como medida de seguridad ante acciones
 * potencialmente destructivas.
 *
 * @param titulo texto principal del diálogo, describiendo la pregunta de confirmación.
 * @param textoConfirmar etiqueta del botón de confirmación (por ejemplo,
 * "Aceptar" o "Eliminar").
 * @param esDestructivo si es `true`, el botón de confirmación se resalta en
 * rojo para indicar que la acción es irreversible o destructiva.
 * @param onConfirmar callback invocado al presionar el botón de confirmación.
 * @param onCancelar callback invocado al presionar "Cancelar".
 */
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

```kotlin
/**
 * `Modifier` de Compose personalizado que aplica un degradado (fade) en los
 * bordes izquierdo y/o derecho de una lista horizontal.
 *
 * Se usa para indicar visualmente que hay más contenido desplazable en esa
 * dirección: el fundido izquierdo aparece si la lista ya se desplazó hacia
 * adelante (`firstVisibleItemIndex > 0` o `firstVisibleItemScrollOffset > 0`),
 * y el fundido derecho aparece si aún puede desplazarse más
 * (`listState.canScrollForward`).
 *
 * Internamente dibuja el contenido normalmente y luego superpone un
 * degradado con `BlendMode.DstIn` para recortar la opacidad en los bordes,
 * usando una capa offscreen (`CompositingStrategy.Offscreen`) para que el
 * blend mode funcione correctamente sobre el contenido ya dibujado.
 *
 * @param listState estado de la lista horizontal ([TvLazyListState]) sobre
 * la que se calcula si mostrar el fundido izquierdo y/o derecho.
 * @param edgeWidth ancho del área de degradado en cada borde. Por defecto, 80.dp.
 * @return el [Modifier] original con el efecto de fundido en los bordes aplicado.
 */
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

```kotlin
/**
 * Representa los posibles estados por los que atraviesa un [Pedido] dentro
 * del flujo de cocina.
 */
enum class EstadoPedido {
    /** El pedido fue creado pero aún no se comenzó a preparar. */
    PENDIENTE,
    /** El pedido está siendo preparado en cocina. */
    EN_PREPARACION,
    /** El pedido fue completado y está listo para entregar. */
    LISTO,
    /** El pedido fue entregado al cliente. */
    ENTREGADO,
    /** El pedido fue cancelado. */
    CANCELADO
}

/**
 * Representa un platillo específico elegido dentro de un pedido, con su
 * cantidad y precio unitario.
 *
 * @property id identificador del platillo.
 * @property nombre nombre del platillo.
 * @property precio precio unitario del platillo.
 * @property cantidad cantidad seleccionada de este platillo dentro del pedido.
 */
data class PlatilloSeleccionado(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1
)

/**
 * Representa un ítem individual dentro de un pedido, con su descripción y
 * una nota opcional (por ejemplo, indicaciones especiales de preparación).
 *
 * @property descripcion texto que describe el ítem (por ejemplo, el nombre
 * del platillo o una línea del pedido).
 * @property nota indicaciones adicionales asociadas a este ítem.
 */
data class ItemPedido(
    val descripcion: String = "",
    val nota: String = ""
)

/**
 * Entidad principal del dominio que representa un pedido de cocina.
 *
 * @property id identificador único del pedido (clave del nodo en Firebase).
 * @property mesa número de mesa asociada al pedido.
 * @property descripcion descripción general del pedido, usada como
 * respaldo cuando no hay [items] definidos.
 * @property nota nota general del pedido, usada como respaldo cuando no
 * hay [items] definidos.
 * @property estado estado actual del pedido dentro del flujo de cocina.
 * @property timestamp marca de tiempo asociada al pedido (por ejemplo, el
 * momento en que pasó a estado [EstadoPedido.LISTO]).
 * @property imagenUrl URL de la imagen representativa del pedido; si está
 * vacía, la UI usa una imagen por defecto.
 * @property items lista de ítems individuales que componen el pedido.
 * @property platillos lista de platillos seleccionados con cantidad y precio.
 */
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

```kotlin
/**
 * Contrato del repositorio de pedidos para el módulo de cocina.
 *
 * Define las operaciones necesarias para observar los pedidos en tiempo
 * real, actualizar su estado y eliminarlos (cancelarlos), independientemente
 * de la fuente de datos concreta (Firebase u otra).
 */
interface PedidoRepository {

    /**
     * Expone la lista de pedidos como un flujo reactivo que emite una nueva
     * lista cada vez que hay cambios en la fuente de datos.
     *
     * @return [Flow] que emite la lista actualizada de [Pedido].
     */
    fun observarPedidos(): Flow<List<Pedido>>

    /**
     * Actualiza el estado de un pedido existente.
     *
     * @param id identificador del pedido a actualizar.
     * @param nuevoEstado nuevo [EstadoPedido] a aplicar.
     */
    suspend fun actualizarEstado(id: String, nuevoEstado: EstadoPedido)

    /**
     * Elimina (o marca como cancelado) un pedido.
     *
     * @param id identificador del pedido a eliminar.
     */
    suspend fun eliminarPedido(id: String)
}
```

---

## Domain / Usecase

### `domain/usecase/ObservarPedidosUseCase.kt`

```kotlin
/**
 * Caso de uso que expone el flujo de pedidos en tiempo real desde el
 * repositorio hacia el `ViewModel`, siguiendo el patrón de un único punto
 * de entrada por operación de negocio.
 *
 * @property repository repositorio del que se obtiene el flujo de pedidos.
 */
class ObservarPedidosUseCase(private val repository: PedidoRepository) {

    /**
     * Permite invocar el caso de uso como si fuera una función
     * (`observarPedidos()`), delegando en [PedidoRepository.observarPedidos].
     *
     * @return [Flow] con la lista de pedidos observados en tiempo real.
     */
    operator fun invoke(): Flow<List<Pedido>> = repository.observarPedidos()
}
```

### `domain/usecase/ActualizarPedidoUseCase.kt`

```kotlin
/**
 * Caso de uso que encapsula la acción de cambiar el estado de un pedido
 * (por ejemplo, de "en preparación" a "listo").
 *
 * @property repository repositorio sobre el que se aplica la actualización.
 */
class ActualizarEstadoPedidoUseCase(private val repository: PedidoRepository) {

    /**
     * Ejecuta el caso de uso, delegando en [PedidoRepository.actualizarEstado].
     *
     * @param id identificador del pedido a actualizar.
     * @param nuevoEstado nuevo estado a aplicar al pedido.
     */
    suspend operator fun invoke(id: String, nuevoEstado: EstadoPedido) {
        repository.actualizarEstado(id, nuevoEstado)
    }
}
```

### `domain/usecase/EliminarPedidoUseCase.kt`

```kotlin
/**
 * Caso de uso que encapsula la acción de eliminar/cancelar un pedido.
 *
 * @property repository repositorio sobre el que se aplica la eliminación.
 */
class EliminarPedidoUseCase(private val repository: PedidoRepository) {

    /**
     * Ejecuta el caso de uso, delegando en [PedidoRepository.eliminarPedido].
     *
     * @param id identificador del pedido a eliminar.
     */
    suspend operator fun invoke(id: String) {
        repository.eliminarPedido(id)
    }
}
```

---

## Data / Repository (implementación)

### `data/repository/PedidoRepositoryImpl.kt`

```kotlin
/**
 * Implementación de [PedidoRepository] que se conecta directamente al nodo
 * `pedidos` de Firebase Realtime Database.
 *
 * Escucha los cambios con un [ValueEventListener] envuelto en
 * [callbackFlow], parsea cada pedido (incluyendo su lista de ítems) de
 * forma defensiva (capturando errores de parseo por pedido individual) y
 * expone la lista actualizada como [Flow] en el dispatcher de IO. También
 * aplica las actualizaciones de estado (incluyendo el timestamp del
 * servidor cuando un pedido pasa a [EstadoPedido.LISTO]) y el marcado como
 * cancelado al eliminar un pedido.
 *
 * @property database referencia al nodo `pedidos` de Firebase Realtime
 * Database. Por defecto apunta a la instancia global de Firebase.
 */
class PedidoRepositoryImpl(
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("pedidos")
) : PedidoRepository {

    /**
     * Observa el nodo `pedidos` en tiempo real mediante un
     * [ValueEventListener], convirtiendo cada snapshot en una lista de
     * [Pedido] emitida a través de un [Flow] construido con [callbackFlow].
     *
     * Cada hijo del snapshot se parsea individualmente: si ocurre un error
     * al procesar un pedido puntual, se registra en el log y se omite ese
     * pedido, sin afectar al resto de la lista. Si el listener es cancelado
     * por Firebase (por ejemplo, por permisos), el flujo se cierra con la
     * excepción correspondiente. El listener se remueve automáticamente
     * cuando el [Flow] deja de recolectarse (`awaitClose`).
     *
     * @return [Flow] que emite la lista de [Pedido] cada vez que cambian
     * los datos en Firebase, ejecutado en [Dispatchers.IO].
     */
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

    /**
     * Actualiza el campo `estado` del pedido [id] en Firebase.
     *
     * Si el nuevo estado es [EstadoPedido.LISTO], además actualiza el campo
     * `timestamp` con [ServerValue.TIMESTAMP], registrando el momento
     * (según el reloj del servidor) en que el pedido quedó listo.
     *
     * @param id identificador del pedido a actualizar.
     * @param nuevoEstado nuevo estado a escribir en Firebase.
     */
    override suspend fun actualizarEstado(id: String, nuevoEstado: EstadoPedido) {
        database.child(id).child("estado").setValue(nuevoEstado.name)
        if (nuevoEstado == EstadoPedido.LISTO) {
            database.child(id).child("timestamp").setValue(ServerValue.TIMESTAMP)
        }
    }

    /**
     * "Elimina" un pedido estableciendo su campo `estado` como `"CANCELADO"`
     * en Firebase, en lugar de borrar el nodo físicamente. Esto conserva el
     * historial del pedido en la base de datos.
     *
     * @param id identificador del pedido a cancelar.
     */
    override suspend fun eliminarPedido(id: String) {
        database.child(id).child("estado").setValue("CANCELADO")
    }
}
```

## [Regresar al README principal](/README.md)