package mx.utng.carh.meserowatch.presentation

import android.util.Log
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
        Log.d("MeseroWatchWear", "Iniciando escucha de pedidos en Firebase...")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("MeseroWatchWear", "Firebase: onDataChange disparado. Niños: ${snapshot.childrenCount}")
                val lista = mutableListOf<Pedido>()
                for (postSnapshot in snapshot.children) {
                    try {
                        val id = postSnapshot.key ?: ""
                        val mesa = postSnapshot.child("mesa").value?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                        val desc = postSnapshot.child("descripcion").value?.toString() ?: ""
                        val nota = postSnapshot.child("nota").value?.toString() ?: ""
                        val estadoStr = postSnapshot.child("estado").value?.toString() ?: "PENDIENTE"
                        val time = postSnapshot.child("timestamp").value?.toString()?.toLongOrNull() ?: 0L

                        Log.d("MeseroWatchWear", "Procesando Mesa $mesa - Estado: $estadoStr")

                        lista.add(Pedido(
                            id = id,
                            mesa = mesa,
                            descripcion = desc,
                            nota = nota,
                            estado = try { EstadoPedido.valueOf(estadoStr) } catch (e: Exception) { 
                                Log.e("MeseroWatchWear", "Error en Enum EstadoPedido para $estadoStr")
                                EstadoPedido.PENDIENTE 
                            },
                            timestamp = time
                        ))
                    } catch (e: Exception) {
                        Log.e("MeseroWatchWear", "Error al parsear pedido individual: ${e.message}")
                    }
                }
                Log.d("MeseroWatchWear", "Lista total procesada: ${lista.size} pedidos. Listos: ${lista.count { it.estado == EstadoPedido.LISTO }}")
                _pedidos.value = lista
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MeseroWatchWear", "Error en Firebase: ${error.message}")
            }
        })
    }

    fun confirmarEntrega(id: String) {
        database.child(id).child("estado").setValue(EstadoPedido.ENTREGADO.name)
    }

    fun posponerPedido(id: String) {
        database.child(id).child("estado").setValue(EstadoPedido.EN_PREPARACION.name)
    }

    fun completarEntrega(id: String) {
        database.child(id).child("estado").setValue("ENTREGADO")
    }
}