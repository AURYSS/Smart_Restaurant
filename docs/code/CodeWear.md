# Código del Módulo Wear

## [Regresar al README principal](/README.md)

Este documento describe el propósito de cada archivo del módulo `app` (Wear OS), organizado según la arquitectura por capas (Clean Architecture): `presentation`, `domain` y `data`.

---

## Presentation / Raíz

### `presentation/MainActivity.kt`
Actividad principal del reloj. Inicializa el `PedidoViewModel`, el detector de gestos de muñeca (`WristGestureDetector`), el canal de notificaciones y el manejo del permiso de notificaciones. Define el `NavHost` deslizable (`SwipeDismissableNavHost`) con las tres pantallas de la app (inicio, notificación, lista), dispara la vibración y navegación automática cuando llega un pedido en estado "LISTO", y conecta los gestos del giroscopio con las acciones de confirmar o posponer un pedido. También controla el ciclo de vida del sensor (`onResume`/`onPause`) y mantiene la pantalla encendida.

```kotlin
/**
 * Actividad principal de la aplicación Wear OS.
 *
 * Administra el ciclo de vida de la interfaz de usuario, la navegación basada en gestos de deslizamiento,
 * la detección de movimientos de muñeca mediante sensores, y la emisión de alertas hápticas y notificaciones.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: PedidoViewModel by viewModels()
    private val CHANNEL_ID = "meserowatch_notifications"
    private var gestureDetector: WristGestureDetector? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { Log.d("MeseroWatchWear", "Permiso notif: $it") }

    /**
     * Inicializa la actividad, configurando la persistencia de pantalla encendida,
     * los detectores de gestos del giroscopio, canales de notificación, permisos y la UI en Compose.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Inicialización de Sensor de Muñeca
        gestureDetector = WristGestureDetector(
            context = this,
            onGiroArriba = {
                val pedidoAConfirmar = viewModel.pedidos.value.firstOrNull { it.estado == EstadoPedido.LISTO }
                pedidoAConfirmar?.let {
                    Log.d("MeseroWatchWear", "Gesto detectado: Confirmando Mesa ${it.mesa} (ID: ${it.id})")
                    viewModel.confirmarEntrega(it.id)
                    vibrarConfirmacion()
                } ?: Log.d("MeseroWatchWear", "Gesto detectado pero no hay pedidos LISTOS")
            },
            onGiroAbajo = {
                val pedidoAPosponer = viewModel.pedidos.value.firstOrNull { it.estado == EstadoPedido.LISTO }
                pedidoAPosponer?.let {
                    Log.d("MeseroWatchWear", "Gesto detectado: Posponiendo Mesa ${it.mesa}")
                    viewModel.posponerPedido(it.id)
                    vibrarConfirmacion()
                }
            }
        )

        try {
            createNotificationChannel()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } catch (e: Exception) { Log.e("MeseroWatchWear", "Error init: ${e.message}") }

        setContent {
            MaterialTheme {
                val navController = rememberSwipeDismissableNavController()
                val pedidos by viewModel.pedidos.collectAsStateWithLifecycle()
                val listos = remember(pedidos) { pedidos.filter { it.estado == EstadoPedido.LISTO } }

                LaunchedEffect(listos.size) {
                    if (listos.isNotEmpty()) {
                        vibrarAlerta()
                        mostrarNotificacion(listos.last().mesa)
                        navController.navigate("notificacion") { launchSingleTop = true }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    timeText = { TimeText() }
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
                        SwipeDismissableNavHost(
                            navController = navController,
                            startDestination = "inicio"
                        ) {
                            composable("inicio") {
                                PantallaInicio(
                                    cantidadListos = listos.size,
                                    onVerLista = { navController.navigate("lista") }
                                )
                            }
                            composable("notificacion") {
                                val p = listos.firstOrNull()
                                if (p != null) {
                                    PantallaNotificacion(p) { navController.navigate("lista") }
                                }
                            }
                            composable("lista") {
                                PantallaLista(pedidos) { viewModel.confirmarEntrega(it) }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Ejecuta un patrón de vibración distintivo para alertar sobre la llegada de un pedido listo.
     */
    private fun vibrarAlerta() {
        try {
            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (v.hasVibrator()) {
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
            }
        } catch (_: Exception) {}
    }

    /**
     * Emite una vibración corta de retroalimentación tras confirmar o posponer una acción.
     */
    private fun vibrarConfirmacion() {
        try {
            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (v.hasVibrator()) {
                v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {}
    }

    /**
     * Muestra una notificación del sistema indicando el número de mesa que tiene su orden lista.
     *
     * @param mesa Número identificador de la mesa.
     */
    private fun mostrarNotificacion(mesa: Int) {
        try {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Mesa $mesa LISTA")
                .setContentText("Pedido listo para entrega")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
            manager.notify(mesa, builder.build())
        } catch (_: Exception) {}
    }

    /**
     * Crea el canal de notificaciones requerido para versiones de Android 8.0 (API 26) o superiores.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Avisos", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Reanuda la detección del sensor de giroscopio al volver a primer plano.
     */
    override fun onResume() {
        super.onResume()
        gestureDetector?.iniciar()
    }

    /**
     * Pausa y libera la escucha del sensor de giroscopio para optimizar la batería.
     */
    override fun onPause() {
        super.onPause()
        gestureDetector?.detener()
    }
}
```

