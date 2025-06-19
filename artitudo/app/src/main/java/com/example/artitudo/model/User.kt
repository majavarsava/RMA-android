package com.example.artitudo.model

import com.example.artitudo.viewmodel.UserFolderNames
import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val isAdmin: Boolean = false,
    val createdAt: Timestamp? = null,
    val folders: Map<String, List<String>> = mapOf(
        UserFolderNames.MASTERED to emptyList(),
        UserFolderNames.FAVORITES to emptyList(),
        UserFolderNames.WISHLIST to emptyList()
    )
) {
    // No-argument constructor for Firestore
    constructor() : this(
        uid = "",
        username = "",
        email = "",
        isAdmin = false,
        createdAt = null,
        folders = mapOf(
            UserFolderNames.MASTERED to emptyList(),
            UserFolderNames.FAVORITES to emptyList(),
            UserFolderNames.WISHLIST to emptyList()
        )
    )
}