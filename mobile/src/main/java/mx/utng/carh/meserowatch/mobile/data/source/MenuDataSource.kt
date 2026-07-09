package mx.utng.carh.meserowatch.mobile.data.source

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import mx.utng.carh.meserowatch.mobile.domain.model.Platillo

class MenuDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("menu")

    fun getMenu(): Flow<List<Platillo>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val menu = mutableListOf<Platillo>()
                snapshot.children.forEach { child ->
                    try {
                        val p = Platillo(
                            id = child.key ?: "",
                            nombre = child.child("nombre").value?.toString() ?: "",
                            precio = child.child("precio").value.toString().toDoubleOrNull() ?: 0.0,
                            categoria = child.child("categoria").value?.toString() ?: "Platos",
                            disponible = child.child("disponible").value as? Boolean ?: true,
                            emoji = child.child("emoji").value?.toString() ?: "🍽️"
                        )
                        menu.add(p)
                    } catch (_: Exception) {}
                }
                trySend(menu)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    suspend fun addPlatillo(platillo: Platillo) {
        val key = database.push().key ?: ""
        database.child(key).setValue(mapPlatillo(platillo.copy(id = key))).await()
    }

    suspend fun updatePlatillo(platillo: Platillo) {
        database.child(platillo.id).updateChildren(mapPlatillo(platillo)).await()
    }

    suspend fun deletePlatillo(id: String) {
        database.child(id).removeValue().await()
    }

    private fun mapPlatillo(p: Platillo): Map<String, Any?> = mapOf(
        "id" to p.id,
        "nombre" to p.nombre,
        "precio" to p.precio,
        "categoria" to p.categoria,
        "disponible" to p.disponible,
        "emoji" to p.emoji
    )
}