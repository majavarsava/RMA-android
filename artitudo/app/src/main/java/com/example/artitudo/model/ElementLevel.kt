package com.example.artitudo.model

enum class ElementLevel(val displayName: String) {
    SPINS("Spins"),
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    OTHER("Other");

    companion object {
        fun fromDisplayName(displayName: String): ElementLevel {
            return values().find { it.displayName == displayName } ?: OTHER
        }
    }
}