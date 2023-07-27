package com.github.zibnix.droidbones.mvvm

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

open class IORepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    protected fun <T> goFlow(emitter: suspend () -> T) = flow {
        emit(emitter())
    }.flowOn(ioDispatcher)
}