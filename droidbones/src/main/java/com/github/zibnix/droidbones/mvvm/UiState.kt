package com.github.zibnix.droidbones.mvvm

sealed class UiState<T>(val loading: Boolean = false, val msg: String? = null, val state: T? = null) {
    class Loading<T>(loading: Boolean = true) : UiState<T>(loading = loading)
    class Update<T>(update: T) : UiState<T>(state = update)
    class Message<T>(message: String) : UiState<T>(msg = message)
}
