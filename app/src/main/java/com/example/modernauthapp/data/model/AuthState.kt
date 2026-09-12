package com.example.modernauthapp.data.model

/**
 * Represents the current authentication state of the application.
 */
sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
    data class Error(val message: String) : AuthState
}
