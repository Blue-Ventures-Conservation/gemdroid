package org.blueventures.gemdroid.data.shp

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException
import org.nocrala.tools.gis.data.esri.shapefile.shape.Const
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.AbstractPolyShape
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil
import java.io.InputStream

abstract class AbstractPolyOptionalMShape(shapeHeader: ShapeHeader, shapeType: ShapeType, stream: InputStream, rules: ValidationPreferences): AbstractPolyShape(shapeHeader, shapeType, stream, rules) {
    private var minM = 0.0
    private var maxM = 0.0
    private var m = DoubleArray(0)
    init {
        val minLen = Cursor.mPolyMinLength(numberOfParts, numberOfPoints)
        val maxLen = Cursor.mPolyMaxLength(numberOfParts, numberOfPoints)
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

        if (isMax) {
            minM = ISUtil.readLeDouble(stream)
            maxM = ISUtil.readLeDouble(stream)

            m = DoubleArray(numberOfPoints)
            for (i in 0 until numberOfPoints) {
                m[i] = ISUtil.readLeDouble(stream)
            }
        }
    }
}