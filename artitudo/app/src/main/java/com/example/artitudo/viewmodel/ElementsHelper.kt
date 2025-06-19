package com.example.artitudo.viewmodel
// ElementsHelper.kt

import com.example.artitudo.model.Element
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import java.util.UUID

class ElementsHelper {
    private val db = FirebaseFirestore.getInstance()
    private val elementsCollection = db.collection("elements")
    private val storage = FirebaseStorage.getInstance()

    suspend fun getAllElements(): Result<List<Element>> {
        return try {
            val querySnapshot = elementsCollection.orderBy("name", Query.Direction.ASCENDING).get().await()
            val elements = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Element::class.java)?.copy(id = document.id)
            }
            Result.success(elements)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getElementById(elementId: String): Result<Element?> {
        return try {
            val documentSnapshot = elementsCollection.document(elementId).get().await()
            val element = documentSnapshot.toObject(Element::class.java)?.copy(id = documentSnapshot.id)
            Result.success(element)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createElementWithIdInFirestore(documentId: String, data: Map<String, Any>) {
        elementsCollection.document(documentId).set(data).await()
    }

    suspend fun checkIfElementExists(documentId: String): Boolean {
        return try {
            val document = elementsCollection.document(documentId).get().await()
            document.exists()
        } catch (e: Exception) {
            false
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

    suspend fun uploadFileToStorage(fileUri: Uri, storagePath: String): Result<String> {
        return try {
            val fileName = "${UUID.randomUUID()}-${fileUri.lastPathSegment ?: "file"}"
            val fileRef = storage.reference.child("$storagePath$fileName")

            fileRef.putFile(fileUri).await()

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

    suspend fun deleteFileFromStorage(fileUrl: String): Result<Unit> {
        if (fileUrl.isEmpty()) return Result.success(Unit)
        return try {
            val storageRef = storage.getReferenceFromUrl(fileUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: StorageException) {
            if (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                Result.success(Unit)
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}