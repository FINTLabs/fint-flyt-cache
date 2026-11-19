package no.novari.cache.ehcache

import no.novari.cache.FintCacheManager
import no.novari.cache.FintCacheOptions
import no.novari.cache.exceptions.NoSuchCacheException
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

class FintEhCacheManager(
    defaultCacheOptions: FintCacheOptions,
) : FintCacheManager {
    private val defaultTimeToLive =
        requireNotNull(defaultCacheOptions.timeToLive) { "Default cache options must set timeToLive" }
    private val defaultHeapSize =
        requireNotNull(defaultCacheOptions.heapSize) { "Default cache options must set heapSize" }
    private val cacheManager: CacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)

    override fun <K, V> createCache(
        alias: String,
        keyClass: Class<K>,
        valueClass: Class<V>,
        cacheOptions: FintCacheOptions,
    ): FintEhCache<K, V> {
        val cacheConfiguration =
            CacheConfigurationBuilder
                .newCacheConfigurationBuilder(
                    keyClass,
                    valueClass,
                    ResourcePoolsBuilder.heap(cacheOptions.heapSize ?: defaultHeapSize).build(),
                ).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(cacheOptions.timeToLive ?: defaultTimeToLive))
                .build()
        return FintEhCache(alias, cacheManager.createCache(alias, cacheConfiguration)).also {
            it.addEventListener(FintEhCacheEventLogger(alias))
        }
    }

    override fun <K, V> getCache(
        alias: String,
        keyClass: Class<K>,
        valueClass: Class<V>,
    ): FintEhCache<K, V> =
        cacheManager.getCache(alias, keyClass, valueClass)?.let { FintEhCache(alias, it) }
            ?: throw NoSuchCacheException(alias)

    override fun removeCache(alias: String) = cacheManager.removeCache(alias)
}
