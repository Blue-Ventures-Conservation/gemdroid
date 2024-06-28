package org.blueventures.gemdroid.data.shp

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType
import java.io.InputStream

class PolygonOptionalZShape(shapeHeader: ShapeHeader, shapeType: ShapeType, stream: InputStream, rules: ValidationPreferences): AbstractPolyOptionalZShape(shapeHeader, shapeType, stream, rules) {
    override fun getShapeTypeName() = "PolygonZ"
}