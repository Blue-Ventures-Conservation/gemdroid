package org.blueventures.gemdroid.data

object Regexp {
    val roiName by lazy { Regex("[a-zA-Z\\d]+[a-zA-Z\\d\\s]*") }
    val assetName by lazy { Regex("[a-zA-Z\\d\\-_]+") }
    val subRegionName by lazy { Regex("[a-zA-Z\\d\\-_\\s]+") }
}