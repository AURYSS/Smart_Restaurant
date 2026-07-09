package mx.utng.carh.meserowatch.mobile.data.source

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import mx.utng.carh.meserowatch.mobile.domain.model.EstadoUsuario
import mx.utng.carh.meserowatch.mobile.domain.model.RolUsuario
import mx.utng.carh.meserowatch.mobile.domain.model.Usuario

class UsuarioDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("usuarios")

    fun getUsuarios(): Flow<List<Usuario>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuarios = mutableListOf<Usuario>()
                snapshot.children.forEach { child ->
                    try {
                        usuarios.add(
                            Usuario(
                                id = child.key ?: "",
                                nombre = child.child("nombre").value?.toString() ?: "",
                                rol = try { RolUsuario.valueOf(child.child("rol").value?.toString() ?: "MESERO") } catch(e: Exception) { RolUsuario.MESERO },
                                activo = child.child("activo").value as? Boolean ?: false,
                                estadoUsuario = try { EstadoUsuario.valueOf(child.child("estadoUsuario").value?.toString() ?: "ACTIVO") } catch(e: Exception) { EstadoUsuario.ACTIVO },
                                zonaId = child.child("zonaId").value?.toString() ?: "",
                                zonaAsignada = child.child("zonaAsignada").value?.toString() ?: "",
                                fotoEmoji = child.child("fotoEmoji").value?.toString() ?: "👤"
                            )
                        )
                    } catch (_: Exception) {}
                }
                trySend(usuarios)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    suspend fun addUsuario(usuario: Usuario) {
        val key = database.push().key ?: ""
        database.child(key).setValue(mapFromUsuario(usuario.copy(id = key))).await()
    }

    suspend fun updateUsuario(usuario: Usuario) {
        database.child(usuario.id).updateChildren(mapFromUsuario(usuario)).await()
    }

    suspend fun deleteUsuario(id: String) {
        database.child(id).removeValue().await()
    }

    private fun mapFromUsuario(u: Usuario): Map<String, Any?> = mapOf(
        "id" to u.id,
        "nombre" to u.nombre,
        "rol" to u.rol.name,
        "activo" to u.activo,
        "estadoUsuario" to u.estadoUsuario.name,
        "zonaId" to u.zonaId,
        "zonaAsignada" to u.zonaAsignada,
        "fotoEmoji" to u.fotoEmoji
    )
}