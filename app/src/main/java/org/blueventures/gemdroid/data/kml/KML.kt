package org.blueventures.gemdroid.data.kml

import com.github.zibnix.droidbones.NoStack
import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.data.polyfile.PolyFile.MAX_VERTICES
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

object KML {
    data class PolygonData(
        val outerBoundary: List<LatLng> = emptyList(),
        val holes: List<List<LatLng>> = emptyList()
    )

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
        var bis: BufferedInputStream? = null
        try {
            kmlStream = FileInputStream(kmlPath)
            bis = BufferedInputStream(kmlStream)
            val polysResult = parse(bis)
            if (polysResult.isFailure) return Result.failure(polysResult.exceptionOrNull()!!)
            val placemarks = polysResult.getOrNull()!!

            var vertexCount = 0

            for (kmlPoly in placemarks) {
                vertexCount += kmlPoly.outerBoundary.size + kmlPoly.holes.size
                if (vertexCount > MAX_VERTICES) {
                    return Result.failure(NoStack(R.string.please_use_smaller_poly_file))
                }

                val toAdd =  mutableListOf<List<LatLng>>()
                toAdd.add(kmlPoly.outerBoundary)
                toAdd.addAll(kmlPoly.holes)

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
            }
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            kmlStream?.close()
            bis?.close()
        }

        return Result.success(polys)
    }

    private const val PLACEMARK: String = "Placemark"
    private const val GEOMETRY_REGEX: String = "Point|LineString|Polygon|MultiGeometry|Track|MultiTrack"
    private const val BOUNDARY_REGEX: String = "outerBoundaryIs|innerBoundaryIs"
    private const val LONGITUDE_INDEX: Int = 0
    private const val LATITUDE_INDEX: Int = 1
    private const val LAT_LNG_ALT_SEPARATOR: String = ","

    private const val UNSUPPORTED_REGEX: String = "altitude|altitudeModeGroup|altitudeMode|" +
            "begin|bottomFov|cookie|displayName|displayMode|end|expires|extrude|" +
            "flyToView|gridOrigin|httpQuery|leftFov|linkDescription|linkName|linkSnippet|" +
            "listItemType|maxSnippetLines|maxSessionLength|message|minAltitude|minFadeExtent|" +
            "minLodPixels|minRefreshPeriod|maxAltitude|maxFadeExtent|maxLodPixels|maxHeight|" +
            "maxWidth|near|NetworkLink|NetworkLinkControl|overlayXY|range|refreshMode|" +
            "refreshInterval|refreshVisibility|rightFov|roll|rotationXY|screenXY|shape|sourceHref|" +
            "state|targetHref|tessellate|tileSize|topFov|viewBoundScale|viewFormat|viewRefreshMode|" +
            "viewRefreshTime|when"

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parse(stream: InputStream): Result<List<PolygonData>> {
        val parser = createXmlParser(stream)
        var eventType = parser.eventType
        val polys = mutableListOf<PolygonData>()

        while(eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.name.matches(UNSUPPORTED_REGEX.toRegex())) {
                    skip(parser)
                }
                if (parser.name.equals(PLACEMARK)) {
                    val polyResult = createPlacemark(parser)
                    if (polyResult.isFailure) return Result.failure(polyResult.exceptionOrNull()!!)
                    polys.add(polyResult.getOrNull()!!)
                }
            }
            eventType = parser.next()
        }

        if (polys.isEmpty()) {
            return Result.failure(NoStack(R.string.please_use_polygon_poly_file))
        }

        return Result.success(polys)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    fun createPlacemark(parser: XmlPullParser): Result<PolygonData> {
        var eventType = parser.eventType

        while (!(eventType == XmlPullParser.END_TAG && parser.name == "Placemark")) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.name.matches(GEOMETRY_REGEX.toRegex())) {
                    return createGeometry(parser, parser.name)
                }
            }
            eventType = parser.next()
        }

        return Result.failure(NoStack(R.string.please_use_only_polygons_poly_file))
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun createGeometry(parser: XmlPullParser, geometryType: String): Result<PolygonData> {
        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG && parser.name == geometryType)) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.name == "Polygon") {
                    return Result.success(createPolygon(parser))
                }
            }
            eventType = parser.next()
        }

        return Result.failure(NoStack(R.string.please_use_only_polygons_poly_file))
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun createPolygon(parser: XmlPullParser): PolygonData {
        // Indicates if an outer boundary needs to be defined
        var isOuterBoundary = false
        var outerBoundary = mutableListOf<LatLng>()
        val innerBoundaries = mutableListOf<List<LatLng>>()
        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "Polygon")) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.name.matches(BOUNDARY_REGEX.toRegex())) {
                    isOuterBoundary = parser.name == "outerBoundaryIs"
                } else if (parser.name == "coordinates") {
                    if (isOuterBoundary) {
                        outerBoundary = convertToLatLngList(parser.nextText())
                    } else {
                        innerBoundaries.add(convertToLatLngList(parser.nextText()))
                    }
                }
            }
            eventType = parser.next()
        }
        return PolygonData(outerBoundary, innerBoundaries)
    }

    private fun convertToLatLngList(coordinatesString: String): MutableList<LatLng> {
        val latLngAltsArray = mutableListOf<LatLng>()
        // Need to trim to avoid whitespace around the coordinates such as tabs
        val coordinates = coordinatesString.trim { it <= ' ' }.split("(\\s+)".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (coordinate in coordinates) {
            latLngAltsArray.add(convertToLatLng(coordinate, LAT_LNG_ALT_SEPARATOR))
        }
        return latLngAltsArray
    }

    private fun convertToLatLng(coordinateString: String, separator: String): LatLng {
        val coordinate = coordinateString.split(separator)

        val lat = coordinate[LATITUDE_INDEX].toDouble()
        val lon = coordinate[LONGITUDE_INDEX].toDouble()
        return LatLng(lat, lon)
    }

    @Throws(XmlPullParserException::class)
    private fun createXmlParser(stream: InputStream): XmlPullParser {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(stream, null)
        return parser
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun skip(parser: XmlPullParser) {
        check(parser.eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}