package com.example.modernauthapp.ui.screens.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.modernauthapp.data.auth.AuthCancelledException
import com.example.modernauthapp.data.auth.AuthInvalidCredentialsException
import com.example.modernauthapp.data.auth.AuthManager
import com.example.modernauthapp.data.auth.AuthNetworkException
import com.example.modernauthapp.data.model.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val authenticatedUser: AuthUser? = null
)

class LoginViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signInWithGoogle(activityContext: Context) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authManager.signInWithGoogle(activityContext)

            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            authenticatedUser = user,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    when (error) {
                        is AuthCancelledException -> {
                            // User intentionally cancelled the Google bottom sheet: reset state gracefully
                            _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                        }
                        is AuthNetworkException -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Network error: Please check your internet connection and try again."
                                )
                            }
                        }
                        is AuthInvalidCredentialsException -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Credential verification failed. Please try signing in again."
                                )
                            }
                        }
                        else -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = error.localizedMessage ?: "Sign-in failed. Please try again."
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(private val authManager: AuthManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(authManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
