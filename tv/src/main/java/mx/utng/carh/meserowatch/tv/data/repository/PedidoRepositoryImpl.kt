package mx.utng.carh.meserowatch.tv.data.repository

import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import mx.utng.carh.meserowatch.tv.domain.model.EstadoPedido
import mx.utng.carh.meserowatch.tv.domain.model.ItemPedido
import mx.utng.carh.meserowatch.tv.domain.model.Pedido
import mx.utng.carh.meserowatch.tv.domain.repository.PedidoRepository

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