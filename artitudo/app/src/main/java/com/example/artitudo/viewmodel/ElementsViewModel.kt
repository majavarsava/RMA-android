// ElementsViewModel.kt
package com.example.artitudo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artitudo.model.Element
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri

class ElementsViewModel(private val elementsHelper: ElementsHelper = ElementsHelper()) : ViewModel() {

    private val _elements = MutableStateFlow<List<Element>>(emptyList())
    val elements: StateFlow<List<Element>> = _elements.asStateFlow()

    private val _filteredElements = MutableStateFlow<List<Element>>(emptyList())
    val filteredElements: StateFlow<List<Element>> = _filteredElements.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // For ElementDetailScreen
    private val _selectedElement = MutableStateFlow<Element?>(null)
    val selectedElement: StateFlow<Element?> = _selectedElement.asStateFlow()

    // This will hold ALL elements, and FolderPageScreen will filter from this
    // based on IDs provided by AuthViewModel via updateCurrentUserFolderIds
    private val _allElementsForFolderFiltering = MutableStateFlow<List<Element>>(emptyList())
    val elementsForUserFolders: StateFlow<List<Element>> = _allElementsForFolderFiltering.asStateFlow() // Rename or clarify

    private val _elementDeletionSuccess = MutableSharedFlow<Unit>() // Emits Unit on success
    val elementDeletionSuccess = _elementDeletionSuccess.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedLevel = MutableStateFlow("All")

    private var currentUserFavoriteIds: Set<String> = emptySet()
    private var currentUserWishlistIds: Set<String> = emptySet()
    private var currentUserMasteredIds: Set<String> = emptySet()

    fun clearError() {
        _error.value = null
    }

    init {
        fetchAllElementsForFiltering()
    }

    fun updateFilters(searchQuery: String, selectedLevel: String) {
        _searchQuery.value = searchQuery
        _selectedLevel.value = selectedLevel
        applyFiltersForSearchScreen(searchQuery, selectedLevel)
    }

