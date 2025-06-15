package com.example.artitudo.model

enum class ElementLevel(val displayName: String, val levelNumber: String) {
    SPINS("Spins", "0"),
    BEGINNER("Beginner", "1"),
    INTERMEDIATE("Intermediate", "2"),
    ADVANCED("Advanced", "3"),
    OTHER("Other", "4");

    companion object {
        fun fromDisplayName(displayName: String): ElementLevel {
            return values().find { it.displayName == displayName } ?: OTHER
        }
    }
}