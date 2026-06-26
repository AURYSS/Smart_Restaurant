package mx.utng.carh.meserowatch.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.firebase.database.DatabaseError

class MainActivity : ComponentActivity() {

    private val viewModel: PedidoViewModel by viewModels()
    private var gestureDetector: WristGestureDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gestureDetector = WristGestureDetector(
            context = this,
            onGiroArriba = {
                viewModel.pedidoActual?.let { viewModel.confirmarEntrega(it.id) }
            },
            onGiroAbajo = {
                viewModel.pedidoActual?.let { viewModel.posponerPedido(it.id) }
            }
        )

        setContent {
            val navController = rememberSwipeDismissableNavController()
            val pedidos by viewModel.pedidos.collectAsStateWithLifecycle()

            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "notificacion"
            ) {
                composable("notificacion") {
                    val pedidoActual = pedidos.firstOrNull { it.estado == EstadoPedido.LISTO }
                    if (pedidoActual != null) {
                        PantallaNotificacion(
                            pedido = pedidoActual,
                            onVerLista = { navController.navigate("lista") }
                        )
                    }
                }

                composable("lista") {
                    PantallaLista(
                        pedidos = pedidos,
                        onConfirmar = { id -> viewModel.confirmarEntrega(id) }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gestureDetector?.iniciar()
    }

    override fun onPause() {
        super.onPause()
        gestureDetector?.detener()
    }
}