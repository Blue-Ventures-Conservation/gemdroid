package org.blueventures.gemdroid.model

import com.github.zibnix.droidbones.NoStack

fun <T> resultCheck(r1: Result<T>, r2: Result<T>) = resultCheck(r1) ?: resultCheck(r2)
fun <T> resultCheck(result: Result<T>) = if (result.isFailure) NoStack(result.exceptionOrNull()!!) else null