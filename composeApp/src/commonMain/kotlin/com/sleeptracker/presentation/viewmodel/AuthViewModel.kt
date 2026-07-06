package com.sleeptracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeptracker.model.User
import com.sleeptracker.network.HttpApiClient
import com.sleeptracker.storage.AppDataStore
import com.sleeptracker.storage.DataStoreKeys
import com.sleeptracker.storage.SecureStorage
import com.sleeptracker.storage.SecureStorageKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// MIGRATION: Zustand `useAuthStore` → ViewModel + StateFlow.
// ViewModel is lifecycle-aware; survives configuration changes (screen rotations).
// Zustand's `set({})` function → `_state.value = newState` on MutableStateFlow.

// MIGRATION: UiState sealed class replaces the ad-hoc boolean flags in Zustand
// (isLoading, isCheckingAuth) with a well-typed state machine.
sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object CheckingAuth : AuthUiState()
    data class Authenticated(val user: User, val token: String) : AuthUiState()
    data object Unauthenticated : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

data class AuthResult(val success: Boolean, val message: String? = null)

class AuthViewModel(
    private val httpClient: HttpApiClient,
    private val secureStorage: SecureStorage,
    private val dataStore: AppDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.CheckingAuth)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Expose current user and token for direct access by repositories
    // (replaces `useAuthStore.getState().user` pattern)
    val currentUser: User? get() = (_uiState.value as? AuthUiState.Authenticated)?.user
    val currentToken: String? get() = (_uiState.value as? AuthUiState.Authenticated)?.token

    init {
        checkAuth()
    }

    // MIGRATION: `checkAuth: async () => { AsyncStorage.getItem('user'); SecureStore.getItemAsync('authToken') }`
    // → suspend function launched in viewModelScope
    fun checkAuth() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.CheckingAuth
            try {
                val userJson = dataStore.getString(DataStoreKeys.USER)
                val token = secureStorage.getItem(SecureStorageKeys.AUTH_TOKEN)
                if (userJson != null && token != null) {
                    val user = Json.decodeFromString<User>(userJson)
                    _uiState.value = AuthUiState.Authenticated(user, token)
                } else {
                    _uiState.value = AuthUiState.Unauthenticated
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Unauthenticated
            }
        }
    }

    // MIGRATION: `register: async (firstName, lastName, email, password) => { ... }`
    // → suspend fun returning AuthResult (preserves success/message structure)
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): AuthResult {
        _uiState.value = AuthUiState.Loading
        return try {
            // POST /auth/register
            val response = httpClient.post<RegisterResponse>(
                path = "/auth/register",
                body = mapOf(
                    "firstName" to firstName,
                    "lastName"  to lastName,
                    "email"     to email,
                    "password"  to password
                )
            )
            dataStore.setString(DataStoreKeys.USER, Json.encodeToString(response.user))
            // MIGRATION: SecureStore.setItemAsync('authToken', token) → secureStorage.setItem(...)
            secureStorage.setItem(SecureStorageKeys.AUTH_TOKEN, response.token)
            _uiState.value = AuthUiState.Authenticated(response.user, response.token)
            AuthResult(success = true)
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error(e.message ?: "Registration failed")
            AuthResult(success = false, message = e.message ?: "An error occurred during registration.")
        }
    }

    // MIGRATION: `login: async (email, password) => { ... }`
    suspend fun login(email: String, password: String): AuthResult {
        _uiState.value = AuthUiState.Loading
        return try {
            val response = httpClient.post<LoginResponse>(
                path = "/auth/login",
                body = mapOf("email" to email, "password" to password)
            )
            dataStore.setString(DataStoreKeys.USER, Json.encodeToString(response.user))
            secureStorage.setItem(SecureStorageKeys.AUTH_TOKEN, response.token)
            _uiState.value = AuthUiState.Authenticated(response.user, response.token)
            AuthResult(success = true)
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error(e.message ?: "Login failed")
            AuthResult(success = false, message = e.message ?: "An error occurred during login.")
        }
    }

    // MIGRATION: `logout: async () => { AsyncStorage.removeItem('user'); SecureStore.deleteItemAsync('authToken') }`
    fun logout() {
        viewModelScope.launch {
            try {
                dataStore.remove(DataStoreKeys.USER)
                secureStorage.deleteItem(SecureStorageKeys.AUTH_TOKEN)
                _uiState.value = AuthUiState.Unauthenticated
            } catch (e: Exception) {
                // Continue to unauthenticated state even if storage cleanup fails
                _uiState.value = AuthUiState.Unauthenticated
            }
        }
    }
}

// ─── API response types ───────────────────────────────────────────────────────
// MIGRATION: TypeScript inferred `data: any` from httpClient.post → typed data classes
@kotlinx.serialization.Serializable
private data class RegisterResponse(val user: User, val token: String)

@kotlinx.serialization.Serializable
private data class LoginResponse(val user: User, val token: String)
