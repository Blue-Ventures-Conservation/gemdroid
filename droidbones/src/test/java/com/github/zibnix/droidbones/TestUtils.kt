package com.github.zibnix.droidbones

import org.mockito.Mockito

/**
 * Make Kotlin happy with Mockito's any() matcher.
 */
internal fun <T> any() = Mockito.any() as T
