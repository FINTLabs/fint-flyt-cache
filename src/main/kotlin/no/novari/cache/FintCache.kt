package no.novari.cache

import no.novari.cache.exceptions.NoSuchCacheEntryException

interface FintCache<K, V> {
    val alias: String

    fun containsKey(key: K): Boolean

    fun get(key: K): V = getOrNull(key) ?: throw NoSuchCacheEntryException(key.toString())

    fun getOrNull(key: K): V?

    fun get(keys: Collection<K>): List<V>

    fun getAll(): List<V>

    fun getAllDistinct(): List<V> = getAll().distinct()

    fun put(
        key: K,
        value: V,
    )

    fun put(
        keys: Collection<K>,
        value: V,
    ) = put(keys.associateWith { value })

    fun put(entries: Map<out K, V>)

    fun remove(key: K)

    fun remove(keys: Collection<K>)

    fun clear()

    fun getNumberOfEntries(): Long = getAll().size.toLong()

    fun getNumberOfDistinctValues(): Long = getAllDistinct().size.toLong()

    fun addEventListener(listener: FintCacheEventListener<K, V>)

    fun removeEventListener(listener: FintCacheEventListener<K, V>)
}
