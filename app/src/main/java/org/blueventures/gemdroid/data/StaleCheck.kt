package org.blueventures.gemdroid.data

// seconds
fun staleCheck(createdAt: Int, timeout: Int) = (System.currentTimeMillis() / 1000) - createdAt > timeout