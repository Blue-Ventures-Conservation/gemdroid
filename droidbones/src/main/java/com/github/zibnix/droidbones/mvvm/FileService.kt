package com.github.zibnix.droidbones.mvvm

import java.io.File

open class FileService {
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
            when(dir.mkdirs()) {
                true -> dir
                false -> null
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

    fun renameFile(file: File, name: String, ext: String): Boolean {
        val dir = file.parentFile

        return if (dir == null) {
            false
        } else {
            try {
                file.renameTo(File(dir, "$name.$ext"))
            } catch (e: Exception) {
                false
            }
        }
    }

    fun deleteFile(file: File): Boolean {
        return try {
            file.delete()
        } catch(e: Exception) {
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
        } catch(e: Exception) {
            false
        }
    }
}