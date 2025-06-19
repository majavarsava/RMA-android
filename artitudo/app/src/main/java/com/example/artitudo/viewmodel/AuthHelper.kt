package com.example.artitudo.viewmodel

import com.example.artitudo.model.User
import com.google.firebase.Timestamp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class AuthHelper {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = Firebase.firestore
    private val usersCollection = db.collection("users")

    companion object {
        const val FOLDER_MASTERED = "mastered"
        const val FOLDER_FAVORITES = "favorites"
        const val FOLDER_WISHLIST = "wishlist"
    }
    private fun usernameToEmail(username: String): String {
        return "${username.lowercase()}@artitudo.app"
    }
    private fun isValidUsername(username: String): Boolean {
        return username.matches(Regex("^[a-zA-Z0-9_]{3,}$"))
    }
    // --- Registration ---
    suspend fun registerUser(username: String, password: String): Result<FirebaseUser> {
        if (!isValidUsername(username)) {
            return Result.failure(IllegalArgumentException("Korisničko ime može sadržavati samo slova, brojeve i donju crtu, minimalno 3 znaka."))
        }
        val email = usernameToEmail(username)

        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val initialUserData = User(
                    uid = firebaseUser.uid,
                    username = username,
                    email = email,
                    isAdmin = false,
                    createdAt = Timestamp.now(),
                    folders = mapOf(
                        FOLDER_FAVORITES to emptyList(),
                        FOLDER_WISHLIST to emptyList(),
                        FOLDER_MASTERED to emptyList()
                    )
                )
                val userDataMap = mapOf(
                    "uid" to initialUserData.uid,
                    "username" to initialUserData.username,
                    "email" to initialUserData.email,
                    "isAdmin" to initialUserData.isAdmin,
                    "createdAt" to initialUserData.createdAt,
                    "folders" to initialUserData.folders
                )
                usersCollection.document(firebaseUser.uid).set(userDataMap).await()
                Result.success(firebaseUser)
            } else {
                Result.failure(Exception("Registracija nije uspjela, korisnik nije kreiran."))
            }
        } catch (e: Exception) {
            val message = when (e) {
                is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "Korisničko ime '$username' je već zauzeto."
                is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "Lozinka treba imati barem 6 znakova."
                else -> e.localizedMessage ?: "Registracija nije uspjela: ${e.message}"
            }
            Result.failure(Exception(message))
        }
    }

    // --- Login ---
    suspend fun loginUser(username: String, password: String): Result<FirebaseUser> {
        if (username.isBlank()) {
            return Result.failure(IllegalArgumentException("Korisničko ime ne može biti prazno."))
        }
        val email = usernameToEmail(username)

        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                Result.success(firebaseUser)
            } else {
                Result.failure(Exception("Prijava nije uspjela."))
            }
        } catch (e: Exception) {
            val message = when (e) {
                is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "Korisničko ime '$username' nije pronađeno."
                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Netočna lozinka."
                else -> e.localizedMessage ?: "Prijava nije uspjela: ${e.message}"
            }
            Result.failure(Exception(message))
        }
    }

    // --- Logout ---
    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun getUserData(userId: String): Result<User?> {
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            val user = documentSnapshot.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            println("Error fetching user data: ${e.message}")
            Result.failure(e)
        }
    }

    // --- Generalized Add/Remove for any folder ---
    suspend fun addElementToUserFolder(elementId: String, folderName: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Korisnik nije prijavljen."))
        return try {
            val folderPath = "folders.$folderName"
            usersCollection.document(user.uid).update(folderPath, FieldValue.arrayUnion(elementId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Greška dodavanja u '$folderName': ${e.message}", e))
        }
    }

    suspend fun removeElementFromUserFolder(elementId: String, folderName: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Korisnik nije prijavljen."))
        return try {
            val folderPath = "folders.$folderName"
            usersCollection.document(user.uid).update(folderPath, FieldValue.arrayRemove(elementId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Greška uklanjanja iz '$folderName': ${e.message}", e))
        }
    }
}