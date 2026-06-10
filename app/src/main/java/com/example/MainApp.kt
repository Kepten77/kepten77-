package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.food.FoodInputScreen
import com.example.ui.insulin.InsulinScreen
import com.example.ui.football.FootballScreen
import com.example.ui.settings.SettingsScreen
import com.example.viewmodel.DashboardViewModel
import com.example.viewmodel.MainViewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(mainViewModel: MainViewModel, dashboardViewModel: DashboardViewModel) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { AppBottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") { DashboardScreen(mainViewModel, dashboardViewModel) }
            composable("food") { FoodInputScreen(mainViewModel) }
            composable("insulin") { InsulinScreen(mainViewModel) }
            composable("football") { FootballScreen(mainViewModel) }
            composable("settings") { SettingsScreen(mainViewModel) }
        }
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem("dashboard", "ГЛАВНАЯ", Icons.Filled.Home),
        NavigationItem("food", "СОБЫТИЯ", Icons.AutoMirrored.Filled.List),
        NavigationItem("insulin", "ИНСУЛИНЫ", Icons.Filled.Info),
        NavigationItem("football", "ФУТБОЛ", Icons.Filled.Star),
        NavigationItem("settings", "НАСТРОЙКИ", Icons.Filled.Settings)
    )
    
    NavigationBar(
        containerColor = Color(0xFF212121),
        tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = { 
                    Icon(
                        item.icon, 
                        contentDescription = item.title,
                        tint = if (isSelected) Color(0xFFFFFF55) else Color.Gray
                    ) 
                },
                label = { 
                    Text(
                        text = item.title,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = if (isSelected) Color(0xFFFFFF55) else Color.Gray
                    ) 
                },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFF866043) // Dirt brown active block selection
                ),
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class NavigationItem(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
