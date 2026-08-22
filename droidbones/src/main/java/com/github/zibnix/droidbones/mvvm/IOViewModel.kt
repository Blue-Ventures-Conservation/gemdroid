package com.github.zibnix.droidbones.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

open class IOViewModel(private val repo: IORepository): ViewModel() {
    protected fun scoped(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(block = block)
    }
    fun <T> background(work: () -> T, callback: (T) -> Unit = {}) { scoped { repo.background(work).collect(callback) }}
}