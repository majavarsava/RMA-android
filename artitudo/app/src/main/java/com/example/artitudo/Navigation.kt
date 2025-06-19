package com.example.artitudo

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.artitudo.view.ElementDetailScreen
import com.example.artitudo.view.ElementsScreen
import com.example.artitudo.view.FolderPageScreen
import com.example.artitudo.view.LevelerScreen
import com.example.artitudo.view.LoginScreen
import com.example.artitudo.view.NewElementScreen
import com.example.artitudo.view.ProfilePageScreen
import com.example.artitudo.view.RegisterScreen
import com.example.artitudo.view.EditElementScreen
import com.example.artitudo.viewmodel.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.example.artitudo.viewmodel.ElementsViewModel

sealed class Screen(val route: String) {
    object LoginScreen : Screen("loginscreen")
    object RegisterScreen : Screen("registerscreen")
    object ElementsScreen : Screen("elementsscreen")
    object ProfilePageScreen : Screen("profilepagescreen")
    object FolderPageScreen : Screen("folderpagescreen/{folderName}") {
        fun createRoute(folderName: String) = "folderpagescreen/$folderName"
    }
    object LevelerScreen : Screen("levelerscreen")
    object NewElementScreen : Screen("newelementscreen")
    object ElementDetailScreen : Screen("elementdetailscreen/{elementId}") {
        fun createRoute(elementId: String) = "elementdetailscreen/$elementId"
    }
    object EditElementScreen : Screen("editelementscreen/{elementId}") {
        fun createRoute(elementId: String) = "editelementscreen/$elementId"
    }
}

object FolderNames {
    const val USAVRSENI_ELEMENTI = "Usavršeni elementi"
    const val FAVORITI = "Favoriti"
    const val ZELJE = "Želje"
}

