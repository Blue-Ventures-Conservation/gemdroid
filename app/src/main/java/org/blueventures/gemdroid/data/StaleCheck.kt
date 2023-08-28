package org.blueventures.gemdroid.data

interface URLs {
    val createdAt: Int
    val timeout: Int
    fun ordered(i: Int): String
}

// seconds
fun staleCheck(urls: URLs) = (System.currentTimeMillis() / 1000) - urls.createdAt > urls.timeout