package com.example.artitudo.viewmodel
// ElementsHelper.kt

import com.example.artitudo.model.Element
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.tasks.await
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import java.util.UUID // For generating unique file names

class ElementsHelper {
    private val db = FirebaseFirestore.getInstance()
    private val elementsCollection = db.collection("elements")
    private val storage = FirebaseStorage.getInstance()

    suspend fun getAllElements(): Result<List<Element>> {
        return try {
            val querySnapshot = elementsCollection.orderBy("name", Query.Direction.ASCENDING).get().await()
            // Map Firestore documents to Element objects, including the document ID
            val elements = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Element::class.java)?.copy(id = document.id)
            }
            Result.success(elements)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getElementById(elementId: String): Result<com.example.artitudo.model.Element?> {
        return try {
            val documentSnapshot = elementsCollection.document(elementId).get().await()
            val element = documentSnapshot.toObject(Element::class.java)?.copy(id = documentSnapshot.id)
            Result.success(element)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NEW METHOD - Custom ID
    suspend fun createElementWithIdInFirestore(documentId: String, data: Map<String, Any>) {
        // Use .set() to create a document with a specific ID.
        // If the document does not exist, it will be created.
        // If the document does exist, its contents will be overwritten (unless using SetOptions.merge()).
        elementsCollection.document(documentId).set(data).await()
    }

    // NEW METHOD - Check if document ID exists
    suspend fun checkIfElementExists(documentId: String): Boolean {
        return try {
            val document = elementsCollection.document(documentId).get().await()
            document.exists()
        } catch (e: Exception) {
            // Log.e("ElementsHelper", "Error checking if document exists: $documentId", e)
            // Decide how to handle errors: conservatively assume it might exist, or rethrow.
            // For simplicity, returning false, but you might want more robust error handling.
            false // Or true to be super cautious and prevent overwrite on error
        }
    }

    suspend fun deleteElementInFirestore(elementId: String): Result<Unit> {
        return try {
            elementsCollection.document(elementId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uploads a file to Firebase Storage and returns its download URL.
     * @param fileUri The local URI of the file to upload.
     * @param storagePath The path in Firebase Storage where the file should be stored (e.g., "element_images/").
     * @return Result containing the download URL String on success, or an Exception on failure.
     */
    suspend fun uploadFileToStorage(fileUri: Uri, storagePath: String): Result<String> {
        return try {
            // Generate a unique file name to avoid overwrites and ensure valid characters
            val fileName = "${UUID.randomUUID()}-${fileUri.lastPathSegment ?: "file"}"
            val fileRef = storage.reference.child("$storagePath$fileName")

            // Upload file
            fileRef.putFile(fileUri).await()

            // Get download URL
            val downloadUrl = fileRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateElementInFirestore(elementId: String, data: Map<String, Any>): Result<Unit> {
        return try {
            elementsCollection.document(elementId).update(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a file from Firebase Storage given its full download URL.
     * @param fileUrl The download URL of the file to delete.
     * @return Result indicating success or failure.
     */
    suspend fun deleteFileFromStorage(fileUrl: String): Result<Unit> {
        if (fileUrl.isEmpty()) return Result.success(Unit) // Nothing to delete
        return try {
            val storageRef = storage.getReferenceFromUrl(fileUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: StorageException) {
            // It's common for a file to not exist if it was already deleted or never uploaded properly.
            // You can choose to ignore "object not found" errors or log them differently.
            if (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                // Log.w("ElementsHelper", "Attempted to delete non-existent file: $fileUrl")
                Result.success(Unit) // Treat as success if file wasn't there to delete
            } else {
                // Log.e("ElementsHelper", "Error deleting file from storage: $fileUrl", e)
                Result.failure(e)
            }
        } catch (e: Exception) { // Catch other potential errors like invalid URL
            // Log.e("ElementsHelper", "Error deleting file from storage (invalid URL?): $fileUrl", e)
            Result.failure(e)
        }
    }
}