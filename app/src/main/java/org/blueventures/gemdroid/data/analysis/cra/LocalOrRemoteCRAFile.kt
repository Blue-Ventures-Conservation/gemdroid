package org.blueventures.gemdroid.data.analysis.cra

import com.github.zibnix.droidbones.mvvm.FileService.sep
import java.io.File

// This class holds data that may come from local parsing or from fetching details from remote.
data class LocalOrRemoteCRAFile(
    var isShapefile: Boolean = true,
    var eeUploadName: String? = null,
    var overwrite: Boolean = false,
    val storageKey: String? = null,
    val localFile: File? = null,
    val counted: FieldsCounts = FieldsCounts(),
) {
    fun equivalent(o: LocalOrRemoteCRAFile): Boolean {
        val remote = (storageKey != null && storageKey == o.storageKey)
        val local = (localFile != null && localFile.path == o.localFile?.path)
        return remote || local
    }

    fun key() = storageKey ?: localFile?.path?.substringAfterLast(sep)?.substringBeforeLast(".") ?: ""
    fun shpKey() = if (isShapefile) key() else  null
    fun jsonKey() = if (!isShapefile) key() else null
    fun filetypes() = if (isShapefile) "shps" else "geojsons"

    fun readyToUpload() = isLocal() && counted.complete()
    fun isLocal() = localFile != null && storageKey == null
    fun isRemote() = localFile == null && storageKey != null
    fun badFinalState() = (!isRemote() && !readyToUpload()) || (isRemote() && !counted.complete())
}

data class BothFieldsCounted(
    val fields: Fields = Fields(),
    val histCounts: StringsNumerics = StringsNumerics(),
    val contCounts: StringsNumerics = StringsNumerics(),
) {
    fun complete() = fields.complete()
}

data class FieldsCounts(
    var fields: Fields = Fields(),
    var counts: StringsNumerics = StringsNumerics()
) {
    fun parsedLocally() = counts.parsedLocally() && fields.parsedLocally()
    fun complete() = fields.complete()
}

// numericCounts has data only when it has been parsed from a local file
data class StringsNumerics(
    var stringCounts: ClassCounts = ClassCounts(),
    var numericCounts: ClassCounts = ClassCounts(),
) {
    fun parsedLocally() = stringCounts.parsedLocally() && numericCounts.parsedLocally()
}

data class ClassCounts(
    val counts: Map<String, List<ClassCount>> = mapOf(),
    val chosenCounts: List<ClassCount> = listOf(),
) {
    fun parsedLocally() = counts.isNotEmpty()

    fun getChosenCounts(chosen: String, nums: Map<String, Int>?): List<ClassCount> {
        val count = counts[chosen]!!

        if (nums != null) {
            for (cc in count) {
                if (cc.classNumber == Int.MIN_VALUE) {
                    cc.classNumber = nums[cc.className]!!
                }
            }
        }

        return count.sortedBy { it.classNumber }
    }

    fun getChosenNumerics(chosen: String) = counts[chosen]!!.map { it.classNumber }
}

data class Fields(
    // locally parsed
    val numerics: List<String>? = null,
    val strings: List<String>? = null,
    val stringValues: Map<String, List<String>>? = null,
    val numericValues: Map<String, List<Int>>? = null,
    // locally parsed and stored remotely
    val chosenNumeric: String? = null,
    val chosenString: String? = null,
    val chosenStringValues: List<String>? = null,
) {
    fun parsedLocally() = numerics != null && strings != null && stringValues != null && numericValues != null
    fun complete() = chosenNumeric != null && chosenString != null && chosenStringValues != null
}
