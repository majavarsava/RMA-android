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
import kotlinx.coroutines.tasks.await

object UserFolderNames {
    const val MASTERED = AuthHelper.FOLDER_MASTERED // "mastered"
    const val FAVORITES = AuthHelper.FOLDER_FAVORITES // "favorites"
    const val WISHLIST = AuthHelper.FOLDER_WISHLIST // "wishlist"
}

class AuthViewModel(
    private val authHelper: AuthHelper = AuthHelper(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance() ,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(authHelper.getCurrentUser())
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val username: StateFlow<String?> = MutableStateFlow<String?>("Loading...").apply {
        viewModelScope.launch { userData.collect { value = it?.username ?: "" } }
    }.asStateFlow()

    private val _isElementInMastered = MutableStateFlow(false)
    val isElementInMastered: StateFlow<Boolean> = _isElementInMastered.asStateFlow()

    private val _isElementInFavorites = MutableStateFlow(false)
    val isElementInFavorites: StateFlow<Boolean> = _isElementInFavorites.asStateFlow()

    private val _isElementInWishlist = MutableStateFlow(false)
    val isElementInWishlist: StateFlow<Boolean> = _isElementInWishlist.asStateFlow()

    init {
        _currentUser.value = authHelper.getCurrentUser()
        _currentUser.value?.uid?.let { loadUserDataAndAdminStatus(it) }

        firebaseAuth.addAuthStateListener { auth ->
            val fbUser = auth.currentUser
            _currentUser.value = fbUser
            if (fbUser != null) {
                loadUserDataAndAdminStatus(fbUser.uid)
            } else {
                _userData.value = null
                clearElementFolderStatus()
                _authError.value = null
            }
        }
    }

    private fun loadUserDataAndAdminStatus(userId: String) {
        viewModelScope.launch {
            _authError.value = null

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
                _isAdmin.value = false
            }

            authHelper.getUserData(userId).fold(
                onSuccess = { user ->
                    _userData.value = user
                },
                onFailure = { exception ->
                    _userData.value = null
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
            _authError.value = null
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
            _authError.value = null

            if (password != confirmPassword) {
                _authError.value = "Lozinke se ne podudaraju."
                return@launch
            }
            val result = authHelper.registerUser(username, password)
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    onSuccess()
                },
                onFailure = { exception ->
                    _authError.value = exception.message ?: "Nepoznata greška pri registraciji."
                }
            )
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        authHelper.logout()
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
                    val updatedFolderList = if (isInFolder) {
                        folderList - elementId
                    } else {
                        folderList + elementId
                    }
                    val updatedFoldersMap = currentFolders.toMutableMap().apply {
                        this[folderName] = updatedFolderList
                    }
                    _userData.value = _userData.value?.copy(folders = updatedFoldersMap)

                    when (folderName) {
                        UserFolderNames.MASTERED -> _isElementInMastered.value = !isInFolder
                        UserFolderNames.FAVORITES -> _isElementInFavorites.value = !isInFolder
                        UserFolderNames.WISHLIST -> _isElementInWishlist.value = !isInFolder
                    }
                },
                onFailure = { e ->
                    _authError.value = e.message ?: "Greška pri ažuriranju mape '$folderName'."
                }
            )
        }
    }

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