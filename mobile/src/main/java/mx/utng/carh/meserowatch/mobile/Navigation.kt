package mx.utng.carh.meserowatch.mobile

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Login : Screen("login", "Login", Icons.Default.Lock)
    object InicioAdmin : Screen("inicio_admin", "Inicio", Icons.Default.Home)
    object Turnos : Screen("turnos", "Turnos", Icons.Default.Schedule)
    object Historial : Screen("historial", "Pedidos", Icons.Default.History)
    object Nuevo : Screen("nuevo", "Nuevo", Icons.Default.Add)
    object MisPedidos : Screen("mis_pedidos", "Mis pedidos", Icons.Default.Checklist)
    object Mesas : Screen("mesas", "Mesas", Icons.Default.TableBar)
    object Alertas : Screen("alertas", "Alertas", Icons.Default.Notifications)
    object Menu : Screen("menu", "Menú", Icons.Default.RestaurantMenu)
    object Personal : Screen("personal", "Usuarios", Icons.Default.Group)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Definir qué pestañas ve cada quién (Basado en imagen Admin)
    val adminItems = listOf(Screen.InicioAdmin, Screen.Turnos, Screen.Historial, Screen.Personal)
    val userItems = listOf(Screen.Nuevo, Screen.Alertas, Screen.Menu)
    
    val currentItems = if (SessionManager.isAdmin) adminItems else userItems

    Scaffold(
        topBar = {
            if (currentRoute != Screen.Login.route) {
                TopAppBar(
                    title = { 
                        Text(
                            if(SessionManager.isAdmin) "Panel de administrador" else "MeseroWatch", 
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    actions = {
                        IconButton(onClick = {
                            SessionManager.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
                )
            }
        },
        bottomBar = {
            if (currentRoute != Screen.Login.route) {
                NavigationBar(
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color.White
                ) {
                    currentItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title, color = Color.Gray) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color.Gray,
                                indicatorColor = Color(0xFF3B82F6).copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) { 
                LoginScreen(onLoginSuccess = {
                    val startRoute = if (SessionManager.isAdmin) Screen.InicioAdmin.route else Screen.Nuevo.route
                    navController.navigate(startRoute) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }) 
            }
            composable(Screen.InicioAdmin.route) { 
                AdminDashboardScreen(
                    onNavigateTo = { route -> navController.navigate(route) }
                ) 
            }
            composable(Screen.Turnos.route) { TurnosPersonalScreen() }
            composable(Screen.Historial.route) { HistorialPedidosScreen() }
            composable(Screen.Nuevo.route) { 
                NuevoPedidoScreen(onNavigateToAlertas = {
                    navController.navigate(Screen.Alertas.route) {
                        popUpTo(Screen.Nuevo.route) { inclusive = false }
                    }
                }) 
            }
            composable(Screen.Alertas.route) { AlertasScreen() }
            composable(Screen.MisPedidos.route) { Text("Mis Pedidos Screen", color = Color.White) }
            composable(Screen.Mesas.route) { EstadoMesasScreen() }
            composable(Screen.Menu.route) { MenuAdminScreen() }
            composable(Screen.Personal.route) { PersonalAdminScreen() }
        }
    }
}