---

## Presentation / Theme

### `presentation/theme/MeseroWatchTheme.kt`
Define el tema visual de la app envolviendo el contenido en el `MaterialTheme` de Wear Compose (Material 3), centralizando la apariencia general de las pantallas.

```kotlin
/**
 * Componente contenedor que aplica la configuración base de diseño y tipografía de Material Theme en Wear OS.
 *
 * @param content Elementos composables hijos a estilizar.
 */
@Composable
fun MeseroWatchTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
```

---

## Presentation / UI

### `presentation/ui/PantallaInicio.kt`
Pantalla de bienvenida/inicio del reloj. Muestra cuántos pedidos están en estado "LISTO" y da acceso, mediante un chip, a la pantalla de lista completa de pedidos.

```kotlin
/**
 * Pantalla principal que muestra el resumen de pedidos listos para servir.
 *
 * @param cantidadListos Número total de pedidos que se encuentran en estado [EstadoPedido.LISTO].
 * @param onVerLista Callback que se invoca cuando el usuario decide navegar al listado completo de pedidos.
 */
@Composable
fun PantallaInicio(
    cantidadListos: Int,
    onVerLista: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2C2C)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "MeseroWatch",
                color = Color.Cyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                if (cantidadListos > 0) "¡Tienes pedidos!" else "Sin pedidos",
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (cantidadListos > 0) Color.Green else Color.White
            )

            if (cantidadListos > 0) {
                Text(
                    "$cantidadListos mesas listas",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }

            Spacer(Modifier.height(12.dp))

            Chip(
                onClick = onVerLista,
                label = { Text("Ver Mesas", color = Color.White) },
                colors = ChipDefaults.chipColors(
                    backgroundColor = Color(0xFF444444)
                )
            )
        }
    }
}
```

### `presentation/ui/PantallaNotificacion.kt`
Pantalla de alerta que se muestra automáticamente cuando un pedido pasa a estado "LISTO". Presenta los datos del pedido (mesa, descripción) y un acceso directo para ver el listado completo.

```kotlin
/**
 * Pantalla de aviso emergente que resalta de forma inmediata la mesa que tiene un pedido listo.
 *
 * @param pedido Instancia del [Pedido] recién completado por cocina.
 * @param onVerLista Callback accionado al pulsar el botón para ver todos los pedidos.
 */
@Composable
fun PantallaNotificacion(
    pedido: Pedido,
    onVerLista: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2C2C)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text("🔔", fontSize = 32.sp)

            Spacer(Modifier.height(6.dp))

            Text(
                text = "MESA ${pedido.mesa}",
                color = Color(0xFFBB86FC),
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "¡LISTO AHORA!",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Chip(
                onClick = onVerLista,
                label = { Text("Ver Pedidos", fontSize = 12.sp) },
                colors = ChipDefaults.chipColors(
                    backgroundColor = Color(0xFF444444)
                )
            )
        }
    }
}
```

### `presentation/ui/PantallaLista.kt`
Pantalla de listado de pedidos activos. Usa una lista optimizada para reloj (`ScalingLazyColumn`) para mostrar todos los pedidos, agrupados/encabezados por estado, y permite confirmar la entrega de un pedido desde ahí.

