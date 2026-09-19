package com.example.arise.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arise.data.AriseRepository
import com.example.arise.data.AuthRepository
import com.example.arise.domain.LogIcon
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val isInitializing: Boolean = true,
    val userEmail: String? = null,
    val username: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val ariseRepository: AriseRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<AuthUiState>
    val uiState: StateFlow<AuthUiState>

    init {
        // Seed initial state synchronously from cached Firebase user to prevent flicker
        val cachedUser = authRepository.getCurrentUser()
        _uiState = MutableStateFlow(
            AuthUiState(
                isLoggedIn = cachedUser != null,
                isInitializing = true,
                userEmail = cachedUser?.email,
                username = cachedUser?.displayName ?: cachedUser?.email?.substringBefore("@") ?: "HUNTER"
            )
        )
        uiState = _uiState.asStateFlow()

        viewModelScope.launch {
            authRepository.authState.collect { user ->
                _uiState.update {
                    it.copy(
                        isLoggedIn = user != null,
                        isInitializing = false,
                        userEmail = user?.email,
                        username = user?.displayName ?: user?.email?.substringBefore("@") ?: "HUNTER"
                    )
                }
                if (user != null) {
                    val name = user.displayName ?: user.email?.substringBefore("@") ?: "HUNTER"
                    syncProfileName(name)
                }
            }
        }
    }

    private suspend fun syncProfileName(name: String) {
        val currentProfile = ariseRepository.getProfileOnce()
        if (currentProfile != null) {
            // Preserve user's custom name if already set
            if (currentProfile.username.isBlank() || currentProfile.username == "HUNTER") {
                ariseRepository.upsertProfile(currentProfile.copy(username = name))
            }
        }
    }

    fun signIn(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "System Access Denied: Email & Password required.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.signIn(email, pass)
            if (result.isSuccess) {
                val res = result.getOrThrow()
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                ariseRepository.logEvent(LogIcon.INFO, "System Access Granted: Hunter authenticated.")
            } else {
                val err = result.exceptionOrNull()!!
                val userMsg = when {
                    err is FirebaseAuthInvalidUserException ||
                            err.message?.contains("no user record", ignoreCase = true) == true ->
                        "No account found for this email. Tap the 'NEW HUNTER' tab to register first!"
                    err is FirebaseAuthInvalidCredentialsException ->
                        "Invalid Password / Security Key. Please verify your credentials."
                    else -> err.localizedMessage ?: err.message ?: "Authentication failed."
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = userMsg) }
            }
        }
    }

    fun signUp(email: String, pass: String, username: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Hunter Registration Failed: Email & Password required.") }
            return
        }
        if (pass.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password security requirement: Minimum 6 characters.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.signUp(email, pass, username)
            if (result.isSuccess) {
                val res = result.getOrThrow()
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                val finalName = if (username.isNotBlank()) username.trim() else email.substringBefore("@")
                ariseRepository.logEvent(LogIcon.INFO, "New Hunter Registered: '$finalName'.")
            } else {
                val err = result.exceptionOrNull()!!
                val userMsg = when {
                    err is FirebaseAuthUserCollisionException ->
                        "Account already exists! Tap the 'SYSTEM LOGIN' tab to sign in."
                    err is FirebaseAuthWeakPasswordException ->
                        "Password is too weak. Please use at least 6 characters."
                    else -> err.localizedMessage ?: err.message ?: "Registration failed."
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = userMsg) }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your registered email address.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, successMessage = "System Password Reset link transmitted to $email.") }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage ?: "Failed to send reset link.") }
                }
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.signInWithGoogleCredential(idToken)
            result.fold(
                onSuccess = { res ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                    val name = res.user?.displayName ?: res.user?.email?.substringBefore("@") ?: "HUNTER"
                    ariseRepository.logEvent(LogIcon.INFO, "Google Access Verified: '$name'.")
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage ?: err.message ?: "Google Authentication Failed.") }
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            ariseRepository.logEvent(LogIcon.INFO, "System Access Deactivated: Hunter Logged Out.")
            authRepository.signOut()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(errorMessage = message, isLoading = false) }
    }
}
