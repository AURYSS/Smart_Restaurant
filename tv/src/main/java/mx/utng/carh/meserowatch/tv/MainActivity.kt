package mx.utng.carh.meserowatch.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.MaterialTheme
import mx.utng.carh.meserowatch.tv.data.repository.PedidoRepositoryImpl
import mx.utng.carh.meserowatch.tv.domain.usecase.ActualizarEstadoPedidoUseCase
import mx.utng.carh.meserowatch.tv.domain.usecase.EliminarPedidoUseCase
import mx.utng.carh.meserowatch.tv.domain.usecase.ObservarPedidosUseCase
import mx.utng.carh.meserowatch.tv.presentation.kitchen.KitchenScreen
import mx.utng.carh.meserowatch.tv.presentation.kitchen.KitchenViewModel

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