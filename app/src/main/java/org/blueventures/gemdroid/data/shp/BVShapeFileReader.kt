package org.blueventures.gemdroid.data.shp

import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences
import org.nocrala.tools.gis.data.esri.shapefile.exception.DataStreamEOFException
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.MultiPatchShape
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.MultiPointMShape
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.MultiPointPlainShape
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.MultiPointZShape
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.NullShape
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointMShape
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointShape
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointZShape
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineMShape
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineShape
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineZShape
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil
import java.io.BufferedInputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * The library in use for parsing shapefiles has a bug with PolygonZ and PolygonM types.
 *
 * These types have optional Measures, based on the spec accessed here:
 * https://www.esri.com/content/dam/esrisites/sitecore-archive/Files/Pdfs/library/whitepapers/pdfs/shapefile.pdf
 *
 * We also encountered PolygonZ's in the wild that did not have these Measures sections.
 *
 * The library treats these Measures as always present, and consequently reads past the end of the record and throws off the parsing.
 *
 */
class BVShapeFileReader(private val stream: InputStream, private val rules: ValidationPreferences = ValidationPreferences()): ShapeFileReader(stream, rules) {
    private var eofReached = false

    // Methods
    /**
     * Reads one shape from the InputStream.
     *
     * @return a shape object, or null when the end of the stream is reached. The
     * returned shape object will be of one of the following classes:
     *
     *  * NullShape,
     *  * PointShape,
     *  * PolylineShape,
     *  * PolygonShape,
     *  * MultiPointPlainShape,
     *  * PointZShape,
     *  * PolylineZShape,
     *  * PolygonZShape,
     *  * MultiPointZShape,
     *  * PointMShape,
     *  * PolylineMShape,
     *  * PolygonMShape,
     *  * MultiPointMShape,
     *  * or MultiPatchShape.
     *
     * The method getShapeType() of the AbstractShape object provides the
     * shape type, in order to to cast the object to the appropriate
     * class.
     * @throws InvalidShapeFileException if the data is malformed.
     * @throws IOException               if it's not possible to read from the InputStream.
     */
    @Throws(IOException::class, InvalidShapeFileException::class)
    override fun next(): AbstractShape? {
        if (eofReached) {
            return null
        }
        rules.callPrivateFunc("advanceOneRecordNumber")

        val buf = this.getPrivateProperty<ShapeFileReader, BufferedInputStream>("is") ?: return null

        // Shape header
        val shapeHeader: ShapeHeader?
        val shapeType: ShapeType?
        try {
            shapeHeader = ShapeHeader(buf, rules)
        } catch (e: DataStreamEOFException) {
            eofReached = true
            return null
        }

        // Shape body
        try {
            val typeId = ISUtil.readLeInt(buf)
            if (rules.forceShapeType != null) {
                shapeType = rules.forceShapeType
            } else {
                shapeType = ShapeType.parse(typeId)
                if (shapeType == null) {
                    throw InvalidShapeFileException(
                        "Invalid shape type '" + typeId
                                + "'. " + "The shape type can be forced using "
                                + "the additional constructor with " + "ValidationRules."
                    )
                }
                if (!rules.isAllowMultipleShapeTypes
                    && header.shapeType != shapeType
                ) {
                    throw InvalidShapeFileException(
                        "Invalid shape type '"
                                + shapeType + "'. All included shapes must have the same "
                                + "type as the one specified on the file header ("
                                + header.shapeType
                                + "). This validation can be disabled using the "
                                + "additional constructor with ValidationRules."
                    )
                }
            }
        } catch (e: EOFException) {
            throw InvalidShapeFileException(
                ("Unexpected end of stream. "
                        + "The data is too short for the shape that was being read.")
            )
        }
        try {
            when (shapeType) {
                ShapeType.NULL -> return NullShape(shapeHeader, shapeType, buf, rules)
                ShapeType.POINT -> return PointShape(shapeHeader, shapeType, buf, rules)
                ShapeType.POLYLINE -> return PolylineShape(shapeHeader, shapeType, buf, rules)
                ShapeType.POLYGON -> return PolygonShape(shapeHeader, shapeType, buf, rules)
                ShapeType.MULTIPOINT -> return MultiPointPlainShape(shapeHeader, shapeType, buf, rules)

                ShapeType.POINT_Z -> return PointZShape(shapeHeader, shapeType, buf, rules)
                ShapeType.POLYLINE_Z -> return PolylineZShape(shapeHeader, shapeType, buf, rules)
                ShapeType.POLYGON_Z -> return PolygonOptionalZShape(shapeHeader, shapeType, buf, rules)
                ShapeType.MULTIPOINT_Z -> return MultiPointZShape(shapeHeader, shapeType, buf, rules)
                ShapeType.POINT_M -> return PointMShape(shapeHeader, shapeType, buf, rules)
                ShapeType.POLYLINE_M -> return PolylineMShape(shapeHeader, shapeType, buf, rules)
                ShapeType.POLYGON_M -> return PolygonOptionalMShape(shapeHeader, shapeType, buf, rules)
                ShapeType.MULTIPOINT_M -> return MultiPointMShape(shapeHeader, shapeType, stream, rules)
                ShapeType.MULTIPATCH -> return MultiPatchShape(shapeHeader, shapeType, buf, rules)
                else -> throw InvalidShapeFileException(
                    ("Unexpected shape type '$shapeType'")
                )
            }
        } catch (e: EOFException) {
            throw InvalidShapeFileException(
                ("Unexpected end of stream. "
                        + "The data is too short for the last shape (" + shapeType
                        + ") that was being read.")
            )
        }
    }

    // bad!
    private inline fun <reified T> T.callPrivateFunc(name: String, vararg args: Any?): Any? =
        T::class
            .declaredMemberFunctions
            .firstOrNull { it.name == name }
            ?.apply { isAccessible = true }
            ?.call(this, *args)

    // not again...
    inline fun <reified T : Any, R> T.getPrivateProperty(name: String): R? =
        T::class
            .memberProperties
            .firstOrNull { it.name == name }
            ?.apply { isAccessible = true }
            ?.get(this) as? R
}