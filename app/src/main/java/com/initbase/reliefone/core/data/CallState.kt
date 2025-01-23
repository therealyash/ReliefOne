package com.initbase.reliefone.core.data

sealed class CallState<out T> {
    data class Success<out T>(val data: T) : CallState<T>()
    data class Error(val error: Exception) : CallState<Nothing>()
    object Loading : CallState<Nothing>()
    object Initial : CallState<Nothing>()
}