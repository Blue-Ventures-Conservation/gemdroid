package org.blueventures.gemdroid.data.shp

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException
import org.nocrala.tools.gis.data.esri.shapefile.shape.Const
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.AbstractPolyShape
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil
import java.io.InputStream

abstract class AbstractPolyOptionalZShape(shapeHeader: ShapeHeader, shapeType: ShapeType, stream: InputStream, rules: ValidationPreferences): AbstractPolyShape(shapeHeader, shapeType, stream, rules) {
    private var minZ = 0.0
    private var maxZ = 0.0
    private var z: DoubleArray = DoubleArray(0)

    private var minM = 0.0
    private var maxM = 0.0
    private var measures: DoubleArray = DoubleArray(0)
    init {
        val minLen = Cursor.zPolyMinLength(numberOfParts, numberOfPoints)
        val maxLen = Cursor.zPolyMaxLength(numberOfParts, numberOfPoints)
        val isMin = header.contentLength == minLen
        val isMax = header.contentLength == maxLen

        if (!rules.isAllowBadContentLength && !isMin && !isMax) {
            throw InvalidShapeFileException(
                "Invalid " + shapeTypeName
                        + " shape header's content length. " + "Expected " + minLen + " or " + maxLen
                        + " 16-bit words (for " + numberOfParts + " parts and "
                        + numberOfPoints + " points)" + " but found "
                        + header.contentLength + ". " + Const.PREFERENCES
            )
        }

        minZ = ISUtil.readLeDouble(stream)
        maxZ = ISUtil.readLeDouble(stream)

        z = DoubleArray(numberOfPoints)
        for (i in 0 until numberOfPoints) {
            z[i] = ISUtil.readLeDouble(stream)
        }

        if (isMax) {
            minM = ISUtil.readLeDouble(stream)
            maxM = ISUtil.readLeDouble(stream)

            measures = DoubleArray(numberOfPoints)
            for (i in 0 until numberOfPoints) {
                measures[i] = ISUtil.readLeDouble(stream)
            }
        }
    }
}