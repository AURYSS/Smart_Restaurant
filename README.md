# MeseroWatch 🍽️⌚

**Estudiantes:** Cecilia Aurora Robelo Hernández, Bryan Emilio Arredondo López, José Armando Ruano Mascorro  
**Grupo:** GIDS6093-E

## 🎯 Objetivo
Desarrollar un ecosistema digital integral que optimice la gestión operativa de un restaurante mediante la sincronización en tiempo real entre el personal de servicio (meseros), el área de producción (cocina) y la administración. El sistema utiliza dispositivos móviles, relojes inteligentes (Wear OS) y Android TV para reducir tiempos de espera y mejorar la experiencia del cliente.

## 📝 Descripción de las Funcionalidades

### 📱 Módulo Móvil (Admin & Mesero)
*   **Seguridad Avanzada:** Pantalla de registro y login con validación de requisitos de contraseña en tiempo real (mayúsculas, números y caracteres especiales).
*   **UX del Teclado:** Adaptación automática de la interfaz (`imePadding`) para evitar que el teclado oculte los campos de entrada.
*   **Control de Comandas:** Creación de pedidos con cálculo automático de totales y sincronización instantánea con la cocina.
*   **Dashboard Administrativo:** Panel con indicadores dinámicos de ventas y contador inteligente de ocupación de mesas (ej. 0/15).
*   **Gestión de Zonas (Diseño Moderno):** Clasificación de zonas en categorías A, B y C con tarjetas expandibles para visualizar el personal y edición mediante pulsación larga (Long-press).

### ⌚ Módulo Wear OS (Reloj Inteligente)
*   **Notificaciones de Sistema:** Alertas visuales y vibratorias inmediatas cuando un pedido está listo en cocina.
*   **Control Manos Libres (Sensores):** Integración con el giroscopio para marcar pedidos como "Entregado" mediante un giro de muñeca hacia arriba o posponerlos con un giro hacia abajo.
*   **Vibración Háptica:** Patrones de vibración diferenciados para alertas de cocina y confirmación de gestos.

### 📺 Módulo TV (Panel de Cocina)
*   **Monitor de Producción:** Visualización de pedidos entrantes con imágenes de platillos y notas especiales.
*   **Gestión de Estados:** Sincronización global que libera automáticamente la mesa en el móvil al completar o cancelar un pedido.

## 🛠️ Tecnologías Utilizadas
*   **Lenguaje:** Kotlin 2.2.10
*   **Interfaz de Usuario:** Jetpack Compose (Material 3) y Compose for Wear OS.
*   **Backend:** Firebase Realtime Database (Sincronización NoSQL en tiempo real).
*   **Sensores:** Giroscopio para detección de movimientos en Wear OS.
*   **Librerías Clave:** 
    *   `Coil`: Carga asíncrona de imágenes.
    *   `Navigation Compose`: Flujo entre pantallas.
    *   `Google Services`: Conexión con servicios de Firebase.

## 🚀 Instrucciones para Ejecutar el Proyecto

1.  **Descarga:** Clonar el repositorio en su equipo local.
2.  **Configuración de Firebase:** 
    *   Descargar el archivo `google-services.json` desde la consola de Firebase.
    *   Colocar una copia en las carpetas: `/app`, `/mobile` y `/tv`.
3.  **Preparación de Base de Datos:**
    *   Habilitar **Realtime Database** en Firebase.
    *   Configurar las reglas de seguridad como `".read": true` y `".write": true` para pruebas.
4.  **Ejecución:**
    *   Compilar y ejecutar el módulo `:mobile` en un smartphone Android.
    *   Ejecutar el módulo `:app` en un emulador de Wear OS (asegurarse de tener activado el giroscopio en los Virtual Sensors).
    *   Ejecutar el módulo `:tv` en un emulador de Android TV.

## 📦 Estructura del Proyecto

El ecosistema está dividido en tres módulos principales, utilizando una arquitectura de capas (Clean Architecture) para asegurar la separación de responsabilidades:

```text
MeseroWatch/
├── mobile/                      # Aplicación para teléfonos (Admin/Mesero)
│   └── src/main/java/.../mobile/
│       ├── data/                # Repositorios y fuentes de datos (Firebase)
│       ├── domain/              # Modelos de negocio e interfaces
│       └── presentation/        # Capa de interfaz de usuario y lógica
│           ├── di/              # Inyección de dependencias (AppModule)
│           ├── ui/              # Pantallas Compose (Login, Mesas, Zonas, etc.)
│           ├── viewmodel/       # Gestión de estados (ViewModels)
│           └── navigation/      # Rutas y flujo de navegación
├── app/                         # Aplicación para Wear OS (Reloj inteligente)
│   └── src/main/java/.../
│       └── presentation/        # UI de Wear OS y manejo de sensores
└── tv/                          # Aplicación para Android TV (Pantalla de cocina)
    └── src/main/java/.../tv/
        ├── data/                # Sincronización de pedidos de cocina
        └── presentation/        # Monitor de producción visual
```

## 📸 Capturas de Pantalla

> [!TIP]
> Puedes agregar tus capturas aquí usando el formato: `![Nombre](ruta_de_la_imagen.png)`

---
© 2026 MeseroWatch - Sistema de Gestión Inteligente.
