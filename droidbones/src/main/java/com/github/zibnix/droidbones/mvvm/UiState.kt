package com.github.zibnix.droidbones.mvvm

sealed class UiState<T>(val loading: Boolean = false, val msg: String? = null, val value: T? = null) {
    class Loading<T>(loading: Boolean = true, value: T? = null) : UiState<T>(loading = loading, value = value)
    class Update<T>(update: T) : UiState<T>(value = update)
    class Message<T>(message: String, value: T? = null) : UiState<T>(msg = message, value = value)
}
