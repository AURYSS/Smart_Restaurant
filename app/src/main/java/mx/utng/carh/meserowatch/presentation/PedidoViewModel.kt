package mx.utng.carh.meserowatch.presentation

import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PedidoViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().getReference("pedidos")

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos.asStateFlow()

    val pedidoActual: Pedido?
        get() = _pedidos.value.firstOrNull { it.estado == EstadoPedido.LISTO }

    init {
        escucharPedidos()
    }

    private fun escucharPedidos() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Pedido>()
                for (postSnapshot in snapshot.children) {
                    val pedido = postSnapshot.getValue(Pedido::class.java)
                    if (pedido != null) {
                        lista.add(pedido.copy(id = postSnapshot.key ?: ""))
                    }
                }
                _pedidos.value = lista
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error si es necesario
            }
        })
    }

    fun confirmarEntrega(id: String) {
        // En un escenario real, podríamos eliminarlo o cambiar el estado a ENTREGADO
        database.child(id).removeValue()
    }

    fun posponerPedido(id: String) {
        // Posponer podría ser cambiar una prioridad o simplemente moverlo al final localmente
        // pero para persistencia en DB, podríamos cambiar un timestamp
        val pedido = _pedidos.value.find { it.id == id }
        pedido?.let {
            database.child(id).child("estado").setValue(EstadoPedido.EN_PREPARACION)
        }
    }
}