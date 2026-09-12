package com.example.modernauthapp.data.model

import com.google.firebase.auth.FirebaseUser

/**
 * Domain model representing an authenticated user in the application.
 */
data class AuthUser(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val firstName: String = "",
    val surname: String = "",
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false
) {
    /**
     * Exact greeting format requested: "Hello [Firstname] [Surname]".
     * Safely handles edge-cases if surname or firstname are blank.
     */
    val welcomeGreeting: String
        get() {
            val fn = firstName.trim().ifEmpty { "User" }
            val sn = surname.trim()
            return if (sn.isNotEmpty()) {
                "Hello $fn $sn"
            } else {
                "Hello $fn"
            }
        }

    companion object {
        /**
         * Safely parse first and last names from a Firebase user and optional Google ID token details.
         */
        fun fromFirebaseUser(
            user: FirebaseUser,
            googleGivenName: String? = null,
            googleFamilyName: String? = null
        ): AuthUser {
            val (first, last) = parseNames(
                displayName = user.displayName,
                fallbackGivenName = googleGivenName,
                fallbackFamilyName = googleFamilyName
            )

            return AuthUser(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName,
                firstName = first,
                surname = last,
                photoUrl = user.photoUrl?.toString(),
                isAnonymous = user.isAnonymous
            )
        }

        fun parseNames(
            displayName: String?,
            fallbackGivenName: String?,
            fallbackFamilyName: String?
        ): Pair<String, String> {
            // Prioritize explicit given/family names from Google ID Token if present
            if (!fallbackGivenName.isNullOrBlank()) {
                val family = fallbackFamilyName.orEmpty().trim()
                return fallbackGivenName.trim() to family
            }

            if (displayName.isNullOrBlank()) {
                return "User" to ""
            }

            val tokens = displayName.trim().split("\\s+".toRegex())
            return when {
                tokens.isEmpty() -> "User" to ""
                tokens.size == 1 -> tokens[0] to ""
                else -> {
                    val first = tokens.first()
                    val surname = tokens.drop(1).joinToString(" ")
                    first to surname
                }
            }
        }
    }
}
