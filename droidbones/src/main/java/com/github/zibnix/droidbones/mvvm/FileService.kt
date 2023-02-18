package com.github.zibnix.droidbones.mvvm

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

object FileService {
    val sep: String = File.separator

    fun getSubdirs(dir: File): List<File> {
        return try {
            val fs = arrayListOf<File>()
            for (file in dir.listFiles() ?: emptyArray()) {
                if (file.exists() && file.isDirectory) {
                    fs.add(file)
                }
            }

            fs.sortedByDescending { file ->
                file.lastModified()
            }
        } catch (e: Exception) {
            listOf()
        }
    }

    fun getFiles(dir: File): List<File> {
        return try {
            val fs = arrayListOf<File>()
            for (file in dir.listFiles() ?: emptyArray()) {
                if (file.exists() && !file.isDirectory && file.length() > 0) {
                    fs.add(file)
                }
            }

            fs.sortedByDescending { file ->
                file.lastModified()
            }
        } catch (e: Exception) {
            listOf()
        }
    }

    fun createDir(parent: File, name: String): File? {
        return try {
            val dir = File(parent, name)

            if (dir.exists()) {
                dir
            } else {
                when (dir.mkdirs()) {
                    true -> dir
                    false -> null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun createFile(dir: File, child: String): File? {
        return try {
            val f = File(dir, child)

            if (f.exists()) {
                f
            } else {
                when (f.createNewFile()) {
                    true -> f
                    false -> null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun renameFile(file: File, name: String): Boolean {
        val dir = file.parentFile

        return if (dir == null) {
            false
        } else {
            try {
                file.renameTo(File(dir, name))
            } catch (e: Exception) {
                false
            }
        }
    }

    fun deleteFile(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    fun deleteDir(dir: File): Boolean {
        return try {
            val contents = dir.listFiles()

            var allGone = true
            if (contents != null) {
                for (f in contents) {
                    if (!deleteDir(f)) {
                        allGone = false
                    }
                }
            }

            if (!allGone) {
                false
            } else {
                dir.delete()
            }
        } catch (e: Exception) {
            false
        }
    }

    fun readFile(file: File): ByteArray? {
        return try {
            val b = file.readBytes()
            if (b.isEmpty()) {
                null
            } else {
                b
            }
        } catch (e: Exception) {
            null
        }
    }

    fun writeFile(file: File, b: ByteArray): Boolean {
        return try {
            file.writeBytes(b)
            true
        } catch (e: Exception) {
            false
        }
    }

    inline fun <reified T> fromFile(file: File, adapter: JsonAdapter<T> = adapter()): T? {
        return try {
            val json = file.bufferedReader().use { it.readText() }
            adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    inline fun <reified T> toFile(file: File, t: T?, adapter: JsonAdapter<T> = adapter()): Boolean {
        return try {
            file.writeText(adapter.toJson(t))
            true
        } catch (e: Exception) {
            false
        }
    }

    inline fun <reified T> adapter(): JsonAdapter<T> {
        return Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(T::class.java)
    }
}