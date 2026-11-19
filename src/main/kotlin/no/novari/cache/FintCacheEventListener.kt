package no.novari.cache

fun interface FintCacheEventListener<K, V> {
    fun onEvent(event: FintCacheEvent<K, V>)
}