    fun fetchAllElementsForFiltering() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            elementsHelper.getAllElements().fold(
                onSuccess = { elementList ->
                    _allElementsForFolderFiltering.value = elementList
                    // Initial filter application for search screen (if needed)
                    applyFiltersForSearchScreen(_searchQuery.value, _selectedLevel.value)
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = "Greška pri dohvaćanju elemenata: ${exception.localizedMessage}"
                    _isLoading.value = false
                }
            )
        }
    }

    // Call this when you get updated user data (folder IDs) from AuthViewModel
    fun updateUserFolderIds(
        masteredIds: List<String>?,
        favoritesIds: List<String>?,
        wishlistIds: List<String>?
    ) {
        currentUserMasteredIds = masteredIds?.toSet() ?: emptySet()
        currentUserFavoriteIds = favoritesIds?.toSet() ?: emptySet()
        currentUserWishlistIds = wishlistIds?.toSet() ?: emptySet()

        // If FolderPageScreen is active and relies on a specific Flow from this VM
        // for its content, you might need to trigger a recalculation here.
        // For now, FolderPageScreen will directly use these ID sets.
    }

    fun applyFiltersForSearchScreen(searchQuery: String, selectedLevel: String) {
        _searchQuery.value = searchQuery // Update internal state
        _selectedLevel.value = selectedLevel // Update internal state
        val currentElements = _allElementsForFolderFiltering.value // Use the master list
        _filteredElements.value = currentElements.filter { element ->
            val nameMatches = element.name.contains(searchQuery, ignoreCase = true)
            val levelMatches = selectedLevel == "All" || element.level.equals(selectedLevel, ignoreCase = true)
            nameMatches && levelMatches
        }
    }

    fun fetchElementById(elementId: String) {
        viewModelScope.launch {
            _isLoading.value = true // Can reuse isLoading or have a specific one for selectedElement
            _error.value = null
            _selectedElement.value = null // Clear previous
            elementsHelper.getElementById(elementId).fold(
                onSuccess = { element ->
                    _selectedElement.value = element
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _selectedElement.value = null // Ensure it's null on failure
                    _error.value = "Greška pri dohvaćanju detalja elementa: ${exception.localizedMessage}"
                    _isLoading.value = false
                }
            )
        }
    }

    // --- Functions for FolderPageScreen to get elements ---
    // FolderPageScreen will call these and provide the current list of all elements
    // or ElementsViewModel can expose the filtered list based on these IDs.

    fun getElementsForFolder(folderNameConstant: String): List<Element> {
        val allEls = _allElementsForFolderFiltering.value
        return when (folderNameConstant) {
            UserFolderNames.MASTERED -> allEls.filter { it.id in currentUserMasteredIds }
            UserFolderNames.FAVORITES -> allEls.filter { it.id in currentUserFavoriteIds }
            UserFolderNames.WISHLIST -> allEls.filter { it.id in currentUserWishlistIds }
            else -> emptyList()
        }
    }


    fun clearSelectedElement() {
        _selectedElement.value = null
    }

    // Function to update these IDs, perhaps called after fetching user data
    fun updateCurrentUserFolderIds(favorites: Set<String>, wishlist: Set<String>, mastered: Set<String>) {
        currentUserFavoriteIds = favorites
        currentUserWishlistIds = wishlist
        currentUserMasteredIds = mastered
        // Potentially re-trigger calculations or update a combined list if needed
    }

    // These functions are called by FolderPageScreen
    fun getUserFavoriteElements(allElements: List<Element>): List<Element> {
        return allElements.filter { it.id in currentUserFavoriteIds }
    }

    fun getUserWishlistElements(allElements: List<Element>): List<Element> {
        return allElements.filter { it.id in currentUserWishlistIds }
    }

    fun getUserMasteredElements(allElements: List<Element>): List<Element> {
        return allElements.filter { it.id in currentUserMasteredIds }
    }

    private fun String.toFirestoreId(): String {
        var id = this.lowercase()
        id = id.replace("\\s+".toRegex(), "_")
        id = id.replace("[^a-z0-9_\\-]".toRegex(), "")
        id = id.trim('_', '-')
        if (id.isEmpty()) {
            return "element_${System.currentTimeMillis()}"
        }
        return id
    }

    fun addNewElement(
        name: String,
        description: String,
        level: String,
        levelNumber: String,
        imageLocalUri: Uri?, // Changed to Uri?
        videoLocalUri: Uri?, // Changed to Uri?
        onSuccess: (newId: String) -> Unit,
        onFailure: (errorMessage: String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            var imageUrl = "" // Default to empty
            var videoUrl = "" // Default to empty
            try {
                // 1. Upload Image if URI is provided
                imageLocalUri?.let { uri ->
                    elementsHelper.uploadFileToStorage(uri, "element_images/").fold(
                        onSuccess = { downloadUrl -> imageUrl = downloadUrl },
                        onFailure = { e ->
                            _error.value = "Greška pri uploadu slike: ${e.localizedMessage}"
                            onFailure("Greška pri uploadu slike: ${e.localizedMessage}")
                            _isLoading.value = false
                            return@launch // Stop if image upload fails
                        }
                    )
                }

                // 2. Upload Video if URI is provided
                videoLocalUri?.let { uri ->
                    elementsHelper.uploadFileToStorage(uri, "element_videos/").fold(
                        onSuccess = { downloadUrl -> videoUrl = downloadUrl },
                        onFailure = { e ->
                            _error.value = "Greška pri uploadu videa: ${e.localizedMessage}"
                            onFailure("Greška pri uploadu videa: ${e.localizedMessage}")
                            _isLoading.value = false
                            return@launch // Stop if video upload fails
                        }
                    )
                }

                // 3. Create an Element object (without ID first, Firestore generates it)
                val customId = name.toFirestoreId()
                val idExists = elementsHelper.checkIfElementExists(customId)
                if (idExists) {
                    val errorMessage = "Element sa sličnim imenom (ID: $customId) već postoji."
                    _error.value = errorMessage // << SET THE ERROR STATE
                    onFailure(errorMessage) // Also call the lambda if the screen needs immediate complex reaction
                    _isLoading.value = false
                    return@launch
                }
                val newElementData = hashMapOf(
                    "name" to name,
                    "description" to description,
                    "level" to level,
                    "levelNumber" to levelNumber,
                    "image" to imageUrl, // using the download url
                    "video" to videoUrl,
                )
                elementsHelper.createElementWithIdInFirestore(customId, newElementData)
                fetchAllElementsForFiltering() // Refresh the list
                onSuccess(customId) // Pass back the auto-generated ID
            } catch (e: Exception) {
                val errorMessage = "Greška prilikom dodavanja elementa: ${e.message ?: "Nepoznata greška."}"
                _error.value = errorMessage // << SET THE ERROR STATE
                onFailure(errorMessage) // Also call the lambda
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteElement(elementId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true // Indicate loading state
            _error.value = null     // Clear previous errors
            _elements.value = _elements.value.filter { it.id != elementId }

            // Step 1: Fetch the element details to get file URLs
            elementsHelper.getElementById(elementId).fold(
                onSuccess = { elementToDelete ->
                    if (elementToDelete == null) {
                        _error.value = "Greška: Element za brisanje nije pronađen."
                        _isLoading.value = false
                        return@launch
                    }

                    val imageUrl = elementToDelete.image
                    val videoUrl = elementToDelete.video

                    // Step 2: Attempt to delete files from Storage (if URLs exist)
                    // We'll proceed even if file deletion fails, but log it.
                    if (imageUrl.isNotEmpty()) {
                        elementsHelper.deleteFileFromStorage(imageUrl).onFailure { e ->
                            println("Warning: Failed to delete image $imageUrl during element deletion: ${e.localizedMessage}")
                            // Optionally set a non-critical error or log more formally
                        }
                    }
                    if (videoUrl.isNotEmpty()) {
                        elementsHelper.deleteFileFromStorage(videoUrl).onFailure { e ->
                            println("Warning: Failed to delete video $videoUrl during element deletion: ${e.localizedMessage}")
                            // Optionally set a non-critical error or log more formally
                        }
                    }

                    // Step 3: Delete the element from Firestore
                    elementsHelper.deleteElementInFirestore(elementId).fold(
                        onSuccess = {
                            // Optimistically update local lists immediately before full refresh for smoother UI
                            _elements.value = _elements.value.filter { it.id != elementId }
                            applyFiltersForSearchScreen(_searchQuery.value, _selectedLevel.value)
                            if (_selectedElement.value?.id == elementId) {
                                _selectedElement.value = null
                            }

                            fetchAllElementsForFiltering() // Refresh the list from Firestore to ensure consistency
                            _elementDeletionSuccess.emit(Unit)
                            onSuccess()
                            _isLoading.value = false
                        },
                        onFailure = { firestoreException ->
                            _error.value = "Greška pri brisanju elementa iz baze: ${firestoreException.localizedMessage}"
                            _isLoading.value = false
                            // Re-fetch all elements to revert optimistic updates if Firestore delete failed
                            fetchAllElementsForFiltering()
                        }
                    )
                },
                onFailure = { fetchException ->
                    // Failure to fetch the element to get its file URLs
                    _error.value = "Greška pri dohvaćanju detalja elementa za brisanje: ${fetchException.localizedMessage}"
                    _isLoading.value = false
                }
            )
        }
    }

    fun updateElement(
        elementId: String,
        name: String,
        description: String,
        level: String,
        levelNumber: String,
        newImageLocalUri: Uri?,
        currentImageUrl: String, // Keep track of the old image URL
        newVideoLocalUri: Uri?,
        currentVideoUrl: String, // Keep track of the old video URL
        onSuccess: () -> Unit,
        onFailure: (errorMessage: String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            var finalImageUrl = currentImageUrl // Start with current image URL
            var finalVideoUrl = currentVideoUrl // Start with current video URL

            try {
                // 1. Upload new Image if URI is provided
                if (newImageLocalUri != null) {
                    // Optionally delete old image first if you want to replace it immediately
                    // Or delete after new one is successfully uploaded and DB updated
                    elementsHelper.uploadFileToStorage(newImageLocalUri, "element_images/").fold(
                        onSuccess = { downloadUrl ->
                            finalImageUrl = downloadUrl
                            // Delete old image only if a new one was successfully uploaded AND old one existed
                            if (currentImageUrl.isNotEmpty() && currentImageUrl != finalImageUrl) {
                                elementsHelper.deleteFileFromStorage(currentImageUrl).onFailure { e ->
                                    println("Warning: Failed to delete old image $currentImageUrl: ${e.localizedMessage}")
                                }
                            }
                        },
                        onFailure = { e ->
                            _error.value = "Greška pri uploadu nove slike: ${e.localizedMessage}"
                            onFailure("Greška pri uploadu nove slike: ${e.localizedMessage}")
                            _isLoading.value = false
                            return@launch
                        }
                    )
                }

                // 2. Upload new Video if URI is provided
                if (newVideoLocalUri != null) {
                    elementsHelper.uploadFileToStorage(newVideoLocalUri, "element_videos/").fold(
                        onSuccess = { downloadUrl ->
                            finalVideoUrl = downloadUrl
                            // Delete old video only if a new one was successfully uploaded AND old one existed
                            if (currentVideoUrl.isNotEmpty() && currentVideoUrl != finalVideoUrl) {
                                elementsHelper.deleteFileFromStorage(currentVideoUrl).onFailure { e ->
                                    println("Warning: Failed to delete old video $currentVideoUrl: ${e.localizedMessage}")
                                }
                            }
                        },
                        onFailure = { e ->
                            _error.value = "Greška pri uploadu novog videa: ${e.localizedMessage}"
                            onFailure("Greška pri uploadu novog videa: ${e.localizedMessage}")
                            _isLoading.value = false
                            return@launch
                        }
                    )
                }

                // 3. Prepare data for Firestore update
                // Note: It's usually better not to allow changing the ID (elementId / name.toFirestoreId())
                // as it can break relationships or queries. Here, we assume ID remains the same.
                val updatedElementData = hashMapOf(
                    "name" to name,
                    "description" to description,
                    "level" to level,
                    "levelNumber" to levelNumber,
                    "image" to finalImageUrl,
                    "video" to finalVideoUrl
                )

                // 4. Update Element in Firestore
                elementsHelper.updateElementInFirestore(elementId, updatedElementData).fold(
                    onSuccess = {
                        fetchAllElementsForFiltering() // Refresh the list of all elements
                        fetchElementById(elementId) // Refresh the selected element details
                        onSuccess()
                    },
                    onFailure = { e ->
                        _error.value = "Greška pri ažuriranju elementa: ${e.localizedMessage}"
                        onFailure("Greška pri ažuriranju elementa: ${e.localizedMessage}")
                    }
                )

            } catch (e: Exception) {
                val errorMessage = "Neočekivana greška prilikom ažuriranja: ${e.localizedMessage ?: "Nepoznata greška."}"
                _error.value = errorMessage
                onFailure(errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }
}