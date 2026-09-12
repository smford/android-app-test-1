package com.example.modernauthapp.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.modernauthapp.data.auth.AuthManager
import com.example.modernauthapp.data.model.AuthState
import com.example.modernauthapp.data.model.AuthUser
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val user: AuthUser? = null,
    val isLoggingOut: Boolean = false,
    val logoutComplete: Boolean = false
)

class DashboardViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = authManager.authStateFlow
        .map { authState ->
            when (authState) {
                is AuthState.Authenticated -> DashboardUiState(user = authState.user)
                else -> DashboardUiState(user = null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(user = authManager.currentUser)
        )

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authManager.signOut()
            onComplete()
        }
    }

    class Factory(private val authManager: AuthManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(authManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
