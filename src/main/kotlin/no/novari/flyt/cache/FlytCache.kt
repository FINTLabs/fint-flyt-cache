package no.novari.flyt.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import no.novari.flyt.cache.exceptions.NoSuchCacheEntryException
import java.time.Duration

/**
 * In-memory cache backed by Caffeine. Entries expire [timeToLive] after they were last written, and the least
 * valuable entries are evicted when the cache holds more than [maximumSize].
 */
class FlytCache<K : Any, V : Any>(
    val alias: String,
    timeToLive: Duration = Duration.ofDays(6),
    maximumSize: Long = 1_000_000,
) {
    internal val nativeCache: Cache<K, V> =
        Caffeine
            .newBuilder()
            .expireAfterWrite(timeToLive)
            .maximumSize(maximumSize)
            .recordStats()
            .build()

    /** The number of entries, after pending expiry and eviction has run. */
    val size: Long
        get() {
            nativeCache.cleanUp()
            return nativeCache.estimatedSize()
        }

    fun containsKey(key: K): Boolean = nativeCache.asMap().containsKey(key)

    fun get(key: K): V = getOrNull(key) ?: throw NoSuchCacheEntryException(key.toString())

    fun getOrNull(key: K): V? = nativeCache.getIfPresent(key)

    /** The values for the keys that are in the cache. Missing keys are left out. */
    fun get(keys: Collection<K>): List<V> = nativeCache.getAllPresent(keys).values.toList()

    fun values(): List<V> = nativeCache.asMap().values.toList()

    fun distinctValues(): List<V> = values().distinct()

    fun put(
        key: K,
        value: V,
    ) = nativeCache.put(key, value)

    fun put(
        keys: Collection<K>,
        value: V,
    ) = put(keys.associateWith { value })

    fun put(entries: Map<out K, V>) = nativeCache.putAll(entries)

    fun remove(key: K) = nativeCache.invalidate(key)

    fun remove(keys: Collection<K>) = nativeCache.invalidateAll(keys)

    fun clear() = nativeCache.invalidateAll()

    override fun toString() = "FlytCache{alias='$alias', entries=$size}"
}
