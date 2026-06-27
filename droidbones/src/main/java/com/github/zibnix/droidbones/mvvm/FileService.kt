package com.github.zibnix.droidbones.mvvm

import com.github.zibnix.droidbones.NoStack
import com.github.zibnix.droidbones.R
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object FileService {
    val sep: String = File.separator
    val buf: Int = 16_384

    fun getSubdirs(dir: File): List<File> {
        return try {
            val fs = mutableListOf<File>()
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
            val fs = mutableListOf<File>()
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

    fun createDir(parent: File, name: String) = createDir(File(parent, name))

    fun createDir(dir: File): Result<File> {
        return try {
            if (dir.exists()) {
                Result.success(dir)
            } else {
                when (dir.mkdirs()) {
                    true -> Result.success(dir)
                    false -> Result.failure(NoStack(R.string.could_not_create_dir))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createFile(dir: File, child: String): Result<File> {
        return try {
            val f = File(dir, child)

            if (f.exists()) {
                Result.success(f)
            } else {
                when (f.createNewFile()) {
                    true -> Result.success(f)
                    false -> Result.failure(NoStack(R.string.could_not_create_file))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun renameFile(file: File, name: String): Result<Unit> {
        val dir = file.parentFile

        return if (dir == null) {
            Result.failure(NoStack(R.string.could_not_read_parent_dir))
        } else {
            try {
                if (file.renameTo(File(dir, name))) {
                    Result.success(Unit)
                } else {
                    Result.failure(NoStack(R.string.could_not_rename_file))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun deleteFile(file: File): Result<Unit> {
        return try {
            if (!file.exists()) return Result.success(Unit)
            if (file.delete()) {
                Result.success(Unit)
            } else {
                Result.failure(NoStack(R.string.could_not_del_file))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteDir(dir: File): Result<Unit> {
        return try {
            val contents = dir.listFiles()

            var allGone = true
            if (contents != null) {
                for (f in contents) {
                    val res = deleteDir(f)
                    when {
                        res.isFailure -> { allGone = false; break }
                        else -> continue
                    }
                }
            }

            if (!allGone) {
                Result.failure(NoStack(R.string.could_not_del_dir_contents))
            } else {
                when (dir.delete()) {
                    true -> Result.success(Unit)
                    false -> Result.failure(NoStack(R.string.could_not_delete_dir))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun readFile(file: File): Result<ByteArray> {
        return try {
            val b = file.readBytes()
            if (b.isEmpty()) {
                Result.failure(NoStack(R.string.file_was_empty))
            } else {
                Result.success(b)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun writeFile(file: File, b: ByteArray): Result<Unit> {
        return try {
            file.writeBytes(b)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun streamToFile(stream: InputStream, path: String): Result<Unit> {
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
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun zip(files: List<File>, out: File): Result<Unit> {
        val res = createFile(out.parentFile!!, out.name)
        if (res.isFailure) {
            return Result.failure(res.exceptionOrNull()!!)
        }

        return try {
            val dest = FileOutputStream(out)
            val out = ZipOutputStream(BufferedOutputStream(dest))
            val data = ByteArray(buf)
            files.forEach { f ->
                val origin = BufferedInputStream(FileInputStream(f), 8192)
                val entry = ZipEntry(f.name)
                out.putNextEntry(entry)
                var count: Int
                while (origin.read(data, 0, buf).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
                origin.close()
            }
            out.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun unzip(zip: InputStream, path: String): Result<List<String>> {
        return try {
            val zin = ZipInputStream(zip)
            val paths = mutableListOf<String>()
            var ze = zin.nextEntry
            while (ze != null) {
                val dir = File(path)
                val canonicalDirPath = dir.canonicalPath
                val file = File(dir, ze.name)
                val canonicalPath = dir.canonicalPath
                if (!canonicalPath.startsWith(canonicalDirPath)) {
                    throw SecurityException(NoStack(R.string.zip_path_traversal))
                }
                val fpath = path+sep+ze.name
                val fout = FileOutputStream(file)
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
            Result.success(paths)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    inline fun <reified T> fromFile(file: File, adapter: JsonAdapter<T> = adapter()): Result<T> {
        return try {
            val json = file.bufferedReader().use { it.readText() }
            val t = adapter.fromJson(json)
            if (t == null) {
                Result.failure(NoStack(R.string.parsed_null))
            } else {
                Result.success(t)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    inline fun <reified T> toFile(file: File, t: T?, adapter: JsonAdapter<T> = adapter()): Result<Unit> {
        return try {
            val parent = file.parentFile
            if (parent != null) {
                val res = createDir(parent)
                if (res.isFailure) {
                    return Result.failure(res.exceptionOrNull()!!)
                }
            }
            file.writeText(adapter.toJson(t))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    inline fun <reified T> adapter(): JsonAdapter<T> {
        return Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(T::class.java)
    }
}