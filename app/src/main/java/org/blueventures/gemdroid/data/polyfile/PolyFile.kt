package org.blueventures.gemdroid.data.polyfile

import com.github.zibnix.droidbones.NoStack
import com.github.zibnix.droidbones.mvvm.FileService
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.data.kml.KML
import org.blueventures.gemdroid.data.shp.RemoteCRAFileInfo
import java.io.File
import java.io.InputStream

object PolyFile {
    const val MAX_VERTICES = 100_000

    fun polygons(workDir: File, files: List<InputStream?>, names: List<String?>): Result<MultiPolyPts> {
        val unzipDir = File(workDir, "shapes_unzip_polygons")
        val pathsResult = unzipOrCopy(unzipDir, files, names)
        if (pathsResult.isFailure) {
            return Result.failure(pathsResult.exceptionOrNull()!!)
        }

        val paths = pathsResult.getOrNull()!!

        val typeResult = kmlOrShapefile(paths)
        if (typeResult.isFailure) {
            return Result.failure(typeResult.exceptionOrNull()!!)
        }

        val polyResult = when(typeResult.getOrNull()!!) {
            PolygonFileType.KML -> KML.polygons(paths)
            PolygonFileType.SHAPEFILE -> RemoteCRAFileInfo.polygons(workDir, paths)
        }

        FileService.deleteDir(unzipDir)

        return polyResult
    }

    enum class PolygonFileType {
        KML, SHAPEFILE
    }

    fun kmlOrShapefile(paths: List<String>): Result<PolygonFileType> {
        for (path in paths) {
            when(path.substringAfterLast(".").lowercase()) {
                "kml" -> return Result.success(PolygonFileType.KML)
                "shp" -> return Result.success(PolygonFileType.SHAPEFILE)
            }
        }

        return Result.failure(NoStack(R.string.unrecognized_file_type))
    }

    fun unzipOrCopy(workDir: File, files: List<InputStream?>, names: List<String?>): Result<List<String>> {
        if (files.isEmpty()) return Result.failure(NoStack(R.string.no_file_selected))

        val unzipDirRes = FileService.createDir(workDir)
        if (unzipDirRes.isFailure) {
            return Result.failure(unzipDirRes.exceptionOrNull()!!)
        }

        val unzipDir = unzipDirRes.getOrNull()!!

        return if (files.size == 1) {
            val fileExt = names[0]?.substringAfterLast(".")?.lowercase()
            when(fileExt) {
                "zip" -> unzip(unzipDir, files[0])
                "kmz" -> unzip(unzipDir, files[0])
                "kml" -> copyShapes(unzipDir, files, names)
                else ->Result.failure(NoStack(R.string.unrecognized_single_file))
            }
        } else {
            copyShapes(unzipDir, files, names)
        }
    }

    private fun copyShapes(dir: File, files: List<InputStream?>, names: List<String?>): Result<List<String>> {
        val paths = mutableListOf<String>()
        files.forEachIndexed { i, file ->
            if (file == null || names[i] == null) {
                return Result.failure(NoStack(R.string.could_not_read_poly_files))
            }
            val path = File(dir, names[i]!!).path
            val streamRes = FileService.streamToFile(file, path)
            if (streamRes.isFailure) {
                return Result.failure(streamRes.exceptionOrNull()!!)
            }

            paths.add(path)
        }

        return Result.success(paths)
    }

    private fun unzip(dir: File, zip: InputStream?): Result<List<String>> {
        if (zip == null) {
            return Result.failure(NoStack(R.string.could_not_open_archive))
        }

        val pathsRes = FileService.unzip(zip, dir.path)
        if (pathsRes.isFailure) {
            return Result.failure(pathsRes.exceptionOrNull()!!)
        }
        return Result.success(pathsRes.getOrNull()!!)
    }
}