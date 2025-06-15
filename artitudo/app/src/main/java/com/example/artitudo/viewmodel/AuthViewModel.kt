package com.example.artitudo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artitudo.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.io.path.exists
import kotlinx.coroutines.tasks.await

// Define folder names constants accessible by UI and ViewModel
object UserFolderNames {
    const val MASTERED = AuthHelper.FOLDER_MASTERED // "mastered"
    const val FAVORITES = AuthHelper.FOLDER_FAVORITES // "favorites"
    const val WISHLIST = AuthHelper.FOLDER_WISHLIST // "wishlist"
}

class AuthViewModel(
    private val authHelper: AuthHelper = AuthHelper(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance() ,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance() // Inject Firestore
) : ViewModel() {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(authHelper.getCurrentUser())
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // Holds the full User object from Firestore, including the 'folders' map
    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _isLoading = MutableStateFlow(false) // For user data loading
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val username: StateFlow<String?> = MutableStateFlow<String?>("Učitavanje...").apply {
        viewModelScope.launch { userData.collect { value = it?.username ?: "Gost" } }
    }.asStateFlow()

    // States for ElementDetailScreen to know if the current element is in a folder
    private val _isElementInMastered = MutableStateFlow(false)
    val isElementInMastered: StateFlow<Boolean> = _isElementInMastered.asStateFlow()

    private val _isElementInFavorites = MutableStateFlow(false)
    val isElementInFavorites: StateFlow<Boolean> = _isElementInFavorites.asStateFlow()

    private val _isElementInWishlist = MutableStateFlow(false)
    val isElementInWishlist: StateFlow<Boolean> = _isElementInWishlist.asStateFlow()

    init {
        // Initial check for current user
        _currentUser.value = authHelper.getCurrentUser()
        _currentUser.value?.uid?.let { loadUserDataAndAdminStatus(it) }

        // Listen for Firebase Auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            val fbUser = auth.currentUser
            _currentUser.value = fbUser
            if (fbUser != null) {
                loadUserDataAndAdminStatus(fbUser.uid)
            } else {
                // User logged out, clear all user-specific data
                _userData.value = null
                // _isAdmin, _username, _masteredElementsCount will update via their collectors
                clearElementFolderStatus() // Clear status for detail screen
                _authError.value = null
            }
        }
    }

    private fun loadUserDataAndAdminStatus(userId: String) {
        viewModelScope.launch {
            _authError.value = null // Clear previous errors

            // Fetch isAdmin directly
            try {
                val userDocRef = firestore.collection("users").document(userId)
                val documentSnapshot = userDocRef.get().await() // Use await()
                if (documentSnapshot.exists()) {
                    val isAdminDirect = documentSnapshot.getBoolean("isAdmin") ?: false
                    _isAdmin.value = isAdminDirect
                } else {
                    _isAdmin.value = false
                }
            } catch (e: Exception) {
                _isAdmin.value = false // Default to false on error
                // Optionally set _authError here if this failure is critical
            }

            // Fetch full user data (can run concurrently or sequentially)
            // This will update _userData, and other dependent flows like username, masteredElementsCount
            authHelper.getUserData(userId).fold(
                onSuccess = { user ->
                    _userData.value = user
                   // If direct fetch worked, _isAdmin is already set.
                    // If you trust _userData more now, you could re-set _isAdmin here:
                    // _isAdmin.value = user?.isAdmin ?: false
                },
                onFailure = { exception ->
                    // Keep _userData null or handle error
                    _userData.value = null // If full data fails, dependent features might not work
                    _authError.value = "Greška pri dohvaćanju korisničkih podataka: ${exception.message}"
                }
            )
        }
    }

    fun refreshUserData(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                authHelper.getUserData(userId).fold(
                    onSuccess = { user -> _userData.value = user },
                    onFailure = { ex -> _authError.value = "Failed to refresh user data: ${ex.message}" }
                )
            } catch (e: Exception) {
                _authError.value = "Exception refreshing user data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun clearAuthError() {
        _authError.value = null
    }

    fun login(usernameInput: String, passwordInput: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authError.value = null // Clear previous errors
            val result = authHelper.loginUser(usernameInput, passwordInput)
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    onSuccess()
                },
                onFailure = { exception ->
                    _authError.value = exception.message ?: "Unknown login error"
                }
            )
        }
    }

    fun register(username: String, password: String, confirmPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authError.value = null // Clear previous errors

            if (password != confirmPassword) {
                _authError.value = "Lozinke se ne podudaraju." // "Passwords do not match."
                return@launch
            }
            // Add other client-side password policy checks here if needed (e.g., minimum length)
            // though Firebase will also enforce its own rules (e.g., min 6 chars)

            val result = authHelper.registerUser(username, password) // AuthHelper only needs one password
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    onSuccess()
                },
                onFailure = { exception ->
                    _authError.value = exception.message ?: "Nepoznata greška pri registraciji." // "Unknown registration error"
                }
            )
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        authHelper.logout()
        // AuthStateListener handles clearing data.
        onLoggedOut()
    }

    fun toggleElementInFolder(elementId: String, folderName: String) {
        val userId = _currentUser.value?.uid ?: run {
            _authError.value = "Korisnik nije prijavljen."
            return
        }
        val currentFolders = _userData.value?.folders
        if (currentFolders == null) {
            _authError.value = "Korisnički podaci nisu učitani."
            // Optionally try to reload: loadUserData(userId)
            return
        }

        val folderList = currentFolders[folderName] ?: emptyList()
        val isInFolder = folderList.contains(elementId)

        viewModelScope.launch {
            _authError.value = null
            val operation = if (isInFolder) {
                authHelper.removeElementFromUserFolder(elementId, folderName)
            } else {
                authHelper.addElementToUserFolder(elementId, folderName)
            }

            operation.fold(
                onSuccess = {
                    // Successfully updated Firestore. Now update local _userData and derived states.
                    val updatedFolderList = if (isInFolder) {
                        folderList - elementId
                    } else {
                        folderList + elementId
                    }
                    val updatedFoldersMap = currentFolders.toMutableMap().apply {
                        this[folderName] = updatedFolderList
                    }
                    _userData.value = _userData.value?.copy(folders = updatedFoldersMap)

                    // Update the specific boolean StateFlow for the UI
                    when (folderName) {
                        UserFolderNames.MASTERED -> _isElementInMastered.value = !isInFolder
                        UserFolderNames.FAVORITES -> _isElementInFavorites.value = !isInFolder
                        UserFolderNames.WISHLIST -> _isElementInWishlist.value = !isInFolder
                    }
                },
                onFailure = { e ->
                    _authError.value = e.message ?: "Greška pri ažuriranju mape '$folderName'."
                    // Optionally, reload user data to revert optimistic UI and get fresh state
                    // loadUserData(userId)
                }
            )
        }
    }

    // Call this from ElementDetailScreen's LaunchedEffect when elementId or userData changes
    fun checkElementFolderStatus(elementId: String?) {
        val folders = _userData.value?.folders
        if (elementId == null || folders == null) {
            clearElementFolderStatus()
            return
        }
        _isElementInMastered.value = folders[UserFolderNames.MASTERED]?.contains(elementId) ?: false
        _isElementInFavorites.value = folders[UserFolderNames.FAVORITES]?.contains(elementId) ?: false
        _isElementInWishlist.value = folders[UserFolderNames.WISHLIST]?.contains(elementId) ?: false
    }

    private fun clearElementFolderStatus() {
        _isElementInMastered.value = false
        _isElementInFavorites.value = false
        _isElementInWishlist.value = false
    }
}