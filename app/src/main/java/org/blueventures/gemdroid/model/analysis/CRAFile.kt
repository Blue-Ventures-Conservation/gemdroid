package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.mvvm.FileService.sep
import java.io.File

data class CRAFile(
    val storageKey: String? = null,
    val localFile: File? = null,
    val fields: Fields = Fields()
) {
    fun equivalent(o: CRAFile): Boolean {
        val remote = (storageKey != null && storageKey == o.storageKey)
        val local = (localFile != null && localFile.path == o.localFile?.path)
        return remote || local
    }

    fun key(): String {
        return storageKey ?: localFile?.path?.substringAfterLast(sep)?.substringBeforeLast(".") ?: ""
    }

    fun readyToUpload(): Boolean {
        return isLocal() && fields.complete()
    }

    fun isLocal(): Boolean {
        return localFile != null && storageKey == null
    }

    fun isRemote(): Boolean {
        return localFile == null && storageKey != null
    }

    fun badFinalState(): Boolean {
        return (!isRemote() && !readyToUpload()) || (isRemote() && !fields.complete())
    }
}

data class Fields(
    val list: List<String>? = null,
    val numeric: String? = null,
    val string: String? = null
) {
    fun complete(): Boolean {
        return numeric != null && string != null
    }
}
