package com.example.artitudo.model

import com.example.artitudo.viewmodel.UserFolderNames
import com.google.firebase.Timestamp // If you use Timestamp in Firestore

// Assuming your Firestore structure for a user document is:
// {
//   "uid": "someUid",
//   "username": "someUsername",
//   "email": "user@example.com",
//   "isAdmin": false,
//   "createdAt": Timestamp,
//   "folders": {
//     "mastered": ["elementId1", "elementId2"],
//     "favorites": ["elementId3"],
//     "wishlist": []
//   }
// }

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val isAdmin: Boolean = false,
    val createdAt: Timestamp? = null, // Or your specific Timestamp class if not Firebase's
    val folders: Map<String, List<String>> = mapOf(
        UserFolderNames.MASTERED to emptyList(),   // Using your constants
        UserFolderNames.FAVORITES to emptyList(),
        UserFolderNames.WISHLIST to emptyList()
    )
) {
    // No-argument constructor for Firestore
    constructor() : this(
        uid = "",
        username = "",
        email = "",
        isAdmin = false, // Must also default here if User() is called by Firestore
        createdAt = null,
        folders = mapOf(
            UserFolderNames.MASTERED to emptyList(),
            UserFolderNames.FAVORITES to emptyList(),
            UserFolderNames.WISHLIST to emptyList()
        )
    )
}