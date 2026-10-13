package com.moneymanagement.spendwise.utils

sealed interface UiState<out T>{
    data object Loading: UiState<Nothing>
    data class Success<T>(val data: T): UiState<T>
    data class Error(val message: String): UiState<Nothing>
    object Empty: UiState<Nothing>
}
 inline fun<T> UiState<T>.onSuccess(action:(T)->Unit):UiState<T>{
    if (this is UiState.Success) action(data)
    return this
}
