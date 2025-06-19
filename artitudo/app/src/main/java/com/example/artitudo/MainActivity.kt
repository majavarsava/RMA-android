package com.example.artitudo

import android.os.Bundle
import android.Manifest
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.artitudo.ui.theme.ArtitudoTheme

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {

    private var currentNavController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ArtitudoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ){
                    val navController = rememberNavController()
                    this.currentNavController = navController

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        NotificationPermissionRequester()
                    }
                    LaunchedEffect(key1 = intent) {
                        intent?.let {
                            if (it.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == 0) {
                                handleNotificationIntent(it, navController)
                            }
                        }
                    }
                    NavigationController(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { newIntent ->
            currentNavController?.let { navController ->
                handleNotificationIntent(newIntent, navController)
            }
        }
    }

    companion object {
        fun handleNotificationIntent(intent: Intent, navController: NavHostController) {
            val elementId = intent.getStringExtra("elementId_from_notification")
            if (elementId != null) {
                navController.navigate(Screen.ElementDetailScreen.createRoute(elementId)) {
                    launchSingleTop = true
                }
                intent.removeExtra("elementId_from_notification")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionRequester() {
    val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            Log.d("Permission", "Requesting POST_NOTIFICATIONS permission.")
            permissionState.launchPermissionRequest()
        } else {
            Log.d("Permission", "POST_NOTIFICATIONS permission already granted.")
        }
    }
}