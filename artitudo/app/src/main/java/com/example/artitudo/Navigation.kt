package com.example.artitudo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.artitudo.view.ElementDetailScreen
import com.example.artitudo.view.ElementsScreen
import com.example.artitudo.view.FolderPageScreen
import com.example.artitudo.view.LevelerScreen
import com.example.artitudo.view.LoginScreen
import com.example.artitudo.view.NewElementScreen
import com.example.artitudo.view.ProfilePageScreen
import com.example.artitudo.view.RegisterScreen

sealed class Screen(val route: String) {
    object LoginScreen : Screen("loginscreen")
    object RegisterScreen : Screen("registerscreen") // Assuming you have this
    object ElementsScreen : Screen("elementsscreen")
    object ProfilePageScreen : Screen("profilepagescreen")
    object FolderPageScreen : Screen("folderpagescreen/{folderName}") {
        fun createRoute(folderName: String) = "folderpagescreen/$folderName"
    }
    object LevelerScreen : Screen("levelerscreen") // Assuming you have this
    object NewElementScreen : Screen("newelementscreen")
    object ElementDetailScreen : Screen("elementdetailscreen/{elementId}") {
        fun createRoute(elementId: Int) = "elementdetailscreen/$elementId"
    }

    // You can add more routes here as your app grows
}

object FolderNames {
    const val USAVRSENI_ELEMENTI = "Usavršeni elementi"
    const val FAVORITI = "Favoriti"
    const val ZELJE = "Želje"
}

@Composable
fun NavigationController() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.LoginScreen.route) {
        composable(Screen.LoginScreen.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.RegisterScreen.route) },
                onNavigateToProfile = {
                    navController.navigate(Screen.ProfilePageScreen.route) {
                        // Clear back stack up to LoginScreen when logging in successfully
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                        launchSingleTop = true // Avoid multiple copies of ProfilePageScreen
                    }
                }
            )
        }
        composable(Screen.RegisterScreen.route) {
            // Assuming you have a RegisterScreen composable
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Screen.LoginScreen.route) },
                onNavigateToProfile = {
                    navController.navigate(Screen.ProfilePageScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true } // Clear back stack
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.ElementsScreen.route) {
            ElementsScreen(
                onNavigateToAccount = { navController.navigate(Screen.ProfilePageScreen.route) { launchSingleTop = true } },
                onNavigateToSearch = { /* Already on ElementsScreen, or refresh logic */ navController.navigate(Screen.ElementsScreen.route) { launchSingleTop = true; popUpTo(Screen.ElementsScreen.route) {inclusive = true} } },
                onNavigateToCheckmark = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) { launchSingleTop = true } },
                onNavigateToHeart = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) { launchSingleTop = true } },
                onNavigateToStar = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) { launchSingleTop = true } },
                onElementClick = { elementId ->
                    navController.navigate(Screen.ElementDetailScreen.createRoute(elementId))
                }
                // Add other necessary parameters
            )
        }
        composable(Screen.ProfilePageScreen.route) {
            ProfilePageScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLeveler = { navController.navigate(Screen.LevelerScreen.route) },
                onAddElementClick = { navController.navigate(Screen.NewElementScreen.route) },
                // Footer Navigation
                onNavigateToAccount = { /* Already on ProfilePageScreen */ navController.navigate(Screen.ProfilePageScreen.route) { launchSingleTop = true; popUpTo(Screen.ProfilePageScreen.route) {inclusive = true} } },
                onNavigateToSearch = { navController.navigate(Screen.ElementsScreen.route) { launchSingleTop = true } },
                onNavigateToCheckmark = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) { launchSingleTop = true } },
                onNavigateToHeart = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) { launchSingleTop = true } },
                onNavigateToStar = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) { launchSingleTop = true } }
                // Add other necessary parameters
            )
        }
        composable(
            route = Screen.FolderPageScreen.route,
            arguments = listOf(navArgument("folderName") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
            FolderPageScreen(
                folderName = folderName,
                onElementClick = { elementId ->
                    navController.navigate(Screen.ElementDetailScreen.createRoute(elementId))
                },
                // Footer Navigation
                onNavigateToAccount = { navController.navigate(Screen.ProfilePageScreen.route) { launchSingleTop = true } },
                onNavigateToSearch = { navController.navigate(Screen.ElementsScreen.route) { launchSingleTop = true } },
                onNavigateToCheckmark = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) {inclusive = true} } },
                onNavigateToHeart = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) {inclusive = true} } },
                onNavigateToStar = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) {inclusive = true} } }
                // Add other necessary parameters
            )
        }
        composable(Screen.LevelerScreen.route) {
            LevelerScreen(
                onNavigateBack = { navController.popBackStack() }
                // Add other necessary parameters
            )
        }
        composable(Screen.NewElementScreen.route) {
            NewElementScreen(
                onAddElement = { /* Assuming you pass some data or ID */
                    // For now, let's assume it navigates with a placeholder or new ID
                    // This might need adjustment based on how ElementDetailScreen handles a "new" element
                    navController.navigate(Screen.ElementDetailScreen.createRoute(-1)) // -1 for a new element, adjust as needed
                },
                onNavigateBack = { navController.popBackStack() }
                // Add other necessary parameters
            )
        }
        composable(
            route = Screen.ElementDetailScreen.route,
            arguments = listOf(navArgument("elementId") { type = NavType.IntType })
        ) { backStackEntry ->
            val elementId = backStackEntry.arguments?.getInt("elementId") ?: -1 // Default or error ID
            ElementDetailScreen(
                elementId = elementId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAccount = { navController.navigate(Screen.ProfilePageScreen.route) { launchSingleTop = true } },
                onNavigateToSearch = { navController.navigate(Screen.ElementsScreen.route) { launchSingleTop = true } },
                onNavigateToCheckmark = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) {inclusive = true} } },
                onNavigateToHeart = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) {inclusive = true} } },
                onNavigateToStar = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) {inclusive = true} } }
            )
        }
    }
}