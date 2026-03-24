package org.blueventures.gemdroid.data.kml

import com.github.zibnix.droidbones.NoStack
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.data.kml.KmlLayer
import com.google.maps.android.data.kml.KmlPolygon
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.data.polyfile.PolyFile.MAX_VERTICES
import java.io.FileInputStream

object KML {
    fun polygons(paths: List<String>): Result<MultiPolyPts> {
        var kmlPath: String? = null
        for(path in paths) {
            when(path.substringAfterLast(".").lowercase()) {
                "kml" -> {
                    kmlPath = path
                    break
                }
            }
        }

        if (kmlPath == null) {
            return Result.failure(NoStack(R.string.missing_kml_file))
        }

        val polys = mutableListOf<List<List<LatLng>>>()

        var kmlStream: FileInputStream? = null
        try {
            kmlStream = FileInputStream(kmlPath)
            val layer = KmlLayer(null, kmlStream, null)
            var atLeastOne = false
            var vertexCount = 0

            for (placemark in layer.placemarks) {
                if (placemark.geometry.geometryType == "Polygon") {
                    atLeastOne = true
                    val kmlPoly = placemark.geometry as KmlPolygon

                    vertexCount += kmlPoly.outerBoundaryCoordinates.size + kmlPoly.innerBoundaryCoordinates.size
                    if (vertexCount > MAX_VERTICES) {
                        return Result.failure(NoStack(R.string.please_use_smaller_poly_file))
                    }

                    val toAdd =  mutableListOf<List<LatLng>>()
                    toAdd.add(kmlPoly.outerBoundaryCoordinates)
                    toAdd.addAll(kmlPoly.innerBoundaryCoordinates)

                    val poly = mutableListOf<List<LatLng>>()
                    for (p in toAdd) {
                        val pts = mutableListOf<LatLng>()
                        pts.addAll(p)

                        if (pts.isNotEmpty()) {
                            if (pts.first() != pts.last()) {
                                pts.add(pts.first())
                            }
                            if (pts.size > 3) {
                                poly.add(pts)
                            }
                        }
                    }

                    polys.add(poly)
                } else {
                    return Result.failure(NoStack(R.string.please_use_only_polygons_poly_file))
                }
            }

            if (!atLeastOne) {
                return Result.failure(NoStack(R.string.please_use_polygon_poly_file))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            kmlStream?.close()
        }

        return Result.success(polys)
    }
}