package no.novari.cache.ehcache

import no.novari.cache.FintCacheOptions
import no.novari.cache.exceptions.NoSuchCacheException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration

class FintEhCacheManagerTest {
    private val fintCacheManager = FintEhCacheManager(FintCacheOptions(Duration.ofMillis(1000), 10))

    @Test
    fun `returns the cache by alias`() {
        val createCacheResult = fintCacheManager.createCache("testAlias", String::class.java, Int::class.javaObjectType)
        createCacheResult.put("entryKey", 1)

        val getCacheResult = fintCacheManager.getCache("testAlias", String::class.java, Int::class.javaObjectType)

        assertEquals(createCacheResult.alias, getCacheResult.alias)
        assertEquals(createCacheResult.getNumberOfEntries(), getCacheResult.getNumberOfEntries())
        assertEquals(createCacheResult.get("entryKey"), getCacheResult.get("entryKey"))
    }

    @Test
    fun `throws when creating a cache with an alias that already exists`() {
        fintCacheManager.createCache("testAlias", String::class.java, Int::class.javaObjectType)
        assertThrows<IllegalArgumentException> {
            fintCacheManager.createCache("testAlias", String::class.java, Int::class.javaObjectType)
        }
    }

    @Test
    fun `throws when getting a cache with an alias that does not exist`() {
        assertThrows<NoSuchCacheException> {
            fintCacheManager.getCache("testAlias", String::class.java, Int::class.javaObjectType)
        }
    }

    @Test
    fun `creates a FintEhCache`() {
        assertInstanceOf(FintEhCache::class.java, fintCacheManager.createCache("testCache", String::class.java, Int::class.javaObjectType))
    }

    @Test
    fun `requires default time to live and heap size`() {
        assertThrows<IllegalArgumentException> { FintEhCacheManager(FintCacheOptions(heapSize = 10)) }
        assertThrows<IllegalArgumentException> { FintEhCacheManager(FintCacheOptions(timeToLive = Duration.ofSeconds(1))) }
    }
}
