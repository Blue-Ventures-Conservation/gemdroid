package org.blueventures.gemdroid.model.analysis.cra

import com.github.zibnix.droidbones.mvvm.FileService.sep
import java.io.File

data class CRAFile(
    val storageKey: String? = null,
    val localFile: File? = null,
    val fields: Fields = Fields(),
) {
    fun equivalent(o: CRAFile): Boolean {
        val remote = (storageKey != null && storageKey == o.storageKey)
        val local = (localFile != null && localFile.path == o.localFile?.path)
        return remote || local
    }

    fun key() = storageKey ?: localFile?.path?.substringAfterLast(sep)?.substringBeforeLast(".") ?: ""
    fun readyToUpload() = isLocal() && fields.complete()
    fun isLocal() = localFile != null && storageKey == null
    fun isRemote() = localFile == null && storageKey != null
    fun badFinalState() = (!isRemote() && !readyToUpload()) || (isRemote() && !fields.complete())
}

data class Fields(
    val numerics: List<String>? = null,
    val strings: List<String>? = null,
    val stringValues: Map<String, List<String>>? = null,
    val chosenNumeric: String? = null,
    val chosenString: String? = null,
    val chosenStringValues: List<String>? = null,
) {
    fun parsedLocally() = numerics != null && strings != null && stringValues != null
    fun complete() = chosenNumeric != null && chosenString != null && chosenStringValues != null
}