```kotlin
/**
 * Pantalla que presenta la lista desplazable y adaptada a pantallas circulares con los pedidos listos.
 *
 * @param pedidos Colección total de pedidos obtenidos.
 * @param onConfirmar Callback que recibe el ID del pedido cuya entrega ha sido confirmada por el mesero.
 */
@Composable
fun PantallaLista(
    pedidos: List<Pedido>,
    onConfirmar: (String) -> Unit
) {
    val listos = pedidos.count { it.estado == EstadoPedido.LISTO }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 32.dp)
    ) {
        item {
            ListHeader {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "PEDIDOS LISTOS",
                        color = Color.Cyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "$listos mesas esperando",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        items(pedidos.filter { it.estado == EstadoPedido.LISTO }) { pedido ->
            TarjetaPedido(pedido = pedido, onConfirmar = onConfirmar)
        }
    }
}
```

### `presentation/ui/TarjetaPedido.kt`
Composable reutilizable que representa un pedido individual dentro de la lista, con formato de `Chip`: muestra la mesa y los datos resumidos del pedido, y ejecuta la confirmación de entrega al pulsarlo.

```kotlin
/**
 * Elemento gráfico interactivo (Chip) que representa un pedido individual dentro del listado.
 *
 * @param pedido Datos del [Pedido] a desplegar (mesa, descripción, etc.).
 * @param onConfirmar Callback invocado al hacer clic en el componente, enviando el ID del pedido.
 */
@Composable
fun TarjetaPedido(pedido: Pedido, onConfirmar: (String) -> Unit) {
    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = { onConfirmar(pedido.id) },
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Mesa ${pedido.mesa}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        pedido.descripcion.take(15) + "...",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
                Text(
                    "OK",
                    color = Color.Green,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = Color(0xFF1E3A1E)
        )
    )
}
```

---

## Presentation / ViewModel

### `presentation/viewmodel/PedidoViewModel.kt`
Maneja el estado de los pedidos en el reloj. Se conecta al repositorio, expone la lista de pedidos como `StateFlow` y el pedido actual en estado "LISTO", y ofrece las acciones de confirmar entrega, posponer o completar un pedido, delegando cada una al repositorio.

```kotlin
/**
 * ViewModel encargado de la lógica de presentación y sincronización del estado de pedidos en Wear OS.
 *
 * Gestiona el flujo reactivo de datos proveniente de [PedidoRepository] y expone métodos para alterar
 * los estados de los pedidos.
 */
class PedidoViewModel : ViewModel() {

    private val repository: PedidoRepository = PedidoRepositoryImpl(PedidoDataSource())

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    
    /**
     * Flujo observable con el listado actualizado de pedidos.
     */
    val pedidos: StateFlow<List<Pedido>> = _pedidos.asStateFlow()

    /**
     * Retorna el primer pedido que se encuentra listo para entrega, o `null` si no hay ninguno.
     */
    val pedidoActual: Pedido?
        get() = _pedidos.value.firstOrNull { it.estado == EstadoPedido.LISTO }

    init {
        viewModelScope.launch {
            repository.getPedidos().collect { lista ->
                _pedidos.value = lista
            }
        }
    }

    /**
     * Marca un pedido como entregado.
     *
     * @param id Identificador único del pedido a confirmar.
     */
    fun confirmarEntrega(id: String) {
        viewModelScope.launch {
            repository.confirmarEntrega(id)
        }
    }

    /**
     * Devuelve el pedido al estado de preparación.
     *
     * @param id Identificador único del pedido a posponer.
     */
    fun posponerPedido(id: String) {
        viewModelScope.launch {
            repository.posponerPedido(id)
        }
    }

    /**
     * Finaliza completamente el flujo de entrega de un pedido.
     *
     * @param id Identificador único del pedido a completar.
     */
    fun completarEntrega(id: String) {
        viewModelScope.launch {
            repository.completarEntrega(id)
        }
    }
}
```

---

## Presentation / Utils

### `presentation/utils/WristGestureDetector.kt`
Detector de gestos de muñeca basado en el giroscopio del reloj. Acumula el ángulo de giro en el eje correspondiente y, al superar un umbral definido, dispara el callback de "giro hacia arriba" (confirmar entrega) o "giro hacia abajo" (posponer pedido). Expone funciones para iniciar y detener la escucha del sensor.

