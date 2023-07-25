package org.blueventures.gemdroid.model

fun <T> resultCheck(r1: Result<T>, r2: Result<T>) = resultCheck(r1) ?: resultCheck(r2)
fun <T> resultCheck(result: Result<T>) = result.exceptionOrNull()