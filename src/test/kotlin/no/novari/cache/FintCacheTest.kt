package no.novari.cache

import no.novari.cache.exceptions.NoSuchCacheEntryException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration

class FintCacheTest {
    private fun createCacheWithEntries(): FintCache<String, Int> =
        FintCache<String, Int>("testAlias").apply {
            put("testKey1", 1)
            put("testKey2", 1)
            put("testKey3", 5)
        }

    @Test
    fun `has the alias it was created with`() {
        assertEquals("testAlias", FintCache<String, Int>("testAlias").alias)
    }

    @Test
    fun `contains a key only after an entry with it is added`() {
        val cache = FintCache<String, Int>("testAlias")
        assertFalse(cache.containsKey("testKey"))

        cache.put("testKey", 1)

        assertTrue(cache.containsKey("testKey"))
    }

    @Test
    fun `gets a value by key`() {
        assertEquals(1, createCacheWithEntries().get("testKey1"))
    }

    @Test
    fun `throws when no entry has the key`() {
        assertThrows<NoSuchCacheEntryException> { createCacheWithEntries().get("differentKey") }
    }

    @Test
    fun `gets a value or null by key`() {
        val cache = createCacheWithEntries()

        assertEquals(1, cache.getOrNull("testKey1"))
        assertNull(cache.getOrNull("differentKey"))
    }

    @Test
    fun `gets values by a collection of keys and leaves out missing keys`() {
        val getResult = createCacheWithEntries().get(listOf("testKey1", "testKey3", "differentKey"))

        assertEquals(listOf(1, 5), getResult.sorted())
    }

    @Test
    fun `gets all values`() {
        assertEquals(listOf(1, 1, 5), createCacheWithEntries().values().sorted())
    }

    @Test
    fun `gets all distinct values`() {
        assertEquals(listOf(1, 5), createCacheWithEntries().distinctValues().sorted())
    }

    @Test
    fun `puts one value for several keys`() {
        val cache = FintCache<String, Int>("testAlias")
        cache.put(listOf("testKey1", "testKey2"), 1)

        assertEquals(2, cache.size)
        assertEquals(1, cache.get("testKey1"))
        assertEquals(1, cache.get("testKey2"))
    }

    @Test
    fun `puts values from a map`() {
        val cache = FintCache<String, Int>("testAlias")
        cache.put(mapOf("testKey1" to 1, "testKey2" to 3))

        assertEquals(2, cache.size)
        assertEquals(3, cache.get("testKey2"))
    }

    @Test
    fun `replaces the value for an existing key`() {
        val cache = createCacheWithEntries()

        cache.put("testKey1", 2)

        assertEquals(2, cache.get("testKey1"))
        assertEquals(3, cache.size)
    }

    @Test
    fun `removes an entry by key`() {
        val cache = createCacheWithEntries()

        cache.remove("testKey1")

        assertNull(cache.getOrNull("testKey1"))
        assertEquals(2, cache.size)
    }

    @Test
    fun `removes entries by keys`() {
        val cache = createCacheWithEntries()

        cache.remove(listOf("testKey1", "testKey3"))

        assertEquals(listOf(1), cache.values())
    }

    @Test
    fun `clears all entries`() {
        val cache = createCacheWithEntries()

        cache.clear()

        assertEquals(0, cache.size)
    }

    @Test
    fun `expires an entry after its time to live`() {
        val cache = FintCache<String, Int>("testAlias", timeToLive = Duration.ofMillis(1))
        cache.put("testKey", 1)
        Thread.sleep(5)

        assertNull(cache.getOrNull("testKey"))
        assertEquals(0, cache.size)
    }

    @Test
    fun `evicts entries when the maximum size is exceeded`() {
        val cache = FintCache<String, Int>("testAlias", maximumSize = 2)
        cache.put("testKey1", 1)
        cache.put("testKey2", 2)
        cache.put("testKey3", 3)

        assertEquals(2, cache.size)
    }

    @Test
    fun `does not expose values in toString`() {
        assertEquals("FintCache{alias='testAlias', entries=3}", createCacheWithEntries().toString())
    }
}
