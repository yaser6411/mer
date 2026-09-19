package com.example.domain.model

/**
 * Production-ready sealed UI state representation.
 * Guarantees exhaustive state handling across Compose screens (Loading, Success, Empty, Error).
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data object Empty : UiState<Nothing>
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>
}
