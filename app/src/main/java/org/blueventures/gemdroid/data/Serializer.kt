package org.blueventures.gemdroid.data

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.JsonAdapter
import java.io.File

abstract class Serializer<T> {
    abstract fun fromFile(file: File): Result<T>
    abstract fun toFile(file: File, data: T): Result<Unit>
    protected inline fun <reified T> make(): JsonAdapter<T> = FileService.adapter()
    protected inline fun <reified T> fromFile(adapter: JsonAdapter<T>, file: File) = FileService.fromFile(file, adapter)
    protected inline fun <reified T> toFile(adapter: JsonAdapter<T>, file: File, data: T) = FileService.toFile(file, data, adapter)
}