# MeseroWatch 🍽️⌚

**Sistema Integral de Gestión Restaurantera Multiplataforma**

MeseroWatch es un ecosistema de aplicaciones diseñado para optimizar la comunicación y operación de restaurantes en tiempo real. El sistema integra tres plataformas sincronizadas mediante Firebase para ofrecer una experiencia fluida entre meseros, cocineros y administradores.

---

## 🚀 Funcionalidades Principales

### 📱 Módulo Móvil (Meseros y Administradores)
*   **Seguridad Avanzada:** Pantalla de Login y Registro con validación de requisitos de contraseña en tiempo real (longitud, números, mayúsculas y caracteres especiales).
*   **Dashboard Dinámico:** Contador inteligente de ocupación de mesas (ej. 0/15) que se actualiza automáticamente al agregar nuevas mesas.
*   **Gestión de Zonas (UX Moderna):** 
    *   Clasificación de zonas en categorías A, B y C.
    *   Vistas expandibles para visualizar el personal asignado a cada área.
    *   Edición y eliminación de zonas mediante pulsación larga (Long-press).
*   **Control de Pedidos:** Historial con visualización de montos totales y estados detallados (Cocina, Listo, Cancelado).

### ⌚ Módulo Wear OS (Reloj Inteligente)
*   **Notificaciones Instantáneas:** Alerta vibratoria y visual inmediata cuando un pedido es marcado como "Listo" en cocina.
*   **Control Manos Libres (Sensores):**
    *   **Giro Arriba:** Confirma la entrega del pedido a la mesa mediante el giroscopio.
    *   **Giro Abajo:** Pospone la alerta si el mesero está ocupado.
*   **Vibración Háptica:** Diferenciación entre alertas de nuevos pedidos y confirmación de gestos.

### 📺 Módulo TV (Cocina)
*   **Gestión de Comandas:** Visualización clara de pedidos entrantes con notas especiales e imágenes.
*   **Sincronización:** Botones para marcar pedidos como "Listos" o "Cancelados", liberando automáticamente las mesas en el sistema móvil.

---

## 🛠️ Stack Tecnológico
*   **Lenguaje:** Kotlin 2.0+
*   **UI Framework:** Jetpack Compose / Compose for Wear OS
*   **Backend:** Firebase Realtime Database
*   **Arquitectura:** MVVM (Model-View-ViewModel)
*   **Hardware:** Integración de Giroscopio y Motores de Vibración (Haptics)
*   **Librerías:** Coil (Imágenes), Navigation Compose, Google Services

---

## 🎮 Guía de Pruebas en Emulador (Wear OS)
Para simular los gestos del reloj sin un dispositivo físico:
1.  En el emulador, abre **Extended Controls (...)**.
2.  Ve a **Virtual Sensors** > **Device Pose**.
3.  Mueve rápidamente el eje **Z-Rot** o **Y-Rot**.
4.  Observa en el **Logcat** (filtro: `MeseroWatchWear`) cómo el sistema detecta el gesto y actualiza Firebase automáticamente.

---

## 📦 Estructura del Proyecto
*   `/mobile`: Aplicación para teléfonos Android (Admin/Mesero).
*   `/app`: Aplicación para Wear OS (Reloj inteligente).
*   `/tv`: Aplicación para Android TV (Pantalla de cocina).
