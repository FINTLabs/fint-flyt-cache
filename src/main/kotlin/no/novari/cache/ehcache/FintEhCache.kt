package no.novari.cache.ehcache

import no.novari.cache.FintCache
import no.novari.cache.FintCacheEventListener
import org.ehcache.Cache
import org.ehcache.event.EventFiring
import org.ehcache.event.EventOrdering
import org.ehcache.event.EventType
import java.util.EnumSet

class FintEhCache<K, V>(
    override val alias: String,
    private val cache: Cache<K, V>,
) : FintCache<K, V> {
    override fun containsKey(key: K) = cache.containsKey(key)

    override fun getOrNull(key: K): V? = cache.get(key)

    // Ehcache maps missing keys to null.
    override fun get(keys: Collection<K>): List<V> = cache.getAll(keys.toSet()).values.filterNotNull()

    override fun getAll(): List<V> = cache.map { it.value }

    override fun put(
        key: K,
        value: V,
    ) = cache.put(key, value)

    override fun put(entries: Map<out K, V>) = cache.putAll(entries)

    override fun remove(key: K) = cache.remove(key)

    override fun remove(keys: Collection<K>) = cache.removeAll(keys.toSet())

    override fun clear() = cache.clear()

    override fun addEventListener(listener: FintCacheEventListener<K, V>) =
        cache.runtimeConfiguration.registerCacheEventListener(
            listener as FintEhCacheEventListener<K, V>,
            EventOrdering.ORDERED,
            EventFiring.SYNCHRONOUS,
            EnumSet.allOf(EventType::class.java),
        )

    override fun removeEventListener(listener: FintCacheEventListener<K, V>) =
        cache.runtimeConfiguration.deregisterCacheEventListener(listener as FintEhCacheEventListener<K, V>)

    override fun toString() = "FintEhCache{alias='$alias', entries=${getNumberOfEntries()}}"
}
