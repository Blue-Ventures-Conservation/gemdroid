package org.blueventures.gemdroid.data

object Regexp {
    val roiName by lazy { Regex("[a-zA-ZÀ-Ÿ\\d\\-_\\s]+") }
    val assetName by lazy { Regex("[a-zA-Z\\d\\-_]+") }
    val subRegionName by lazy { Regex("[a-zA-ZÀ-Ÿ\\d\\-_\\s]+") }
}