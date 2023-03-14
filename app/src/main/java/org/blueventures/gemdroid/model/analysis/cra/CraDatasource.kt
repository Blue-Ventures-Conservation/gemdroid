package org.blueventures.gemdroid.model.analysis.cra

import android.net.Uri
import com.github.zibnix.droidbones.mvvm.FileService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import net.iryndin.jdbf.core.DbfFieldTypeEnum
import net.iryndin.jdbf.reader.DbfReader
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.Shapefile
import org.blueventures.gemdroid.model.SignIn
import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.concurrent.atomic.AtomicInteger

class CraDatasource(
    private val storage: FirebaseStorage = Firebase.storage,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    fun getRemoteCRAs(callback: (Result<List<String>>) -> Unit) {
        auth.currentUser?.uid?.let { uid ->
            storage.reference.child("users/$uid/shps").listAll()
                .addOnSuccessListener { result ->
                    val files = arrayListOf<String>()
                    result.items.forEach {
                        if (it.name.endsWith(".json")) {
                            files.add(it.name.substringBeforeLast("."))
                        }
                    }
                    callback(Result.success(files))
                }
                .addOnFailureListener {
                    callback(Result.failure(Throwable(it)))
                }
        } ?: run {
            callback(Result.failure(SignIn.not))
        }
    }

    fun validateLocalCRA(roiDir: File, files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?): Result<CRAFile> {
        val craDirRes = FileService.createDir(roiDir, crasDir)
        if (craDirRes.isFailure) {
            return Result.failure(craDirRes.exceptionOrNull()!!)
        }

        val crasDir = craDirRes.getOrNull()!!

        val unzipDirRes = FileService.createDir(crasDir, crasUnzipDir)
        if (unzipDirRes.isFailure) {
            return Result.failure(unzipDirRes.exceptionOrNull()!!)
        }

        val unzipDir = unzipDirRes.getOrNull()!!

        return when {
            files.isEmpty() -> Result.failure(Throwable("No file selected."))
            files.size == 1 -> validateShapes(crasDir, remoteCRAs, previous, unzip(unzipDir, files[0], names[0]))
            else -> validateShapes(crasDir, remoteCRAs, previous, copyShapes(unzipDir, files, names))
        }
    }

    private fun copyShapes(dir: File, shps: List<InputStream?>, names: List<String?>): Result<List<String>> {
        val paths = arrayListOf<String>()
        shps.forEachIndexed { i, shp ->
            if (shp == null || names[i] == null) {
                return Result.failure(Throwable("Could not read shapefiles"))
            }
            val path = File(dir, names[i]!!).path
            val streamRes = FileService.streamToFile(shp, path)
            if (streamRes.isFailure) {
                return Result.failure(streamRes.exceptionOrNull()!!)
            }

            paths.add(path)
        }

        return Result.success(paths)
    }

    private fun unzip(dir: File, zip: InputStream?, name: String?): Result<List<String>> {
        val notZip = Throwable("If one file is selected, it must be a .zip")
        if (name?.substringAfterLast(".")?.lowercase() != "zip") {
            return Result.failure(notZip)
        }

        if (zip == null) {
            return Result.failure(Throwable("Could not open selected .zip for validation"))
        }

        val pathsRes = FileService.unzip(zip, dir.path)
        if (pathsRes.isFailure) {
            return Result.failure(pathsRes.exceptionOrNull()!!)
        }
        return Result.success(pathsRes.getOrNull()!!)
    }

    private fun validateShapes(crasDir: File, remoteCRAs: List<String>, previous: String?, pathsResult: Result<List<String>>): Result<CRAFile> {
        if (pathsResult.isFailure) {
            return Result.failure(pathsResult.exceptionOrNull()!!)
        }

        val paths = pathsResult.getOrNull()!!

        val badShape = Throwable("Your shapefile must include a .shp, .shx, .dbf and .prj")
        if (paths.size != 4) {
            return Result.failure(badShape)
        }

        var shp: String? = null
        var shpName = "shp"
        var shx: String? = null
        var shxName = "shx"
        var dbf: String? = null
        var dbfName = "dbf"
        var prj: String? = null
        var prjName = "prj"

        paths.forEach { path ->
            when(path.substringAfterLast(".").lowercase()) {
                "shp" -> {
                    shp = path
                    shpName = path.substringAfterLast(FileService.sep).substringBeforeLast(".")
                }
                "shx" -> {
                    shx = path
                    shxName = path.substringAfterLast(FileService.sep).substringBeforeLast(".")
                }
                "dbf" -> {
                    dbf = path
                    dbfName = path.substringAfterLast(FileService.sep).substringBeforeLast(".")
                }
                "prj" -> {
                    prj = path
                    prjName = path.substringAfterLast(FileService.sep).substringBeforeLast(".")
                }
                else -> {
                    return Result.failure(badShape)
                }
            }
        }

        if (shp == null || shx == null || dbf == null || prj == null) {
            return Result.failure(badShape)
        }

        if (shpName != shxName || shpName != dbfName || shpName != prjName) {
            return Result.failure(Throwable("Shapefiles should all have the same name."))
        }

        if (previous != null && previous == shpName) {
            return Result.failure(Throwable("Please select two different shapefiles."))
        }

        if (remoteCRAs.contains(shpName)) {
            return Result.failure(Throwable("Please use the previously uploaded shapefile by that name."))
        }

        val numerics = arrayListOf<String>()
        val strings = arrayListOf<String>()
        val numericsMap = mutableMapOf<String, MutableMap<String, Int>>()
        val stringsMap = mutableMapOf<String, MutableMap<String, Int>>()

        try {
            // these constructors will inspect the file header
            val shpStream = FileInputStream(shp)
            val sr = ShapeFileReader(shpStream)
            val dr = DbfReader(FileInputStream(dbf))

            var count = 0
            var s = sr.next()
            while(s != null) {
                count++

                val rec = dr.read()
                rec.fields.forEach { field ->
                    val name = field.name
                    when(field.type) {
                        DbfFieldTypeEnum.Numeric -> {
                            addToMap(numericsMap, name, rec.getString(name).toFloat().toInt().toString())
                        }
                        DbfFieldTypeEnum.Character -> {
                            addToMap(stringsMap, name, rec.getString(name))
                        }
                        else -> {}
                    }
                }

                s = sr.next()
            }
            shpStream.close()
            dr.close()

            numericsMap.forEach { (nf, nc) ->
                if (nc.size < count) {
                    stringsMap.forEach { (sf, sc) ->
                        if (nc.size == sc.size) {
                            numerics.add(nf)
                            strings.add(sf)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (numerics.size <= 0) {
            return Result.failure(Throwable("Shapefile has no candidate fields for the numeric class field."))
        }

        if (strings.size <= 0) {
            return Result.failure(Throwable("Shapefile has no candidate fields for the character class field."))
        }

        val zipFile = File(crasDir, "$shpName.zip")

        val zipRes = FileService.zip(arrayOf(shp!!, shx!!, dbf!!, prj!!), zipFile.path)
        if (zipRes.isFailure) {
            return Result.failure(zipRes.exceptionOrNull()!!)
        }

        FileService.deleteDir(File(crasDir, crasUnzipDir))

        val stringValues = mutableMapOf<String, List<String>>()
        stringsMap.forEach { (k, v) ->
            stringValues[k] = v.keys.toList()
        }

        return Result.success(CRAFile(
            localFile = zipFile,
            fields = Fields(numerics, strings, stringValues))
        )
    }

    private fun addToMap(m: MutableMap<String, MutableMap<String, Int>>, field: String, value: String) {
        var fieldMap = m[field]
        if (fieldMap == null) {
            fieldMap = mutableMapOf()
            m[field] = fieldMap
        }
        val count = fieldMap[value]
        if (count == null) {
            fieldMap[value] = 1
        } else {
            fieldMap[value] = count + 1
        }
    }

    fun getCRAFields(cont: CRAFile, hist: CRAFile?, callback: (Result<Fields>) -> Unit) {
        if (hist == null || cont.equivalent(hist)) {
            craFields(cont, callback)
            return
        }

        val successes = AtomicInteger()
        val failures = AtomicInteger()
        var contFields: Fields? = null
        var histFields: Fields? = null

        val handleErr: (Result<Fields>) -> Boolean = { result ->
            if (result.isFailure && failures.addAndGet(1) == 1) {
                callback(result)
                true
            } else {
                false
            }
        }

        craFields(cont) { result ->
            if (!handleErr(result)) {
                contFields = result.getOrNull()
                if (successes.addAndGet(1) == 2) {
                    mergeNullFields(contFields, histFields, callback)
                }
            }
        }

        craFields(hist) { result ->
            if (!handleErr(result)) {
                histFields = result.getOrNull()
                if (successes.addAndGet(1) == 2) {
                    mergeNullFields(histFields, contFields, callback)
                }
            }
        }
    }

    private fun mergeNullFields(f1: Fields?, f2: Fields?, callback: (Result<Fields>) -> Unit) {
        if (f1 == null || f2 == null) {
            return
        }

        callback(mergeFields(f1, f2))
    }

    private fun mergeFields(f1: Fields, f2: Fields): Result<Fields> {
        val mismatch = Throwable("Please select shapefiles that have matching fields")
        return when {
            f1.complete() && f2.complete() -> {
                if (f1.chosenNumeric == f2.chosenNumeric && f1.chosenString == f2.chosenString) {
                    Result.success(f1)
                } else {
                    Result.failure(mismatch)
                }
            }
            f1.complete() && !f2.complete() -> {
                if (f2.numerics!!.contains(f1.chosenNumeric) && f2.strings!!.contains(f1.chosenString)) {
                    Result.success(f1)
                } else {
                    Result.failure(mismatch)
                }
            }
            f2.complete() && !f1.complete() -> {
                if (f1.numerics!!.contains(f2.chosenNumeric) && f1.strings!!.contains(f2.chosenString)) {
                    Result.success(f2)
                } else {
                    Result.failure(mismatch)
                }
            }
            !f1.complete() && !f2.complete() -> {
                val n1 = f1.numerics!!
                val n2 = f2.numerics!!
                val s1 = f1.strings!!
                val s2 = f2.strings!!
                if (n1.size == n2.size && n1.containsAll(n2) && s1.size == s2.size && s1.containsAll(s2)) {
                    Result.success(f1)
                } else {
                    Result.failure(mismatch)
                }
            }
            else -> {
                // should not be reachable
                Result.failure(Throwable("Unreachable error encountered..."))
            }
        }
    }

    private fun craFields(cra: CRAFile, callback: (Result<Fields>) -> Unit) {
        if (cra.fields.parsedLocally()) {
            callback(Result.success(cra.fields))
        } else {
            cra.storageKey?.let { key ->
                auth.currentUser?.uid?.let { uid ->
                    try {
                        val tmp = File.createTempFile("cras", "json")
                        storage.reference.child("users/$uid/shps/$key.json").getFile(tmp).addOnSuccessListener {
                            val shpRes = Shapefile.fromFile(tmp)
                            if (shpRes.isFailure) {
                                callback(Result.failure(shpRes.exceptionOrNull()!!))
                            } else {
                                val shp = shpRes.getOrNull()!!
                                callback(Result.success(
                                    Fields(
                                        chosenNumeric = shp.numericClassField,
                                        chosenString = shp.stringClassField,
                                        chosenStringValues = shp.stringClassValues
                                    )
                                ))
                            }
                        }.addOnFailureListener {
                            callback(Result.failure(Throwable(it)))
                        }
                    } catch (e: Exception) {
                        callback(Result.failure(Throwable(e)))
                    }
                } ?: run {
                    callback(Result.failure(SignIn.not))
                }
            } ?: run {
                callback(Result.failure(Throwable("Internal storage key error, sorry!")))
            }
        }
    }

    fun uploadCRAs(c1: CRAFile, c2: CRAFile, callback: (Result<Unit>) -> Unit) {
        val successes = AtomicInteger()
        val failures = AtomicInteger()

        val handleErr: (Result<Unit>) -> Boolean = { result ->
            if (result.isFailure && failures.addAndGet(1) == 1) {
                callback(result)
                true
            } else {
                false
            }
        }

        uploadCRA(c1) { result ->
            if (!handleErr(result)) {
                if (successes.addAndGet(1) == 2) {
                    callback(Result.success(Unit))
                }
            }
        }

        uploadCRA(c2) { result ->
            if (!handleErr(result)) {
                if (successes.addAndGet(1) == 2) {
                    callback(Result.success(Unit))
                }
            }
        }
    }

    fun uploadCRA(cra: CRAFile, callback: (Result<Unit>) -> Unit) {
        if (cra.localFile == null || cra.fields.chosenNumeric == null || cra.fields.chosenString == null) {
            callback(Result.failure(Throwable("Internal shapefile error, sorry!")))
        }

        val key = cra.key()
        val zip = cra.localFile!!
        uploadShapefile(key, zip) { result ->
            when {
                result.isSuccess -> uploadFields(key, cra.fields.chosenNumeric!!, cra.fields.chosenString!!, cra.fields.chosenStringValues!!, callback)
                result.isFailure -> callback(result)
            }
        }
    }

    private fun uploadShapefile(key: String, zip: File, callback: (Result<Unit>) -> Unit) {
        auth.currentUser?.uid?.let { uid ->
            storage.reference.child("users/$uid/shps/$key.zip").putFile(Uri.fromFile(zip))
                .addOnSuccessListener {
                    callback(Result.success(Unit))
                }.addOnFailureListener {
                    callback(Result.failure(Throwable(it)))
                }
        } ?: run {
            callback(Result.failure(SignIn.not))
        }
    }

    private fun uploadFields(key: String, numeric: String, string: String, stringVals: List<String>, callback: (Result<Unit>) -> Unit) {
        auth.currentUser?.uid?.let { uid ->
            try {
                val tmp = File.createTempFile(key, "json")
                val shpRes = Shapefile.toFile(tmp, Shapefile(key, numeric, string, stringVals))
                if (shpRes.isFailure) {
                    callback(shpRes)
                } else {
                    storage.reference.child("users/$uid/shps/$key.json").putFile(Uri.fromFile(tmp))
                        .addOnSuccessListener {
                            callback(Result.success(Unit))
                        }.addOnFailureListener {
                            callback(Result.failure(Throwable(it)))
                        }
                }
            } catch (e: Exception) {
                callback(Result.failure(Throwable(e)))
            }
        } ?: run {
            callback(Result.failure(SignIn.not))
        }
    }

    fun saveCRAs(roiDir: File, cra: CRA): Result<Unit> {
        val crasDirRes = FileService.createDir(roiDir, crasDir)
        if (crasDirRes.isFailure) {
            return Result.failure(crasDirRes.exceptionOrNull()!!)
        }
        return CRA.toFile(File(crasDirRes.getOrNull()!!, crasFile), cra)
    }

    companion object {
        const val crasDir = "cras"
        const val crasUnzipDir = "unzip"
        const val crasFile = "cras.json"
    }
}