@Composable
fun NavigationController(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    elementsViewModel: ElementsViewModel = viewModel()
) {
    val context = LocalContext.current.applicationContext
    LaunchedEffect(key1 = elementsViewModel) {
        elementsViewModel.initializeNotificationHelper(context)
        elementsViewModel.loadAndListenForElementsRealtime()
    }

    val currentUser by authViewModel.currentUser.collectAsState()

    val startDestination = if (currentUser != null) {
        Screen.ProfilePageScreen.route
    } else {
        Screen.LoginScreen.route
    }


    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.LoginScreen.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.RegisterScreen.route) },
                onNavigateToProfile = {
                    navController.navigate(Screen.ProfilePageScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true } // Clear back stack
                        launchSingleTop = true // Avoid multiple copies of ProfilePageScreen
                    }
                }
            )
        }
        composable(Screen.RegisterScreen.route) {
            RegisterScreen(
                authViewModel = authViewModel,
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
                authViewModel = authViewModel,
                elementsViewModel = elementsViewModel,

                onNavigateToAccount = { navController.navigate(Screen.ProfilePageScreen.route) { launchSingleTop = true } },
                onNavigateToSearch = { navController.navigate(Screen.ElementsScreen.route) { launchSingleTop = true; popUpTo(Screen.ElementsScreen.route) {inclusive = true} } },
                onNavigateToCheckmark = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) { launchSingleTop = true } },
                onNavigateToHeart = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) { launchSingleTop = true } },
                onNavigateToStar = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) { launchSingleTop = true } },
                onElementClick = { elementId ->
                    navController.navigate(Screen.ElementDetailScreen.createRoute(elementId))
                }
            )
        }
        composable(Screen.ProfilePageScreen.route) {
            ProfilePageScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLeveler = { navController.navigate(Screen.LevelerScreen.route) },
                onAddElementClick = { navController.navigate(Screen.NewElementScreen.route) },

                onNavigateToAccount = { navController.navigate(Screen.ProfilePageScreen.route) { launchSingleTop = true; popUpTo(Screen.ProfilePageScreen.route) {inclusive = true} } },
                onNavigateToSearch = { navController.navigate(Screen.ElementsScreen.route) { launchSingleTop = true } },
                onNavigateToCheckmark = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) { launchSingleTop = true } },
                onNavigateToHeart = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) { launchSingleTop = true } },
                onNavigateToStar = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) { launchSingleTop = true } }
            )
        }
        composable(
            route = Screen.FolderPageScreen.route,
            arguments = listOf(navArgument("folderName") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
            FolderPageScreen(
                folderScreenIdentifier = folderName,
                elementsViewModel = elementsViewModel,
                authViewModel = authViewModel,
                onElementClick = { elementId ->
                    navController.navigate(Screen.ElementDetailScreen.createRoute(elementId))
                },

                onNavigateToAccount = { navController.navigate(Screen.ProfilePageScreen.route) { launchSingleTop = true } },
                onNavigateToSearch = { navController.navigate(Screen.ElementsScreen.route) { launchSingleTop = true } },
                onNavigateToCheckmark = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) {inclusive = true} } },
                onNavigateToHeart = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) {inclusive = true} } },
                onNavigateToStar = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) {inclusive = true} } }
            )
        }
        composable(Screen.LevelerScreen.route) {
            LevelerScreen(
                onNavigateBack = { navController.popBackStack() },

                onNavigateToAccount = { navController.navigate(Screen.ProfilePageScreen.route) { launchSingleTop = true } },
                onNavigateToSearch = { navController.navigate(Screen.ElementsScreen.route) { launchSingleTop = true } },
                onNavigateToCheckmark = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) {inclusive = true} } },
                onNavigateToHeart = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) {inclusive = true} } },
                onNavigateToStar = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) {inclusive = true} } }
            )
        }
        composable(Screen.NewElementScreen.route) {
            NewElementScreen(
                authViewModel = authViewModel,
                elementsViewModel = elementsViewModel,
                onElementCreated = { newlyCreatedElementId: String ->
                    navController.navigate(Screen.ElementDetailScreen.createRoute(newlyCreatedElementId)) {
                        popUpTo(Screen.NewElementScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateBack = { navController.popBackStack() },

                onNavigateToAccount = { navController.navigate(Screen.ProfilePageScreen.route) { launchSingleTop = true } },
                onNavigateToSearch = { navController.navigate(Screen.ElementsScreen.route) { launchSingleTop = true } },
                onNavigateToCheckmark = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) {inclusive = true} } },
                onNavigateToHeart = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) {inclusive = true} } },
                onNavigateToStar = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) {inclusive = true} } }
            )
        }
        composable(
            route = Screen.ElementDetailScreen.route,
            arguments = listOf(navArgument("elementId") { type = NavType.StringType })
        ) { backStackEntry ->
            val elementId = backStackEntry.arguments?.getString("elementId")

            if (elementId != null) {
                ElementDetailScreen(
                    elementId = elementId,
                    elementsViewModel = elementsViewModel,
                    authViewModel = authViewModel,
                    onEditClick = { idToEdit ->
                        navController.navigate(Screen.EditElementScreen.createRoute(idToEdit))
                    },
                    onElementDeletedSuccessfully = {
                        navController.navigate(Screen.ElementsScreen.route) {
                            popUpTo(Screen.ElementsScreen.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateBack = { navController.popBackStack() } ,
                    onNavigateToAccount = { navController.navigate(Screen.ProfilePageScreen.route) { launchSingleTop = true } },
                    onNavigateToSearch = { navController.navigate(Screen.ElementsScreen.route) { launchSingleTop = true } },
                    onNavigateToCheckmark = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) {inclusive = true} } },
                    onNavigateToHeart = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) {inclusive = true} } },
                    onNavigateToStar = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) { launchSingleTop = true; popUpTo(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) {inclusive = true} } }
                )
            } else {
                Text(stringResource(R.string.error_element_id_not_found))
            }
        }

        composable(
            route = Screen.EditElementScreen.route,
            arguments = listOf(navArgument("elementId") { type = NavType.StringType })
        ) { backStackEntry ->
            val elementId = backStackEntry.arguments?.getString("elementId")
            if (elementId != null) {
                EditElementScreen(
                    elementId = elementId,
                    elementsViewModel = elementsViewModel,
                    onElementUpdated = {
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() },

                    onNavigateToAccount = { navController.navigate(Screen.ProfilePageScreen.route) { launchSingleTop = true } },
                    onNavigateToSearch = { navController.navigate(Screen.ElementsScreen.route) { launchSingleTop = true } },
                    onNavigateToCheckmark = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.USAVRSENI_ELEMENTI)) { launchSingleTop = true } },
                    onNavigateToHeart = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.FAVORITI)) { launchSingleTop = true } },
                    onNavigateToStar = { navController.navigate(Screen.FolderPageScreen.createRoute(FolderNames.ZELJE)) { launchSingleTop = true } }
                )
            } else {
                Text(stringResource(R.string.error_element_id_not_found_for_edit))
            }
        }
    }
}