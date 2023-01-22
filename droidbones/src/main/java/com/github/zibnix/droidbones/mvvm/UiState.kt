package com.github.zibnix.droidbones.mvvm

sealed class UiState<T : Any>(val loading: Boolean = false, val msg: String? = null, val state: T? = null) {
    class Loading<T : Any>(loading: Boolean) : UiState<T>(loading = loading)
    class Update<T : Any>(update: T) : UiState<T>(state = update)
    class Message<T : Any>(message: String) : UiState<T>(msg = message)
}
