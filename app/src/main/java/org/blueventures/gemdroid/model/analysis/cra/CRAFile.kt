package org.blueventures.gemdroid.model.analysis.cra

import com.github.zibnix.droidbones.mvvm.FileService.sep
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.shp.ClassCount
import org.blueventures.gemdroid.data.shp.Shapefile
import java.io.File

data class CRAFile(
    val storageKey: String? = null,
    var eeUploadName: String? = null,
    val localFile: File? = null,
    val counted: FieldsCounts = FieldsCounts(),
) {
    fun equivalent(o: CRAFile): Boolean {
        val remote = (storageKey != null && storageKey == o.storageKey)
        val local = (localFile != null && localFile.path == o.localFile?.path)
        return remote || local
    }

    fun key() = storageKey ?: localFile?.path?.substringAfterLast(sep)?.substringBeforeLast(".") ?: ""
    fun readyToUpload() = isLocal() && counted.complete()
    fun isLocal() = localFile != null && storageKey == null
    fun isRemote() = localFile == null && storageKey != null
    fun badFinalState() = (!isRemote() && !readyToUpload()) || (isRemote() && !counted.complete())

    companion object {
        fun toCRA(cont: CRAFile, hist: CRAFile?): CRA {
            val histShp = if (hist == null) null else Shapefile(
                hist.key(),
                hist.eeUploadName!!,
                hist.counted.fields.chosenNumeric!!,
                hist.counted.fields.chosenString!!,
                hist.counted.fields.chosenStringValues!!,
                hist.counted.counts.stringCounts.chosenCounts
            )
            val contShp = Shapefile(
                cont.key(),
                cont.eeUploadName!!,
                cont.counted.fields.chosenNumeric!!,
                cont.counted.fields.chosenString!!,
                cont.counted.fields.chosenStringValues!!,
                cont.counted.counts.stringCounts.chosenCounts
            )
            return CRA(histShp, contShp)
        }
    }
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

    fun getChosen(chosen: String, nums: Map<String, Int>): List<ClassCount> {
        val count = counts[chosen]!!

        for (cc in count) {
            val num = nums[cc.className]!!
            cc.classNumber = num
        }

        return count.sortedBy { it.classNumber }
    }

    fun getChosenNumericValues(chosen: String): List<String> {
        val count = counts[chosen]!!

        for (cc in count) {
            cc.classNumber = cc.className.toInt()
        }

        return count.sortedBy { it.classNumber }.map { it.className }
    }
}

data class Fields(
    val numerics: List<String>? = null,
    val strings: List<String>? = null,
    val stringValues: Map<String, List<String>>? = null,
    val numericValues: Map<String, List<String>>? = null,
    val chosenNumeric: String? = null,
    val chosenString: String? = null,
    val chosenStringValues: List<String>? = null,
) {
    fun parsedLocally() = numerics != null && strings != null && stringValues != null && numericValues != null
    fun complete() = chosenNumeric != null && chosenString != null && chosenStringValues != null
}
