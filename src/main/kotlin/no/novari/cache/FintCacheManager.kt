package no.novari.cache

interface FintCacheManager {
    fun <K, V> createCache(
        alias: String,
        keyClass: Class<K>,
        valueClass: Class<V>,
        cacheOptions: FintCacheOptions = FintCacheOptions(),
    ): FintCache<K, V>

    fun <K, V> getCache(
        alias: String,
        keyClass: Class<K>,
        valueClass: Class<V>,
    ): FintCache<K, V>

    fun removeCache(alias: String)
}
