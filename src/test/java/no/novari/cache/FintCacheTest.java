package no.novari.cache;

import no.novari.cache.exceptions.NoSuchCacheEntryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class FintCacheTest {

    protected FintCacheManager fintCacheManager;

    @BeforeEach
    void setUp() {
        this.fintCacheManager = createCacheManager(
                FintCacheOptions.builder()
                        .timeToLive(Duration.of(1000L, ChronoUnit.SECONDS))
                        .heapSize(10L)
                        .build()
        );
    }

    protected abstract FintCacheManager createCacheManager(FintCacheOptions defaultCacheOptions);

    protected abstract <K, V> FintCacheEventListener<K, V> createEventListener(CacheEventObserver<K, V> observer);

    protected static class CacheEventObserver<K, V> {
        final CountDownLatch countDownLatch;
        List<FintCacheEvent<K, V>> emittedEvents;

        public CacheEventObserver(int eventsToWaitFor) {
            this.countDownLatch = new CountDownLatch(eventsToWaitFor);
            this.emittedEvents = new ArrayList<>();
        }

        public void consume(FintCacheEvent<K, V> event) {
            this.emittedEvents.add(event);
            countDownLatch.countDown();
        }
    }

    @Test
    void shouldContainAliasGivenByCacheManager() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        assertEquals("testAlias", cache.getAlias());
    }

    private FintCache<String, Integer> getFintCacheWithoutOptions() {
        return fintCacheManager.createCache("testAlias", String.class, Integer.class);
    }

    @Test
    void shouldContainKeyIfEntryWithKeyHasBeenAdded() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey", 1);
        assertTrue(cache.containsKey("testKey"));
    }

    @Test
    void shouldGetValueBySingleKey() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey", 1);
        assertEquals(1, cache.get("testKey"));
    }

    @Test
    void shouldThrowExceptionIfNoEntryWithKeyExists() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey", 1);

        assertThrows(NoSuchCacheEntryException.class, () -> cache.get("differentKey"));
    }

    @Test
    void shouldGetOptionalValueBySingleKey() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey", 1);

        assertTrue(cache.getOptional("testKey").isPresent());
        assertFalse(cache.getOptional("differentKey").isPresent());
    }

    @Test
    void shouldGetValuesByCollectionOfKeys() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey1", 1);
        cache.put("testKey2", 1);
        cache.put("testKey3", 5);

        List<Integer> getResult = cache.get(asList("testKey1", "testKey2", "testKey3"));

        assertEquals(3, getResult.size());
        assertTrue(getResult.containsAll(asList(1, 1, 5)));
    }

    @Test
    void shouldGetAllValues() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey1", 1);
        cache.put("testKey2", 1);
        cache.put("testKey3", 5);

        List<Integer> getResult = cache.getAll();

        assertEquals(3, getResult.size());
        assertTrue(getResult.containsAll(asList(1, 1, 5)));
    }

    @Test
    void shouldGetAllDistinctValues() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey1", 1);
        cache.put("testKey2", 1);
        cache.put("testKey3", 5);

        List<Integer> getResult = cache.getAllDistinct();

        assertEquals(2, getResult.size());
        assertTrue(getResult.containsAll(asList(1, 5)));
    }

    @Test
    void shouldPutValueForSingleKey() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey1", 1);

        assertEquals(1, cache.get("testKey1"));
    }

    @Test
    void shouldPutValueForMultipleKeys() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put(asList("testKey1", "testKey2"), 1);

        assertEquals(2, cache.getNumberOfEntries());
        assertEquals(1, cache.get("testKey1"));
        assertEquals(1, cache.get("testKey2"));
    }

    @Test
    void shouldPutValuesFromMap() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put(Map.of("testKey1", 1, "testKey2", 3));

        assertEquals(2, cache.getNumberOfEntries());
        assertEquals(1, cache.get("testKey1"));
        assertEquals(3, cache.get("testKey2"));
    }

    @Test
    void shouldRemoveEntryBySingleKey() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey1", 1);

        cache.remove("testKey1");

        assertEquals(0, cache.getNumberOfEntries());
    }

    @Test
    void shouldRemoveEntriesByKeys() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey1", 1);
        cache.put("testKey2", 1);
        cache.put("testKey3", 5);

        cache.remove(asList("testKey1", "testKey3"));

        assertEquals(1, cache.getNumberOfEntries());
        assertEquals(1, cache.get("testKey2"));
    }

    @Test
    void shouldGetNumberOfEntries() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey1", 1);
        cache.put("testKey2", 1);
        cache.put("testKey3", 5);

        assertEquals(3, cache.getNumberOfEntries());
    }

    @Test
    void shouldGetNumberOfDistinctValues() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey1", 1);
        cache.put("testKey2", 1);
        cache.put("testKey3", 5);

        assertEquals(2, cache.getNumberOfDistinctValues());
    }

    @Test
    void shouldClearAllEntries() {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey1", 1);
        cache.put("testKey2", 1);
        cache.put("testKey3", 5);

        cache.clear();

        assertEquals(0, cache.getNumberOfEntries());
    }

    @Test
    void shouldReflectChangesMadeInParallelCacheInstanceWithSameAlias() {
        FintCache<String, Integer> cache1 = getFintCacheWithoutOptions();
        FintCache<String, Integer> cache2 = fintCacheManager.getCache("testAlias", String.class, Integer.class);

        cache1.put("testKey1", 1);

        assertEquals(1, cache2.get("testKey1"));
    }

    @Test
    void shouldRemoveEntryWhenItHasExpired() throws InterruptedException {
        FintCache<String, Integer> cache = fintCacheManager.createCache(
                "testAlias",
                String.class,
                Integer.class,
                FintCacheOptions.builder().timeToLive(Duration.of(1, ChronoUnit.MILLIS)).build()
        );
        cache.put("testKey", 1);
        Thread.sleep(2);

        assertFalse(cache.getOptional("testKey").isPresent());
    }

    @Test
    void shouldRemoveElementsWhenHeapSizeLimitHasBeenExceeded() {
        FintCache<String, Integer> cache = fintCacheManager.createCache(
                "testAlias",
                String.class,
                Integer.class,
                FintCacheOptions.builder().heapSize(2L).build()
        );
        cache.put("testKey1", 1);
        cache.put("testKey2", 2);
        cache.put("testKey3", 3);

        assertEquals(2, cache.getNumberOfEntries());
    }

    @Test
    void shouldNotifyEventListenerWhenEntryIsCreated() throws InterruptedException {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        CacheEventObserver<String, Integer> observer = new CacheEventObserver<>(1);
        cache.addEventListener(createEventListener(observer));

        cache.put("testKey", 1);
        observer.countDownLatch.await();

        assertEquals(1, observer.emittedEvents.size());
        assertEquals(new FintCacheEvent<>(FintCacheEvent.EventType.CREATED, "testKey", null, 1), observer.emittedEvents.getFirst());
    }

    @Test
    void shouldNotifyEventListenerWhenEntryIsUpdated() throws InterruptedException {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey", 1);
        CacheEventObserver<String, Integer> observer = new CacheEventObserver<>(1);
        cache.addEventListener(createEventListener(observer));

        cache.put("testKey", 2);
        observer.countDownLatch.await();

        assertEquals(1, observer.emittedEvents.size());
        assertEquals(new FintCacheEvent<>(FintCacheEvent.EventType.UPDATED, "testKey", 1, 2), observer.emittedEvents.getFirst());
    }

    @Test
    void shouldNotifyEventListenerWhenEntryIsRemoved() throws InterruptedException {
        FintCache<String, Integer> cache = getFintCacheWithoutOptions();
        cache.put("testKey", 1);
        CacheEventObserver<String, Integer> observer = new CacheEventObserver<>(1);
        cache.addEventListener(createEventListener(observer));

        cache.remove("testKey");
        observer.countDownLatch.await();

        assertEquals(1, observer.emittedEvents.size());
        assertEquals(new FintCacheEvent<>(FintCacheEvent.EventType.REMOVED, "testKey", 1, null), observer.emittedEvents.getFirst());
    }

    @Test
    void shouldNotifyEventListenerWhenEntryIsExpired() throws InterruptedException {
        FintCache<String, Integer> cache = fintCacheManager.createCache(
                "testAlias",
                String.class,
                Integer.class,
                FintCacheOptions.builder().timeToLive(Duration.of(1, ChronoUnit.MILLIS)).build()
        );
        cache.put("testKey", 1);
        CacheEventObserver<String, Integer> observer = new CacheEventObserver<>(1);
        cache.addEventListener(createEventListener(observer));
        Thread.sleep(2);

        Optional<Integer> getResult = cache.getOptional("testKey");
        boolean awaitResult = observer.countDownLatch.await(5, TimeUnit.SECONDS);

        assertTrue(awaitResult);
        assertTrue(getResult.isEmpty());
        assertEquals(1, observer.emittedEvents.size());
        assertEquals(new FintCacheEvent<>(FintCacheEvent.EventType.EXPIRED, "testKey", 1, null), observer.emittedEvents.getFirst());
    }

    @Test
    void shouldNotifyEventListenerWhenEntryIsEvicted() throws InterruptedException {
        FintCache<String, Integer> cache = fintCacheManager.createCache(
                "testAlias",
                String.class,
                Integer.class,
                FintCacheOptions.builder().heapSize(1L).build()
        );
        cache.put("testKey1", 1);
        CacheEventObserver<String, Integer> observer = new CacheEventObserver<>(2);
        cache.addEventListener(createEventListener(observer));

        cache.put("testKey2", 2);
        boolean awaitResult = observer.countDownLatch.await(5, TimeUnit.SECONDS);

        assertTrue(awaitResult);
        assertEquals(2, observer.emittedEvents.size());
        assertEquals(new FintCacheEvent<>(FintCacheEvent.EventType.CREATED, "testKey2", null, 2), observer.emittedEvents.get(0));
        assertEquals(FintCacheEvent.EventType.EVICTED, observer.emittedEvents.get(1).getType());
    }


}
