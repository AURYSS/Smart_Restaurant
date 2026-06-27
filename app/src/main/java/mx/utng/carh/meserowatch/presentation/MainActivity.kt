package mx.utng.carh.meserowatch.presentation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController

class MainActivity : ComponentActivity() {

    private val viewModel: PedidoViewModel by viewModels()
    private val CHANNEL_ID = "meserowatch_notifications"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { Log.d("MeseroWatchWear", "Permiso notif: $it") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
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
                        vibrarApp()
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

    private fun vibrarApp() {
        try {
            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (v.hasVibrator()) {
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
            }
        } catch (e: Exception) { }
    }

    private fun mostrarNotificacion(mesa: Int) {
        try {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Mesa $mesa LISTA")
                .setContentText("Pedido listo")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
            manager.notify(mesa, builder.build())
        } catch (e: Exception) { }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Avisos", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
