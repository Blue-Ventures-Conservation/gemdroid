package org.blueventures.gemdroid.data.shp

// returns are counts of 16-bit words
object Cursor {
    private const val polygon_base_length = 44

    fun zPolyMinLength(parts: Int, points: Int): Int {
        return polyLength(parts, points) + ((16 + (points * 8)) / 2)
    }

    fun zPolyMaxLength(parts: Int, points: Int): Int {
        return zPolyMinLength(parts, points) + ((16 + (points * 8)) / 2)
    }

    fun mPolyMinLength(parts: Int, points: Int): Int {
        return polyLength(parts, points)
    }

    fun mPolyMaxLength(parts: Int, points: Int): Int {
        return zPolyMinLength(parts, points)
    }

    private fun polyLength(parts: Int, points: Int): Int {
        return (polygon_base_length + (parts*4) + (points * 16)) / 2
    }
}