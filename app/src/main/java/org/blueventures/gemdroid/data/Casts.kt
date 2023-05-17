package org.blueventures.gemdroid.data

@Suppress("UNCHECKED_CAST")
inline fun <reified K: Any, reified V: Any> Map<*, *>.checkItemsAre() =
    if (all { entry -> (entry.key is K) && (entry.value is V) })
        this as Map<K, V>
    else null

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> List<*>.checkItemsAre() =
    if (all { it is T })
        this as List<T>
    else null