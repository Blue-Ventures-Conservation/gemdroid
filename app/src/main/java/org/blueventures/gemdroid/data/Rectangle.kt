package org.blueventures.gemdroid.data

import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.SphericalUtil
import org.blueventures.gemdroid.data.PolygonDrawer.Companion.opt
import org.blueventures.gemdroid.ui.theme.Chartreuse
import kotlin.math.sqrt

// height and width in meters
open class Rectangle(open val width: Double, open val height: Double)  {
   companion object {
       // side length in meters
       fun square(center: LatLng, side: Double) = toPolygon(center, Rectangle(side, side))

       fun toPolygon(center: LatLng, rect: Rectangle): PolygonOptions {
           val distToSide = rect.width/2.0
           val distToTopBot = rect.height/2.0
           val hyp = hypotenuse(distToSide, distToTopBot)
           val northeast = SphericalUtil.computeOffset(center, hyp, 45.0)
           val southwest = SphericalUtil.computeOffset(center, hyp, 225.0)
           val points = mutableListOf(
               northeast,
               LatLng(southwest.latitude, northeast.longitude), // southeast
               southwest,
               LatLng(northeast.latitude, southwest.longitude), // northwest
               northeast // closed ring
           )

           return opt(listOf(points), 0x00000000, ColorUtils.setAlphaComponent(Chartreuse.toArgb(), 0x7F), 12f)
       }

       private fun hypotenuse(a: Double, b: Double): Double {
           return sqrt((a * a) + (b * b))
       }
   }
}