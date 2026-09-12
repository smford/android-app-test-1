package com.example.modernauthapp.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.NoCredentialException
import com.example.modernauthapp.BuildConfig
import com.example.modernauthapp.data.model.AuthState
import com.example.modernauthapp.data.model.AuthUser
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

/**
 * Custom domain exceptions to provide clear, actionable feedback to ViewModels and UI.
 */
class AuthCancelledException(message: String = "Sign-in was cancelled.") : Exception(message)
class AuthNetworkException(message: String = "Network error. Please check your internet connection.") : Exception(message)
class AuthInvalidCredentialsException(message: String = "Invalid authentication credentials.") : Exception(message)

/**
 * Production-ready Authentication Manager coordinating Android Credential Manager API and Firebase Auth.
 */
class AuthManager(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val credentialManager: CredentialManager = CredentialManager.create(context),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    companion object {
        private const val TAG = "AuthManager"

        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Hot reactive StateFlow emitting the current AuthState.
     * Guaranteed to emit immediately on launch to drive the Auth Gate.
     */
    val authStateFlow: StateFlow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                trySend(AuthState.Authenticated(AuthUser.fromFirebaseUser(firebaseUser)))
            } else {
                trySend(AuthState.Unauthenticated)
            }
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = firebaseAuth.currentUser?.let {
            AuthState.Authenticated(AuthUser.fromFirebaseUser(it))
        } ?: AuthState.Unauthenticated
    )

    /**
     * Returns current authenticated user snapshot synchronously, if any.
     */
    val currentUser: AuthUser?
        get() = firebaseAuth.currentUser?.let { AuthUser.fromFirebaseUser(it) }

    /**
     * Initiates Google Sign-In via Android Credential Manager and links to Firebase Auth.
     *
     * @param activityContext Context from the calling Activity required by Credential Manager bottom sheet.
     * @param serverClientId Optional OAuth 2.0 Web Client ID override (defaults to BuildConfig.WEB_CLIENT_ID).
     */
    suspend fun signInWithGoogle(
        activityContext: Context,
        serverClientId: String = BuildConfig.WEB_CLIENT_ID
    ): Result<AuthUser> {
        return try {
            Log.d(TAG, "Starting Google Sign-In with serverClientId: $serverClientId")

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val givenName = googleIdTokenCredential.givenName
                val familyName = googleIdTokenCredential.familyName

                Log.d(TAG, "Received ID Token. Linking with Firebase Auth...")

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()

                val firebaseUser = authResult.user
                    ?: throw IllegalStateException("Firebase returned null user after sign in.")

                val authUser = AuthUser.fromFirebaseUser(
                    user = firebaseUser,
                    googleGivenName = givenName,
                    googleFamilyName = familyName
                )

                Log.d(TAG, "Successfully authenticated user: ${authUser.welcomeGreeting}")
                Result.success(authUser)
            } else {
                Log.e(TAG, "Unexpected credential type returned: ${credential.type}")
                Result.failure(IllegalStateException("Unsupported credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Sign-in cancelled by user")
            Result.failure(AuthCancelledException("Google Sign-In was cancelled."))
        } catch (e: GetCredentialInterruptedException) {
            Log.w(TAG, "Sign-in interrupted")
            Result.failure(AuthCancelledException("Sign-in dialog was interrupted."))
        } catch (e: NoCredentialException) {
            Log.e(TAG, "No credentials available on this device", e)
            Result.failure(IllegalStateException("No Google account found on device."))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Failed to parse Google ID token", e)
            Result.failure(AuthInvalidCredentialsException("Failed to verify Google credentials."))
        } catch (e: FirebaseNetworkException) {
            Log.e(TAG, "Network failure during Firebase authentication", e)
            Result.failure(AuthNetworkException("Network error. Please check your connection and retry."))
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Firebase Auth error: ${e.errorCode}", e)
            Result.failure(Exception(e.localizedMessage ?: "Authentication failed with Firebase."))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: ${e.message}", e)
            Result.failure(Exception(e.localizedMessage ?: "Failed to retrieve Google credentials."))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            Result.failure(e)
        }
    }

    /**
     * Signs the user out, purging both Credential Manager session and Firebase session.
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            Log.d(TAG, "Signing out user...")
            // Clear Credential Manager state so account chooser shows on next sign in
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            // Sign out from Firebase
            firebaseAuth.signOut()
            Log.d(TAG, "Sign out completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing credentials during logout", e)
            // Ensure local firebase token is purged regardless of CredentialManager clear exception
            firebaseAuth.signOut()
            Result.failure(e)
        }
    }
}
