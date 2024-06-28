package org.blueventures.gemdroid.data

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

object Bounds {
    fun centerFromMultiPoly(multi: MultiPolyPts): LatLng? {
        if (multi.isEmpty()) {
            return null
        }

        val builder = LatLngBounds.builder()
        for (poly in multi) {
            for (ring in poly) {
                for (pt in ring) {
                    builder.include(pt)
                }
            }
        }

        return builder.build().center
    }

    fun centerFromRing(ring: List<LatLng>): LatLng? {
        if (ring.isEmpty()) {
            return null
        }

        val builder = LatLngBounds.builder()
        for (pt in ring) {
            builder.include(pt)
        }

        return builder.build().center
    }
}