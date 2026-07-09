package mx.utng.carh.meserowatch.mobile.data.source

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import mx.utng.carh.meserowatch.mobile.domain.model.Usuario
import mx.utng.carh.meserowatch.mobile.domain.model.RolUsuario
import mx.utng.carh.meserowatch.mobile.domain.model.EstadoUsuario

class AuthDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("usuarios")

    suspend fun loginAsAdmin(): Usuario? {
        return Usuario(id = "admin", nombre = "Administrador", rol = RolUsuario.ADMIN)
    }

    suspend fun login(nombreUsuario: String): Usuario? {
        val snapshot = database.orderByChild("nombre")
            .equalTo(nombreUsuario.trim())
            .get()
            .await()
        if (snapshot.exists()) {
            val child = snapshot.children.first()
            return mapToUsuario(child)
        }
        return null
    }

    suspend fun register(usuario: Usuario) {
        val key = database.push().key ?: ""
        database.child(key).setValue(mapFromUsuario(usuario.copy(id = key))).await()
    }

    private fun mapToUsuario(snapshot: DataSnapshot): Usuario {
        return Usuario(
            id = snapshot.key ?: "",
            nombre = snapshot.child("nombre").value?.toString() ?: "",
            rol = try {
                RolUsuario.valueOf(snapshot.child("rol").value?.toString() ?: "MESERO")
            } catch (e: Exception) {
                RolUsuario.MESERO
            },
            activo = snapshot.child("activo").value as? Boolean ?: false,
            estadoUsuario = try {
                EstadoUsuario.valueOf(snapshot.child("estadoUsuario").value?.toString() ?: "ACTIVO")
            } catch (e: Exception) {
                EstadoUsuario.ACTIVO
            },
            zonaId = snapshot.child("zonaId").value?.toString() ?: "",
            zonaAsignada = snapshot.child("zonaAsignada").value?.toString() ?: "",
            fotoEmoji = snapshot.child("fotoEmoji").value?.toString() ?: "👤"
        )
    }

    private fun mapFromUsuario(usuario: Usuario): Map<String, Any?> {
        return mapOf(
            "id" to usuario.id,
            "nombre" to usuario.nombre,
            "rol" to usuario.rol.name,
            "activo" to usuario.activo,
            "estadoUsuario" to usuario.estadoUsuario.name,
            "zonaId" to usuario.zonaId,
            "zonaAsignada" to usuario.zonaAsignada,
            "fotoEmoji" to usuario.fotoEmoji
        )
    }
}