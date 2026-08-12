# MeseroWatch 🍽️⌚

**Estudiantes:**
* **Cecilia Aurora Robelo Hernández** 
* **Bryan Emilio Arredondo López** 
* **José Armando Ruano Mascorro**
* **Grupo:** GIDS6093-E

## 🎯 Objetivo
Desarrollar un ecosistema digital integral que optimice la gestión operativa de un restaurante mediante la sincronización en tiempo real entre el personal de servicio (meseros), el área de producción (cocina) y la administración. El sistema utiliza dispositivos móviles, relojes inteligentes (Wear OS) y Android TV para reducir tiempos de espera y mejorar la experiencia del cliente.

## 📝 Descripción de las Funcionalidades

### 📱 [Módulo Móvil (Admin & Mesero)](docs/MobileFlow.md)
*   **Seguridad Avanzada:** Pantalla de registro y login con validación de requisitos de contraseña en tiempo real (mayúsculas, números y caracteres especiales).
*   **UX del Teclado:** Adaptación automática de la interfaz (`imePadding`) para evitar que el teclado oculte los campos de entrada.
*   **Control de Comandas:** Creación de pedidos con cálculo automático de totales y sincronización instantánea con la cocina.
*   **Dashboard Administrativo:** Panel con indicadores dinámicos de ventas y contador inteligente de ocupación de mesas (ej. 0/15).
*   **Gestión de Zonas (Diseño Moderno):** Clasificación de zonas en categorías A, B y C con tarjetas expandibles para visualizar el personal y edición mediante pulsación larga (Long-press).

#### [Código del módulo del móvil](docs/code/CodeMobile.md)


### [⌚ Módulo Wear OS (Reloj Inteligente)](docs/WearFlow.md)
*   **Notificaciones de Sistema:** Alertas visuales y vibratorias inmediatas cuando un pedido está listo en cocina.
*   **Control Manos Libres (Sensores):** Integración con el giroscopio para marcar pedidos como "Entregado" mediante un giro de muñeca hacia arriba o posponerlos con un giro hacia abajo.
*   **Vibración Háptica:** Patrones de vibración diferenciados para alertas de cocina y confirmación de gestos.

#### [Código del módulo del reloj](docs/code/CodeWear.md)


### [📺 Módulo TV (Panel de Cocina)](docs/TvFlow.md)
*   **Monitor de Producción:** Visualización de pedidos entrantes con imágenes de platillos y notas especiales.
*   **Gestión de Estados:** Sincronización global que libera automáticamente la mesa en el móvil al completar o cancelar un pedido.

#### [Código del módulo de tv](docs/code/CodeTv.md)


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
├── docs/                        # Documentación técnica del sistema
│   ├── MobileFlow.md            # Diagramas y flujos de la App Móvil
│   ├── WearFlow.md              # Lógica de gestos y sensores (Watch)
│   ├── TvFlow.md                # Proceso de monitorización en Cocina
│   └── images/                  # Activos visuales de documentación
├── mobile/                      # Módulo Smartphone (Admin/Mesero)
│   └── src/main/java/.../mobile/
│       ├── data/                # Persistencia y red
│       │   ├── repository/      # Implementación de repositorios
│       │   └── source/          # Fuentes de datos (Firebase Realtime DB)
│       ├── domain/              # Lógica de negocio pura
│       │   ├── model/           # Entidades (Pedido, Mesa, Usuario, Zona)
│       │   └── repository/      # Interfaces de abstracción
│       └── presentation/        # UI y Controladores
│           ├── di/              # Inyección de dependencias (AppModule)
│           ├── ui/              # Pantallas Compose por característica
│           │   ├── admin/       # Dashboard y panel de control
│           │   ├── zonas/       # Gestión de áreas (Zonas A, B, C)
│           │   ├── personal/    # Administración de empleados
│           │   ├── mesas/       # Control de ocupación en tiempo real
│           │   ├── login/       # Seguridad y autenticación
│           │   └── nuevopedido/ # Flujo de creación de comandas
│           ├── viewmodel/       # ViewModels para gestión de estado
│           └── navigation/      # Grafo de navegación centralizado
├── app/                         # Módulo Wear OS (Reloj inteligente)
│   └── src/main/java/.../
│       ├── data/                # Persistencia de alertas de pedidos
│       │   ├── repository/      # Repositorios de Wear OS
│       │   └── source/          # Conexión con Firebase Realtime DB
│       ├── domain/              # Casos de uso y modelos del reloj
│       │   ├── model/           # Entidades (Pedido, EstadoPedido)
│       │   └── repository/      # Interfaces de datos
│       └── presentation/        # Interfaz y procesamiento de sensores
│           ├── ui/              # Componentes Wear Compose
│           │   ├── PantallaInicio.kt       # Pantalla de bienvenida
│           │   ├── PantallaLista.kt        # Listado de pedidos activos
│           │   └── PantallaNotificacion.kt # Alerta visual de pedido listo
│           ├── viewmodel/       # Gestión de estado del reloj
│           ├── theme/           # Tipografías y colores de Wear OS
│           └── utils/           # WristGestureDetector (Giroscopio)
└── tv/                          # Módulo Android TV (Monitor de cocina)
    └── src/main/java/.../tv/
        ├── data/                # Persistencia y sincronización
        │   └── repository/      # Implementación de repositorio de cocina
        ├── domain/              # Lógica de producción
        │   ├── model/           # Modelo de Pedido de cocina
        │   └── usecase/         # Casos de uso (Observar, Actualizar, Eliminar)
        └── presentation/        # Interfaz de usuario para TV
            └── kitchen/         # Flujo principal de monitorización
                ├── components/  # Tarjetas y elementos visuales
                ├── KitchenScreen.kt      # Pantalla principal (Composables)
                └── KitchenViewScreen.kt  # Lógica de estado y KitchenViewModel
```
---
© 2026 MeseroWatch - Sistema de Gestión Inteligente.
