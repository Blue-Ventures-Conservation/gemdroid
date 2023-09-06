package org.blueventures.gemdroid.data

interface URLs: Expires {
    fun ordered(i: Int): String
}

interface Expires {
    val createdAt: Int
    val timeout: Int
}

// seconds
fun staleCheck(expires: Expires) = (System.currentTimeMillis() / 1000) - expires.createdAt > expires.timeout