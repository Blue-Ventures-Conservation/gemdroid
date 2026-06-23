package org.blueventures.gemdroid.data.shp

import com.github.zibnix.droidbones.NoStack
import com.github.zibnix.droidbones.mvvm.FileService
import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.Json
import net.iryndin.jdbf.core.DbfRecord
import net.iryndin.jdbf.reader.DbfReader
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.data.PolyPts
import org.blueventures.gemdroid.data.polyfile.PolyFile.MAX_VERTICES
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.AbstractPolyShape
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlin.math.abs

data class ClassCount(
    @Json(name = "class_name") val className: String,
    @Json(name = "class_number") var classNumber: Int,
    @Json(name = "cra_count") var craCount: Int,
)

// Saved in Cloud Storage alongside the zip to keep track of
// the parsed/selected fields used for analysis
data class RemoteCRAFileInfo(
    @Json(name = "shp_storage_key") val shapefileStorageKey: String?,
    @Json(name = "json_storage_key") val jsonStorageKey: String?,
    @Json(name = "table_upload_operation_name") val tableUploadOperationName: String,
    @Json(name = "numeric_class_field") val numericClassField: String,
    @Json(name = "string_class_field") val stringClassField: String,
    @Json(name = "string_class_field_values") val stringClassValues: List<String>,
    @Json(name = "class_cra_counts") val classCounts: List<ClassCount>?
) {
    fun storageKey(): String {
        if (shapefileStorageKey != null) return shapefileStorageKey
        return jsonStorageKey!!
    }
    companion object {
        private val adapter = FileService.adapter<RemoteCRAFileInfo>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, info: RemoteCRAFileInfo) = FileService.toFile(file, info, adapter)

        fun polygons(workDir: File, paths: List<String>): Result<MultiPolyPts> {
            val polys = mutableListOf<List<List<LatLng>>>()

            val zipResult = file(workDir, paths, MAX_VERTICES, true, {null}, {null}) { pts ->
                polys.add(pts)
            }
            if (zipResult.isFailure) return Result.failure(zipResult.exceptionOrNull()!!)
            if (polys.isEmpty()) return Result.failure(NoStack(R.string.no_polygons_found))

            return Result.success(polys)
        }

        // this function is exposed primarily for inspecting CRAs
        fun file(workDir: File, paths: List<String>, maxVertices: Int = MAX_VERTICES, mustBeWGS84: Boolean = false, nameCheck: (String) -> Throwable? = { null }, recordf: (DbfRecord) -> Int? = { null }, polyReceiver: (PolyPts) -> Unit = {}): Result<File> {
            return try {
                inspectAndZip(workDir, paths, mustBeWGS84, nameCheck, { shape, record ->
                    var vertexCount = 0
                    var tooManyVertices = false
                    if (shape.shapeType == ShapeType.POLYGON || shape.shapeType == ShapeType.POLYGON_Z || shape.shapeType == ShapeType.POLYGON_M) {
                        val recordErr = recordf(record)
                        if (recordErr == null) {
                            val pShape = shape as AbstractPolyShape
                            val poly = mutableListOf<List<LatLng>>()

                            for (i in 0 until pShape.numberOfParts) {
                                val pts = mutableListOf<LatLng>()
                                val shapePts = pShape.getPointsOfPart(i)
                                vertexCount += shapePts.size

                                if (vertexCount > maxVertices) {
                                    tooManyVertices = true
                                    break
                                }

                                for (pt in shapePts) {
                                    pts.add(toLatLng(pt.x, pt.y))
                                }

                                if (pts.isNotEmpty()) {
                                    if (pts.first() != pts.last()) {
                                        pts.add(pts.first())
                                    }
                                    if (pts.size > 3) {
                                        poly.add(pts)
                                    }
                                }
                            }

                            if (tooManyVertices) {
                                R.string.please_use_smaller_poly_file
                            } else {
                                if (poly.isNotEmpty()) {
                                    polyReceiver(poly)
                                }
                                null
                            }
                        } else {
                            recordErr
                        }
                    } else {
                        R.string.please_use_only_polygons_poly_file
                    }
                }) { atLeastOne ->
                    if (!atLeastOne) R.string.please_use_polygon_poly_file else null
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        private fun inspectAndZip(workDir: File, paths: List<String>, mustBeWGS84: Boolean, nameCheck: (String) -> Throwable?, mapf: (AbstractShape, DbfRecord) -> Int?, finalize: (Boolean) -> Int?): Result<File> {
            val badShape = NoStack(R.string.shp_missing_files)
            if (paths.size < 4) {
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
                }
            }

            if (shp == null || shx == null || dbf == null || prj == null) {
                return Result.failure(badShape)
            }

            if (shpName != shxName || shpName != dbfName || shpName != prjName) {
                return Result.failure(NoStack(R.string.shps_names_must_match))
            }

            val check = nameCheck(shpName)
            if (check != null) {
                return Result.failure(check)
            }

            var errId: Int? = null
            var shpStream: FileInputStream? = null
            var dr: DbfReader? = null

            try {
                val prjBuilder = StringBuilder()
                val prjReader = BufferedReader(InputStreamReader(FileInputStream(prj)))

                var c = prjReader.read()
                while (c != -1) {
                    prjBuilder.append(c.toChar())
                    c = prjReader.read()
                }

                val prjContents = prjBuilder.toString()

                if (mustBeWGS84 && (prjContents.contains("PROJCS", true) || !prjContents.contains("GCS_WGS_1984", true))) {
                    return Result.failure(NoStack(R.string.prj_must_be_wgs_84))
                }

                // these constructors will inspect the file header
                shpStream = FileInputStream(shp)
                val sr = BVShapeFileReader(shpStream)
                dr = DbfReader(FileInputStream(dbf))

                var count = 0
                var s = sr.next()
                var atLeastOne = false
                while(s != null) {
                    count++

                    val rec = dr.read()
                    errId = mapf(s, rec)
                    if (errId != null) {
                        break
                    }
                    atLeastOne = true
                    s = sr.next()
                }

                if (errId == null) {
                    errId = finalize(atLeastOne)
                }
            } catch (e: Exception) {
                return Result.failure(e)
            } finally {
                shpStream?.close()
                dr?.close()
            }

            if (errId != null) {
                return Result.failure(NoStack(errId))
            }

            val zipFile = File(workDir, "$shpName.zip")

            val zipRes = FileService.zip(arrayOf(shp!!, shx!!, dbf!!, prj!!), zipFile.path)
            if (zipRes.isFailure) {
                return Result.failure(zipRes.exceptionOrNull()!!)
            }

            return Result.success(zipFile)
        }

        private fun toLatLng(x: Double, y: Double): LatLng {
            val lat = when {
                y > 90.0 -> 90.0
                y < -90.0 -> -90.0
                else -> y
            }

            var lon = x
            while (abs(lon) > 180.0) {
                lon += if (lon > 0.0) -360.0 else 360.0
            }

            return LatLng(lat, lon)
        }
    }
}