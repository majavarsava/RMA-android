package com.example.artitudo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.artitudo.ui.theme.ArtitudoTheme
import com.example.artitudo.view.ElementsScreen
import com.example.artitudo.view.LoginScreen
import com.example.artitudo.view.NewElementScreen
import com.example.artitudo.view.RegisterScreen
import com.example.artitudo.view.ProfilePageScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ArtitudoTheme {
                ElementsScreen(
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ArtitudoTheme {
        ElementsScreen()
    }
}