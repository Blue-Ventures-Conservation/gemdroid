package org.blueventures.gemdroid.data

interface Stale {
    val createdAt: Int
    val timeout: Int
}

// seconds
fun staleCheck(stale: Stale) = (System.currentTimeMillis() / 1000) - stale.createdAt > stale.timeout