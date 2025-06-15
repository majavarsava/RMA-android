package com.example.artitudo

import android.icu.text.CaseMap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.artitudo.ui.theme.ArtitudoTheme
import com.example.artitudo.view.ElementDetailScreen
import com.example.artitudo.view.ElementsScreen
import com.example.artitudo.view.FolderPageScreen
import com.example.artitudo.view.LevelerScreen
import com.example.artitudo.view.LoginScreen
import com.example.artitudo.view.NewElementScreen
import com.example.artitudo.view.RegisterScreen
import com.example.artitudo.view.ProfilePageScreen

// private lateinit var auth: FirebaseAuth;
//// ...
//// Initialize Firebase Auth
//auth = Firebase.auth

// public override fun onStart() {
//    super.onStart()
//    // Check if user is signed in (non-null) and update UI accordingly.
//    val currentUser = auth.currentUser
//    updateUI(currentUser)
//}
// Firebase.auth.signOut()

// customToken?.let {
//    auth.signInWithCustomToken(it)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithCustomToken:success")
//                    val user = auth.currentUser
//                    updateUI(user)
//                } else {
//                    // If sign in fails, display a message to the user.
//                    Log.w(TAG, "signInWithCustomToken:failure", task.exception)
//                    Toast.makeText(baseContext, "Authentication failed.",
//                            Toast.LENGTH_SHORT).show()
//                    updateUI(null)
//                }
//            }
//}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ArtitudoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ){
                    NavigationController()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ArtitudoTheme {
        LevelerScreen()
    }
}