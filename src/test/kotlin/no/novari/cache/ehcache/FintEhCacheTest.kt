package no.novari.cache.ehcache

import no.novari.cache.FintCache
import no.novari.cache.FintCacheEvent
import no.novari.cache.FintCacheEvent.EventType
import no.novari.cache.FintCacheOptions
import no.novari.cache.exceptions.NoSuchCacheEntryException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class FintEhCacheTest {
    private val fintCacheManager = FintEhCacheManager(FintCacheOptions(Duration.ofSeconds(1000), 10))

    private class CacheEventObserver(
        eventsToWaitFor: Int,
    ) : FintEhCacheEventListener<String, Int>() {
        val countDownLatch = CountDownLatch(eventsToWaitFor)
        val emittedEvents = mutableListOf<FintCacheEvent<String, Int>>()

        override fun onEvent(event: FintCacheEvent<String, Int>) {
            emittedEvents.add(event)
            countDownLatch.countDown()
        }
    }

    private fun createCache(options: FintCacheOptions = FintCacheOptions()): FintCache<String, Int> =
        fintCacheManager.createCache("testAlias", String::class.java, Int::class.javaObjectType, options)

    private fun createCacheWithEntries(): FintCache<String, Int> =
        createCache().apply {
            put("testKey1", 1)
            put("testKey2", 1)
            put("testKey3", 5)
        }

    @Test
    fun `has the alias given by the cache manager`() {
        assertEquals("testAlias", createCache().alias)
    }

    @Test
    fun `contains a key after an entry with it is added`() {
        val cache = createCache()
        cache.put("testKey", 1)
        assertTrue(cache.containsKey("testKey"))
    }

    @Test
    fun `gets a value by key`() {
        val cache = createCache()
        cache.put("testKey", 1)
        assertEquals(1, cache.get("testKey"))
    }

    @Test
    fun `throws when no entry has the key`() {
        val cache = createCache()
        cache.put("testKey", 1)
        assertThrows<NoSuchCacheEntryException> { cache.get("differentKey") }
    }

    @Test
    fun `gets a value or null by key`() {
        val cache = createCache()
        cache.put("testKey", 1)

        assertEquals(1, cache.getOrNull("testKey"))
        assertNull(cache.getOrNull("differentKey"))
    }

    @Test
    fun `gets values by a collection of keys`() {
        val getResult = createCacheWithEntries().get(listOf("testKey1", "testKey2", "testKey3"))

        assertEquals(listOf(1, 1, 5), getResult.sorted())
    }

    @Test
    fun `leaves out missing keys when getting values by a collection of keys`() {
        val getResult = createCacheWithEntries().get(listOf("testKey1", "differentKey"))

        assertEquals(listOf(1), getResult)
    }

    @Test
    fun `gets all values`() {
        assertEquals(listOf(1, 1, 5), createCacheWithEntries().getAll().sorted())
    }

    @Test
    fun `gets all distinct values`() {
        assertEquals(listOf(1, 5), createCacheWithEntries().getAllDistinct().sorted())
    }

    @Test
    fun `puts one value for several keys`() {
        val cache = createCache()
        cache.put(listOf("testKey1", "testKey2"), 1)

        assertEquals(2, cache.getNumberOfEntries())
        assertEquals(1, cache.get("testKey1"))
        assertEquals(1, cache.get("testKey2"))
    }

    @Test
    fun `puts values from a map`() {
        val cache = createCache()
        cache.put(mapOf("testKey1" to 1, "testKey2" to 3))

        assertEquals(2, cache.getNumberOfEntries())
        assertEquals(1, cache.get("testKey1"))
        assertEquals(3, cache.get("testKey2"))
    }

    @Test
    fun `removes an entry by key`() {
        val cache = createCache()
        cache.put("testKey1", 1)

        cache.remove("testKey1")

        assertEquals(0, cache.getNumberOfEntries())
    }

    @Test
    fun `removes entries by keys`() {
        val cache = createCacheWithEntries()

        cache.remove(listOf("testKey1", "testKey3"))

        assertEquals(1, cache.getNumberOfEntries())
        assertEquals(1, cache.get("testKey2"))
    }

    @Test
    fun `counts entries`() {
        assertEquals(3, createCacheWithEntries().getNumberOfEntries())
    }

    @Test
    fun `counts distinct values`() {
        assertEquals(2, createCacheWithEntries().getNumberOfDistinctValues())
    }

    @Test
    fun `clears all entries`() {
        val cache = createCacheWithEntries()

        cache.clear()

        assertEquals(0, cache.getNumberOfEntries())
    }

    @Test
    fun `shares entries between instances with the same alias`() {
        val cache1 = createCache()
        val cache2 = fintCacheManager.getCache("testAlias", String::class.java, Int::class.javaObjectType)

        cache1.put("testKey1", 1)

        assertEquals(1, cache2.get("testKey1"))
    }

    @Test
    fun `removes an entry when it has expired`() {
        val cache = createCache(FintCacheOptions(timeToLive = Duration.ofMillis(1)))
        cache.put("testKey", 1)
        Thread.sleep(2)

        assertNull(cache.getOrNull("testKey"))
    }

    @Test
    fun `evicts entries when the heap size is exceeded`() {
        val cache = createCache(FintCacheOptions(heapSize = 2))
        cache.put("testKey1", 1)
        cache.put("testKey2", 2)
        cache.put("testKey3", 3)

        assertEquals(2, cache.getNumberOfEntries())
    }

    @Test
    fun `notifies the listener when an entry is created`() {
        val cache = createCache()
        val observer = CacheEventObserver(1)
        cache.addEventListener(observer)

        cache.put("testKey", 1)
        observer.countDownLatch.await()

        assertEquals(listOf(FintCacheEvent(EventType.CREATED, "testKey", null, 1)), observer.emittedEvents)
    }

    @Test
    fun `notifies the listener when an entry is updated`() {
        val cache = createCache()
        cache.put("testKey", 1)
        val observer = CacheEventObserver(1)
        cache.addEventListener(observer)

        cache.put("testKey", 2)
        observer.countDownLatch.await()

        assertEquals(listOf(FintCacheEvent(EventType.UPDATED, "testKey", 1, 2)), observer.emittedEvents)
    }

    @Test
    fun `notifies the listener when an entry is removed`() {
        val cache = createCache()
        cache.put("testKey", 1)
        val observer = CacheEventObserver(1)
        cache.addEventListener(observer)

        cache.remove("testKey")
        observer.countDownLatch.await()

        assertEquals(listOf(FintCacheEvent<String, Int>(EventType.REMOVED, "testKey", 1, null)), observer.emittedEvents)
    }

    @Test
    fun `notifies the listener when an entry expires`() {
        val cache = createCache(FintCacheOptions(timeToLive = Duration.ofMillis(1)))
        cache.put("testKey", 1)
        val observer = CacheEventObserver(1)
        cache.addEventListener(observer)
        Thread.sleep(2)

        val getResult = cache.getOrNull("testKey")

        assertTrue(observer.countDownLatch.await(5, TimeUnit.SECONDS))
        assertNull(getResult)
        assertEquals(listOf(FintCacheEvent<String, Int>(EventType.EXPIRED, "testKey", 1, null)), observer.emittedEvents)
    }

    @Test
    fun `notifies the listener when an entry is evicted`() {
        val cache = createCache(FintCacheOptions(heapSize = 1))
        cache.put("testKey1", 1)
        val observer = CacheEventObserver(2)
        cache.addEventListener(observer)

        cache.put("testKey2", 2)

        assertTrue(observer.countDownLatch.await(5, TimeUnit.SECONDS))
        assertEquals(FintCacheEvent(EventType.CREATED, "testKey2", null, 2), observer.emittedEvents[0])
        assertEquals(EventType.EVICTED, observer.emittedEvents[1].type)
    }

    @Test
    fun `stops notifying a removed listener`() {
        val cache = createCache()
        val observer = CacheEventObserver(1)
        cache.addEventListener(observer)
        cache.removeEventListener(observer)

        cache.put("testKey", 1)

        assertEquals(emptyList<FintCacheEvent<String, Int>>(), observer.emittedEvents)
    }

    @Test
    fun `does not expose values in the event toString`() {
        assertEquals("Sensitive data omitted", FintCacheEvent(EventType.CREATED, "testKey", null, 1).toString())
    }
}
