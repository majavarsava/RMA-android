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

import android.content.Context
import com.example.artitudo.utils.NotificationHelper
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject

class ElementsViewModel(
    private val elementsHelper: ElementsHelper = ElementsHelper()
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val elementsCollection = db.collection("elements")
    private var elementsListener: ListenerRegistration? = null

    private val _elements = MutableStateFlow<List<Element>>(emptyList())
    val elements: StateFlow<List<Element>> = _elements.asStateFlow()

    private val _filteredElements = MutableStateFlow<List<Element>>(emptyList())
    val filteredElements: StateFlow<List<Element>> = _filteredElements.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedElement = MutableStateFlow<Element?>(null)
    val selectedElement: StateFlow<Element?> = _selectedElement.asStateFlow()

    private val _allElementsForFolderFiltering = MutableStateFlow<List<Element>>(emptyList())
    val elementsForUserFolders: StateFlow<List<Element>> = _allElementsForFolderFiltering.asStateFlow()

    private val _elementDeletionSuccess = MutableSharedFlow<Unit>()
    val elementDeletionSuccess = _elementDeletionSuccess.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedLevel = MutableStateFlow("All")

    private var currentUserFavoriteIds: Set<String> = emptySet()
    private var currentUserWishlistIds: Set<String> = emptySet()
    private var currentUserMasteredIds: Set<String> = emptySet()

    private var notificationHelperInstance: NotificationHelper? = null
    private var isInitialFetchDone = false
    private var lastShownNotificationTimestamp = 0L
    private val notificationCooldownMillis = 5000
    private val notifiedElementIds = mutableSetOf<String>()

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
                    applyFiltersForSearchScreen(_searchQuery.value, _selectedLevel.value)
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = "${exception.localizedMessage}"
                    _isLoading.value = false
                }
            )
        }
    }

    fun updateUserFolderIds(
        masteredIds: List<String>?,
        favoritesIds: List<String>?,
        wishlistIds: List<String>?
    ) {
        currentUserMasteredIds = masteredIds?.toSet() ?: emptySet()
        currentUserFavoriteIds = favoritesIds?.toSet() ?: emptySet()
        currentUserWishlistIds = wishlistIds?.toSet() ?: emptySet()
    }

    fun applyFiltersForSearchScreen(searchQuery: String, selectedLevel: String) {
        _searchQuery.value = searchQuery
        _selectedLevel.value = selectedLevel
        val currentElements = _allElementsForFolderFiltering.value
        _filteredElements.value = currentElements.filter { element ->
            val nameMatches = element.name.contains(searchQuery, ignoreCase = true)
            val levelMatches = selectedLevel == "All" || element.level.equals(selectedLevel, ignoreCase = true)
            nameMatches && levelMatches
        }
    }

    fun fetchElementById(elementId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedElement.value = null
            elementsHelper.getElementById(elementId).fold(
                onSuccess = { element ->
                    _selectedElement.value = element
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _selectedElement.value = null
                    _error.value = "${exception.localizedMessage}"
                    _isLoading.value = false
                }
            )
        }
    }

    fun getElementsForFolder(folderNameConstant: String): List<Element> {
        val allEls = _allElementsForFolderFiltering.value
        return when (folderNameConstant) {
            UserFolderNames.MASTERED -> allEls.filter { it.id in currentUserMasteredIds }
            UserFolderNames.FAVORITES -> allEls.filter { it.id in currentUserFavoriteIds }
            UserFolderNames.WISHLIST -> allEls.filter { it.id in currentUserWishlistIds }
            else -> emptyList()
        }
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
        imageLocalUri: Uri?,
        videoLocalUri: Uri?,
        onSuccess: (newId: String) -> Unit,
        onFailure: (errorMessage: String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            var imageUrl = ""
            var videoUrl = ""
            try {
                // 1. Upload Image if URI is provided
                imageLocalUri?.let { uri ->
                    elementsHelper.uploadFileToStorage(uri, "element_images/").fold(
                        onSuccess = { downloadUrl -> imageUrl = downloadUrl },
                        onFailure = { e ->
                            _error.value = "${e.localizedMessage}"
                            onFailure("${e.localizedMessage}")
                            _isLoading.value = false
                            return@launch
                        }
                    )
                }

                // 2. Upload Video if URI is provided
                videoLocalUri?.let { uri ->
                    elementsHelper.uploadFileToStorage(uri, "element_videos/").fold(
                        onSuccess = { downloadUrl -> videoUrl = downloadUrl },
                        onFailure = { e ->
                            _error.value = "${e.localizedMessage}"
                            onFailure("${e.localizedMessage}")
                            _isLoading.value = false
                            return@launch
                        }
                    )
                }

                // 3. Create an Element object
                val customId = name.toFirestoreId()
                val idExists = elementsHelper.checkIfElementExists(customId)
                if (idExists) {
                    val errorMessage = "ID already exists (ID: $customId)"
                    _error.value = errorMessage
                    onFailure(errorMessage)
                    _isLoading.value = false
                    return@launch
                }
                val newElementData = hashMapOf(
                    "name" to name,
                    "description" to description,
                    "level" to level,
                    "image" to imageUrl,
                    "video" to videoUrl,
                )
                elementsHelper.createElementWithIdInFirestore(customId, newElementData)
                fetchAllElementsForFiltering()
                onSuccess(customId)
            } catch (e: Exception) {
                val errorMessage = "Loading error: ${e.message ?: "Unknown."}"
                _error.value = errorMessage
                onFailure(errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteElement(elementId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _elements.value = _elements.value.filter { it.id != elementId }

            elementsHelper.getElementById(elementId).fold(
                onSuccess = { elementToDelete ->
                    if (elementToDelete == null) {
                        _error.value = "Not found."
                        _isLoading.value = false
                        return@launch
                    }

                    val imageUrl = elementToDelete.image
                    val videoUrl = elementToDelete.video

                    if (imageUrl.isNotEmpty()) {
                        elementsHelper.deleteFileFromStorage(imageUrl).onFailure { e ->
                            println("Warning: Failed to delete image $imageUrl during element deletion: ${e.localizedMessage}")
                        }
                    }
                    if (videoUrl.isNotEmpty()) {
                        elementsHelper.deleteFileFromStorage(videoUrl).onFailure { e ->
                            println("Warning: Failed to delete video $videoUrl during element deletion: ${e.localizedMessage}")
                        }
                    }

                    elementsHelper.deleteElementInFirestore(elementId).fold(
                        onSuccess = {
                            _elements.value = _elements.value.filter { it.id != elementId }
                            applyFiltersForSearchScreen(_searchQuery.value, _selectedLevel.value)
                            if (_selectedElement.value?.id == elementId) {
                                _selectedElement.value = null
                            }

                            fetchAllElementsForFiltering()
                            _elementDeletionSuccess.emit(Unit)
                            onSuccess()
                            _isLoading.value = false
                        },
                        onFailure = { firestoreException ->
                            _error.value = "Greška pri brisanju elementa iz baze: ${firestoreException.localizedMessage}"
                            _isLoading.value = false
                            fetchAllElementsForFiltering()
                        }
                    )
                },
                onFailure = { fetchException ->
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
        newImageLocalUri: Uri?,
        currentImageUrl: String,
        newVideoLocalUri: Uri?,
        currentVideoUrl: String,
        onSuccess: () -> Unit,
        onFailure: (errorMessage: String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            var finalImageUrl = currentImageUrl
            var finalVideoUrl = currentVideoUrl

            try {
                if (newImageLocalUri != null) {
                    elementsHelper.uploadFileToStorage(newImageLocalUri, "element_images/").fold(
                        onSuccess = { downloadUrl ->
                            finalImageUrl = downloadUrl
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

                if (newVideoLocalUri != null) {
                    elementsHelper.uploadFileToStorage(newVideoLocalUri, "element_videos/").fold(
                        onSuccess = { downloadUrl ->
                            finalVideoUrl = downloadUrl
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

                val updatedElementData = hashMapOf(
                    "name" to name,
                    "description" to description,
                    "level" to level,
                    "image" to finalImageUrl,
                    "video" to finalVideoUrl
                )

                elementsHelper.updateElementInFirestore(elementId, updatedElementData).fold(
                    onSuccess = {
                        fetchAllElementsForFiltering()
                        fetchElementById(elementId)
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

    fun initializeNotificationHelper(context: Context) {
        if (notificationHelperInstance == null) {
            notificationHelperInstance = NotificationHelper(context.applicationContext)
        }
    }

    private fun attachFirestoreListener() {
        _isLoading.value = true
        elementsListener?.remove()

        elementsListener = elementsCollection
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    _error.value = "Error listening to elements: ${e.localizedMessage}"
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshots == null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val currentMasterList = _allElementsForFolderFiltering.value.toMutableList()
                var listChanged = false
                val newlyAddedForNotification = mutableListOf<Element>()

                for (dc in snapshots.documentChanges) {
                    val element = dc.document.toObject<Element>()?.copy(id = dc.document.id) ?: continue

                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            if (!currentMasterList.any { it.id == element.id }) {
                                currentMasterList.add(element)
                                listChanged = true
                                if (isInitialFetchDone && !notifiedElementIds.contains(element.id)) {
                                    newlyAddedForNotification.add(element)
                                }
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val index = currentMasterList.indexOfFirst { it.id == element.id }
                            if (index != -1) {
                                currentMasterList[index] = element
                                listChanged = true
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            if (currentMasterList.removeAll { it.id == element.id }) {
                                listChanged = true
                                notifiedElementIds.remove(element.id)
                            }
                        }
                    }
                }

                if (listChanged) {
                    _allElementsForFolderFiltering.value = currentMasterList.sortedBy { it.name }
                    _elements.value = _allElementsForFolderFiltering.value
                    applyFiltersForSearchScreen(_searchQuery.value, _selectedLevel.value)
                }

                notificationHelperInstance?.let { helper ->
                    newlyAddedForNotification.forEach { element ->
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastShownNotificationTimestamp > notificationCooldownMillis) {
                            helper.showNewElementNotification(element.name, element.id, element.id.hashCode())
                            lastShownNotificationTimestamp = currentTime
                            notifiedElementIds.add(element.id)
                        } else {
                        }
                    }
                }

                if (!isInitialFetchDone && snapshots.documentChanges.any { it.type == DocumentChange.Type.ADDED || it.type == DocumentChange.Type.MODIFIED }) {
                    if (currentMasterList.isNotEmpty()) {
                        isInitialFetchDone = true
                    }
                }
                _isLoading.value = false
            }
    }

    fun loadAndListenForElementsRealtime() {
        if (elementsListener == null) {
            attachFirestoreListener()
        }
    }

    override fun onCleared() {
        super.onCleared()
        elementsListener?.remove()
        isInitialFetchDone = false
        notifiedElementIds.clear()
    }
}