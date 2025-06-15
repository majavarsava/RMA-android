package com.example.artitudo.model

data class Element (
    val id: String = "", // Firebase document ID
    val name: String = "",
    val levelNumber: String = "", // 0=Spins, 1=Beginner, 2=Intermediate, 3=Advanced, 4=Other
    val level: String = "", // "Spins", "Beginner", etc.
    val description: String = "",
    val image: String = "", // Firebase Storage URL
    val video: String = "" // Firebase Storage URL
    ) {
        // Convert to Map for Firebase
        fun toMap(): Map<String, Any> {
            return mapOf(
                "name" to name,
                "levelNumber" to levelNumber,
                "level" to level,
                "description" to description,
                "image" to image,
                "video" to video
            )
        }
    }