```kotlin
/**
 * Detector de gestos físicos de muñeca basado en las lecturas angulares del giroscopio del reloj.
 *
 * @param context Contexto de la aplicación para obtener el servicio de sensores.
 * @param onGiroArriba Callback ejecutado al detectar una rotación rápida hacia arriba.
 * @param onGiroAbajo Callback ejecutado al detectar una rotación rápida hacia abajo.
 */
class WristGestureDetector(
    context: Context,
    private val onGiroArriba: () -> Unit,
    private val onGiroAbajo: () -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val giroscopio: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var anguloAcumulado = 0f
    private val UMBRAL_GIRO = 2.5f
    private var ultimoTimestamp = 0L

    /**
     * Registra el listener en el [SensorManager] para comenzar a capturar datos del giroscopio.
     */
    fun iniciar() {
        giroscopio?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    /**
     * Da de baja el listener del sensor y reinicia los acumuladores internos de rotación.
     */
    fun detener() {
        sensorManager.unregisterListener(this)
        anguloAcumulado = 0f
        ultimoTimestamp = 0L
    }

    /**
     * Procesa los eventos angulares generados por el sensor e integra la velocidad para calcular el desplazamiento.
     *
     * @param event Evento con las mediciones actuales del sensor.
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_GYROSCOPE) return

        if (ultimoTimestamp == 0L) {
            ultimoTimestamp = event.timestamp
            return
        }
        val dt = (event.timestamp - ultimoTimestamp) / 1_000_000_000f
        ultimoTimestamp = event.timestamp

        anguloAcumulado += event.values[2] * dt

        when {
            anguloAcumulado > UMBRAL_GIRO -> {
                onGiroArriba()
                anguloAcumulado = 0f
            }
            anguloAcumulado < -UMBRAL_GIRO -> {
                onGiroAbajo()
                anguloAcumulado = 0f
            }
        }
    }

    /**
     * Maneja variaciones en la precisión del sensor.
     *
     * @param sensor Sensor evaluado.
     * @param accuracy Nuevo valor de precisión.
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
```

---

## Domain / Model

### `domain/model/EstadoPedido.kt`
Enum que define los posibles estados de un pedido: pendiente, en preparación, listo, entregado y cancelado.

```kotlin
/**
 * Representa los diferentes estados por los que puede transitar un pedido en el sistema.
 */
enum class EstadoPedido {
    /** Pedido registrado en espera de ser atendido por cocina. */
    PENDIENTE,
    /** Pedido actualmente en proceso de elaboración. */
    EN_PREPARACION,
    /** Pedido cocinado y listo para ser entregado a la mesa. */
    LISTO,
    /** Pedido servido exitosamente en la mesa. */
    ENTREGADO,
    /** Pedido cancelado o descartado. */
    CANCELADO
}
```

### `domain/model/Pedido.kt`
Entidad que representa un pedido dentro del contexto del reloj: id, mesa, descripción, nota, estado y timestamp.

```kotlin
/**
 * Entidad de dominio que modela la información de un pedido en el reloj inteligente.
 *
 * @property id Identificador único del pedido en la base de datos.
 * @property mesa Número de la mesa receptora del pedido.
 * @property descripcion Detalle de los platillos y bebidas solicitados.
 * @property nota Indicaciones adicionales o especificaciones del cliente.
 * @property estado Estado actual del ciclo de vida del pedido.
 * @property timestamp Marca de tiempo en milisegundos correspondiente a su registro.
 */
data class Pedido(
    val id: String = "",
    val mesa: Int = 0,
    val descripcion: String = "",
    val nota: String = "",
    val estado: EstadoPedido = EstadoPedido.PENDIENTE,
    val timestamp: Long = 0
)
```

---

## Domain / Repository (interfaz)

### `domain/repository/PedidoRepository.kt`
Contrato del repositorio de pedidos para Wear OS: observar los pedidos en tiempo real y las acciones de confirmar entrega, posponer o completar un pedido.

```kotlin
/**
 * Contrato de operaciones para la gestión y sincronización de pedidos en la capa de dominio.
 */
interface PedidoRepository {
    /**
     * Obtiene un flujo reactivo continuo con la lista de pedidos en tiempo real.
     *
     * @return [Flow] que emite la lista actualizada de objetos [Pedido].
     */
    fun getPedidos(): Flow<List<Pedido>>

    /**
     * Actualiza el estado del pedido a entregado.
     *
     * @param id Identificador único del pedido.
     */
    suspend fun confirmarEntrega(id: String)

    /**
     * Cambia el estado del pedido de regreso a preparación.
     *
     * @param id Identificador único del pedido.
     */
    suspend fun posponerPedido(id: String)

    /**
     * Concluye definitivamente la entrega del pedido en el sistema.
     *
     * @param id Identificador único del pedido.
     */
    suspend fun completarEntrega(id: String)
}
```

