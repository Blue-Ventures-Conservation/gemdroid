package org.blueventures.gemdroid.data

import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.SphericalUtil
import org.blueventures.gemdroid.data.PolygonUtils.opt
import org.blueventures.gemdroid.ui.theme.Chartreuse
import kotlin.math.atan
import kotlin.math.hypot

// height and width in meters
open class Rectangle(open val width: Double, open val height: Double)  {
   companion object {
       // side length in meters
       fun square(cellSize: Double, center: LatLng, side: Double) = toPolygon(cellSize, center, Rectangle(side, side))

       fun toPolygon(cellSize: Double, center: LatLng, rect: Rectangle): Pair<PolygonOptions, List<PolygonOptions>>? {
           val distToSide = rect.width/2.0
           val distToTopBot = rect.height/2.0
           val hyp = hypot(distToSide, distToTopBot)
           val angle = Math.toDegrees(atan(distToTopBot/distToSide))
           val northeast = SphericalUtil.computeOffset(center, hyp, angle)
           val southwest = SphericalUtil.computeOffset(center, hyp, angle + 180)

           val polyOpts = optsFromPoints(pointsFromCorners(northeast, southwest)) ?: return null

           val grid = mutableListOf<PolygonOptions>()
           val cellsWide = rect.width / cellSize
           val cellsTall = rect.height / cellSize

           val cellHyp = hypot(cellSize, cellSize)
           for (x in 0 until cellsWide.toInt()) {
               val rowNE = SphericalUtil.computeOffset(northeast, cellSize * x, 90.0)
               for (y in 0 until cellsTall.toInt()) {
                   val ne = SphericalUtil.computeOffset(rowNE, cellSize * y, 180.0)
                   val sw = SphericalUtil.computeOffset(ne, cellHyp, 135.0)
                   val cellOpts = optsFromPoints(pointsFromCorners(ne, sw)) ?: return null
                   grid.add(cellOpts)
               }
           }

           return Pair(polyOpts, grid)
       }

       private fun pointsFromCorners(northeast: LatLng, southwest: LatLng): List<LatLng> {
           val southeast = LatLng(southwest.latitude, northeast.longitude)
           val northwest = LatLng(northeast.latitude, southwest.longitude)
           return listOf(
               northeast,
               southeast,
               southwest,
               northwest,
               northeast // closed ring
           )
       }

       private fun optsFromPoints(points: List<LatLng>): PolygonOptions? {
           return opt(listOf(points), 0x00000000, ColorUtils.setAlphaComponent(Chartreuse.toArgb(), 0x7F), 12f)
       }
   }
}