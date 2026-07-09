package mx.utng.carh.meserowatch.mobile.data.source

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import mx.utng.carh.meserowatch.mobile.domain.model.EstadoPedido
import mx.utng.carh.meserowatch.mobile.domain.model.Pedido
import mx.utng.carh.meserowatch.mobile.domain.model.PlatilloSeleccionado

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