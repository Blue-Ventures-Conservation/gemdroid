package com.github.zibnix.droidbones.mvvm

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object FileService {
    val sep: String = File.separator
    val buf: Int = 16_384

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

    fun streamToFile(stream: InputStream, path: String): Boolean {
        return try {
            val bufStream = BufferedInputStream(stream, 8192)
            val out = BufferedOutputStream(FileOutputStream(path))
            val data = ByteArray(buf)
            var c: Int = bufStream.read(data)
            while (c != -1) {
                out.write(data, 0, c)
                c = bufStream.read(data)
            }
            bufStream.close()
            out.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun zip(files: Array<String>, path: String): Boolean {
        if (createFile(File(path.substringBeforeLast(sep)), path.substringAfterLast(sep)) == null) {
            return false
        }

        return try {
            val dest = FileOutputStream(path)
            val out = ZipOutputStream(BufferedOutputStream(dest))
            val data = ByteArray(buf)
            files.forEach { fpath ->
                val origin = BufferedInputStream(FileInputStream(fpath), 8192)
                val entry = ZipEntry(File(fpath).name)
                out.putNextEntry(entry)
                var count: Int
                while (origin.read(data, 0, buf).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
                origin.close()
            }
            out.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun unzip(zip: InputStream, path: String): List<String>? {
        return try {
            val zin = ZipInputStream(zip)
            val paths = arrayListOf<String>()
            var ze = zin.nextEntry
            while (ze != null) {
                val fpath = path+sep+ze.name
                val fout = FileOutputStream(fpath)
                var c: Int = zin.read()
                while (c != -1) {
                    fout.write(c)
                    c = zin.read()
                }
                zin.closeEntry()
                fout.close()
                paths.add(fpath)
                ze = zin.nextEntry
            }
            zin.close()
            paths
        } catch (e: Exception) {
            null
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