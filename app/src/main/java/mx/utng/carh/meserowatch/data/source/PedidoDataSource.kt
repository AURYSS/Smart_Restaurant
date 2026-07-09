package mx.utng.carh.meserowatch.data.source

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import mx.utng.carh.meserowatch.domain.model.EstadoPedido
import mx.utng.carh.meserowatch.domain.model.Pedido

class PedidoDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("pedidos")

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

    suspend fun confirmarEntrega(id: String) {
        database.child(id).child("estado").setValue(EstadoPedido.ENTREGADO.name).await()
    }

    suspend fun posponerPedido(id: String) {
        database.child(id).child("estado").setValue(EstadoPedido.EN_PREPARACION.name).await()
    }

    suspend fun completarEntrega(id: String) {
        database.child(id).child("estado").setValue("ENTREGADO").await()
    }
}