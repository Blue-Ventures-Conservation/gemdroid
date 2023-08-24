package org.blueventures.gemdroid.model.analysis.cra

import com.github.zibnix.droidbones.mvvm.FileService.sep
import org.blueventures.gemdroid.data.analysis.cra.CRA
import org.blueventures.gemdroid.data.analysis.cra.Shapefile
import java.io.File

data class CRAFile(
    val storageKey: String? = null,
    val localFile: File? = null,
    var fields: Fields = Fields(),
    var eeUploadName: String? = null,
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

    companion object {
        fun toCRA(cont: CRAFile, hist: CRAFile?): CRA {
            val contShp = Shapefile(cont.key(), cont.eeUploadName!!, cont.fields.chosenNumeric!!, cont.fields.chosenString!!, cont.fields.chosenStringValues!!)
            val histShp = if (hist == null) null else Shapefile(hist.key(), hist.eeUploadName!!, hist.fields.chosenNumeric!!, hist.fields.chosenString!!, hist.fields.chosenStringValues!!)
            return CRA(contShp, histShp)
        }
    }
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