---

## Data / Source

### `data/source/PedidoDataSource.kt`
Acceso directo a Firebase Realtime Database (nodo `pedidos`). Escucha los cambios en tiempo real con un `ValueEventListener` envuelto en `callbackFlow`, parsea cada pedido y expone la lista actualizada como `Flow`. Implementa además las escrituras de cambio de estado: confirmar entrega, posponer (regresar a "en preparación") y completar entrega.

```kotlin
/**
 * Fuente de datos remota encargada de la comunicación directa con Firebase Realtime Database.
 */
class PedidoDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("pedidos")

    /**
     * Escucha activamente el nodo de pedidos en Firebase y emite las actualizaciones.
     *
     * @return [Flow] reactivo que emite listas actualizadas de [Pedido].
     */
    fun observePedidos(): Flow<List<Pedido>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pedidos = mutableListOf<Pedido>()
                snapshot.children.forEach { child ->
                    try {
                        val id = child.key ?: ""
                        val mesa = child.child("mesa").value.toString().toDoubleOrNull()?.toInt() ?: 0
                        val descripcion = child.child("descripcion").value?.toString() ?: ""
                        val nota = child.child("nota").value?.toString() ?: ""
                        val estadoStr = child.child("estado").value?.toString() ?: "PENDIENTE"
                        val timestamp = child.child("timestamp").value.toString().toLongOrNull() ?: 0L

                        pedidos.add(
                            Pedido(
                                id = id,
                                mesa = mesa,
                                descripcion = descripcion,
                                nota = nota,
                                estado = try { EstadoPedido.valueOf(estadoStr) } catch(e: Exception) { EstadoPedido.PENDIENTE },
                                timestamp = timestamp
                            )
                        )
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
     * Modifica el estado del nodo remoto correspondiente al ID a [EstadoPedido.ENTREGADO].
     *
     * @param id Identificador único del pedido en la base de datos.
     */
    suspend fun confirmarEntrega(id: String) {
        database.child(id).child("estado").setValue(EstadoPedido.ENTREGADO.name).await()
    }

    /**
     * Modifica el estado del nodo remoto correspondiente al ID a [EstadoPedido.EN_PREPARACION].
     *
     * @param id Identificador único del pedido en la base de datos.
     */
    suspend fun posponerPedido(id: String) {
        database.child(id).child("estado").setValue(EstadoPedido.EN_PREPARACION.name).await()
    }

    /**
     * Actualiza el valor del estado a "ENTREGADO" en la base de datos de Firebase.
     *
     * @param id Identificador único del pedido en la base de datos.
     */
    suspend fun completarEntrega(id: String) {
        database.child(id).child("estado").setValue("ENTREGADO").await()
    }
}
```

### `data/repository/PedidoRepositoryImpl.kt`
Implementa `PedidoRepository` delegando todas las operaciones a `PedidoDataSource`, actuando como puente entre el dominio y Firebase.

```kotlin
/**
 * Implementación concreta de [PedidoRepository] que canaliza las operaciones hacia la fuente de datos [PedidoDataSource].
 *
 * @property dataSource Instancia de la fuente de datos remota.
 */
class PedidoRepositoryImpl(private val dataSource: PedidoDataSource) : PedidoRepository {

    /**
     * Obtiene el flujo de pedidos suscribiéndose a los cambios del data source.
     *
     * @return [Flow] continuo con la lista de pedidos.
     */
    override fun getPedidos(): Flow<List<Pedido>> = dataSource.observePedidos()

    /**
     * Confirma la entrega delegando la actualización al data source.
     *
     * @param id Identificador del pedido.
     */
    override suspend fun confirmarEntrega(id: String) {
        dataSource.confirmarEntrega(id)
    }

    /**
     * Pospone el pedido delegando el retroceso de estado al data source.
     *
     * @param id Identificador del pedido.
     */
    override suspend fun posponerPedido(id: String) {
        dataSource.posponerPedido(id)
    }

    /**
     * Completa la entrega delegando el cambio al data source.
     *
     * @param id Identificador del pedido.
     */
    override suspend fun completarEntrega(id: String) {
        dataSource.completarEntrega(id)
    }
}
```


## [Regresar al README principal](/README.md)
