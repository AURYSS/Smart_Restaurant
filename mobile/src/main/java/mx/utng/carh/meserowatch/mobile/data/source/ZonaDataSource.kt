package mx.utng.carh.meserowatch.mobile.data.source

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import mx.utng.carh.meserowatch.mobile.domain.model.EstadoZona
import mx.utng.carh.meserowatch.mobile.domain.model.Zona

class ZonaDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("zonas")

    fun getZonas(): Flow<List<Zona>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val zonas = mutableListOf<Zona>()
                snapshot.children.forEach { child ->
                    zonas.add(
                        Zona(
                            id = child.key ?: "",
                            nombreZona = child.child("nombreZona").value?.toString() ?: "",
                            estadoZona = try {
                                EstadoZona.valueOf(child.child("estadoZona").value?.toString() ?: "DISPONIBLE")
                            } catch (e: Exception) {
                                EstadoZona.DISPONIBLE
                            }
                        )
                    )
                }
                trySend(zonas)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    suspend fun addZona(zona: Zona) {
        val key = database.push().key ?: ""
        database.child(key).setValue(
            mapOf(
                "id" to key,
                "nombreZona" to zona.nombreZona,
                "estadoZona" to zona.estadoZona.name
            )
        ).await()
    }

    suspend fun updateZona(zona: Zona) {
        database.child(zona.id).updateChildren(
            mapOf(
                "nombreZona" to zona.nombreZona,
                "estadoZona" to zona.estadoZona.name
            )
        ).await()
    }

    suspend fun deleteZona(id: String) {
        database.child(id).removeValue().await()
    }
}
