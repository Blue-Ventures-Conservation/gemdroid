package org.blueventures.gemdroid.data

import com.github.zibnix.droidbones.NoStack
import com.github.zibnix.droidbones.mvvm.FileService
import com.google.android.gms.maps.model.LatLng
import net.iryndin.jdbf.core.DbfRecord
import net.iryndin.jdbf.reader.DbfReader
import org.blueventures.gemdroid.R
import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.math.abs

object Shapefile {
    fun polygons(workDir: File, files: List<InputStream?>, names: List<String?>, maxPoints: Int = 1000, errId: Int = R.string.please_use_smaller_shp): Result<List<List<LatLng>>> {
        val unzipDir = File(workDir, "shapes_unzip_polygons")
        val pathsResult = unzipOrCopy(unzipDir, files, names)
        
        val polys = mutableListOf<MutableList<LatLng>>()

        var count = 0
        val zipResult = try {
            inspectAndZip(workDir, pathsResult, { null }) { shape, _ ->
                if (shape.shapeType == ShapeType.POLYGON) {
                    val pShape = shape as PolygonShape
                    val pts = mutableListOf<LatLng>()

                    var tooManyPts = false
                    for (i in 0 until pShape.numberOfParts) {
                        val shapePts = pShape.getPointsOfPart(i)
                        count += shapePts.size

                        if (count > maxPoints) {
                            tooManyPts = true
                            break
                        }

                        for (pt in shapePts) {
                            pts.add(toLatLng(pt.x, pt.y))
                        }
                    }

                    if (tooManyPts) {
                        errId
                    } else {
                        if (pts.isNotEmpty()) {
                            if (pts.first() != pts.last()) {
                                pts.add(pts.first())
                            }
                            if (pts.size > 3) polys.add(pts)
                        }
                        null
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

        FileService.deleteDir(unzipDir)

        if (zipResult.isFailure) return Result.failure(zipResult.exceptionOrNull()!!)
        if (polys.isEmpty()) return Result.failure(NoStack(R.string.no_polygons_found))

        return Result.success(polys)
    }

    fun unzipOrCopy(workDir: File, files: List<InputStream?>, names: List<String?>): Result<List<String>> {
        if (files.isEmpty()) return Result.failure(NoStack(R.string.no_file_selected))

        val unzipDirRes = FileService.createDir(workDir)
        if (unzipDirRes.isFailure) {
            return Result.failure(unzipDirRes.exceptionOrNull()!!)
        }

        val unzipDir = unzipDirRes.getOrNull()!!

        return if (files.size == 1) {
            unzip(unzipDir, files[0], names[0])
        } else {
            copyShapes(unzipDir, files, names)
        }
    }

    fun inspectAndZip(workDir: File, pathsResult: Result<List<String>>, nameCheck: (String) -> Throwable?, mapf: (AbstractShape, DbfRecord) -> Int?): Result<File> {
        if (pathsResult.isFailure) {
            return Result.failure(pathsResult.exceptionOrNull()!!)
        }

        val paths = pathsResult.getOrNull()!!

        val badShape = NoStack(R.string.shp_missing_files)
        if (paths.size < 3) {
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
                errId = mapf(s, rec)
                if (errId != null) {
                    break
                }
                s = sr.next()
            }
            shpStream.close()
            dr.close()
        } catch (e: Exception) {
            return Result.failure(e)
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

    private fun copyShapes(dir: File, shps: List<InputStream?>, names: List<String?>): Result<List<String>> {
        val paths = mutableListOf<String>()
        shps.forEachIndexed { i, shp ->
            if (shp == null || names[i] == null) {
                return Result.failure(NoStack(R.string.could_not_read_shps))
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
        val notZip = NoStack(R.string.extract_must_be_zip)
        if (name?.substringAfterLast(".")?.lowercase() != "zip") {
            return Result.failure(notZip)
        }

        if (zip == null) {
            return Result.failure(NoStack(R.string.could_not_open_zip))
        }

        val pathsRes = FileService.unzip(zip, dir.path)
        if (pathsRes.isFailure) {
            return Result.failure(pathsRes.exceptionOrNull()!!)
        }
        return Result.success(pathsRes.getOrNull()!!)
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