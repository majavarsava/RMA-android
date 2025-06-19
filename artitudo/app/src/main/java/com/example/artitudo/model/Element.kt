package com.example.artitudo.model

data class Element (
    val id: String = "",
    val name: String = "",
    val level: String = "",
    val description: String = "",
    val image: String = "",
    val video: String = ""
    ) {
        fun toMap(): Map<String, Any> {
            return mapOf(
                "name" to name,
                "level" to level,
                "description" to description,
                "image" to image,
                "video" to video
            )
        }
    }