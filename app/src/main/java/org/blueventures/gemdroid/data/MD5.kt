package org.blueventures.gemdroid.data

import java.math.BigInteger
import java.security.MessageDigest

object MD5 {
    fun string(str: String): String {
        val digest = MessageDigest.getInstance("MD5")
        digest.update(str.encodeToByteArray())
        val magnitude = digest.digest()
        val bi = BigInteger(1, magnitude)
        return String.format("%0" + (magnitude.size shl 1) + "x", bi)
    }
}