package mx.utng.carh.meserowatch.mobile.data.source

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import mx.utng.carh.meserowatch.mobile.domain.model.EstadoMesa
import mx.utng.carh.meserowatch.mobile.domain.model.Mesa

class MesaDataSource {
    private val mesasConfigRef = FirebaseDatabase.getInstance().getReference("mesas_config")
    private val pedidosRef = FirebaseDatabase.getInstance().getReference("pedidos")

    /**
     * Obtiene la configuración de mesas personalizada.
     */
    fun getMesasConfig(): Flow<List<Mesa>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mesas = mutableListOf<Mesa>()
                snapshot.children.forEach { child ->
                    val id = child.child("id").value.toString().toDoubleOrNull()?.toInt() ?: 0
                    if (id > 0) {
                        mesas.add(
                            Mesa(
                                id = id,
                                capacidad = child.child("capacidad").value.toString().toDoubleOrNull()?.toInt() ?: 4,
                                estado = EstadoMesa.LIBRE // se calculará después
                            )
                        )
                    }
                }
                trySend(mesas)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        mesasConfigRef.addValueEventListener(listener)
        awaitClose { mesasConfigRef.removeEventListener(listener) }
    }

    /**
     * Escucha los pedidos activos para saber qué mesas están ocupadas.
     */
    fun getMesasOcupadas(): Flow<Set<Int>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ocupadas = mutableSetOf<Int>()
                snapshot.children.forEach { child ->
                    val mesa = child.child("mesa").value.toString().toDoubleOrNull()?.toInt() ?: 0
                    val estado = child.child("estado").value?.toString() ?: ""
                    if (estado != "ENTREGADO" && estado != "CANCELADO") {
                        ocupadas.add(mesa)
                    }
                }
                trySend(ocupadas)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        pedidosRef.addValueEventListener(listener)
        awaitClose { pedidosRef.removeEventListener(listener) }
    }

    suspend fun addMesa(mesa: Mesa) {
        mesasConfigRef.child(mesa.id.toString()).setValue(
            mapOf("id" to mesa.id, "capacidad" to mesa.capacidad, "estado" to "LIBRE")
        ).await()
    }

    suspend fun updateMesa(mesa: Mesa) {
        mesasConfigRef.child(mesa.id.toString()).updateChildren(
            mapOf("capacidad" to mesa.capacidad)
        ).await()
    }

    suspend fun deleteMesa(id: Int) {
        mesasConfigRef.child(id.toString()).removeValue().await()
    }
}