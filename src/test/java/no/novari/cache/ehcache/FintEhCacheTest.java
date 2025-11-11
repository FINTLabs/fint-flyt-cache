package no.novari.cache.ehcache;

import no.novari.cache.*;
import java.util.function.Consumer;

class FintEhCacheTest extends FintCacheTest {

    @Override
    protected FintCacheManager createCacheManager(FintCacheOptions defaultFintCacheOptions) {
        return new FintEhCacheManager(defaultFintCacheOptions);
    }

    private static class TestEventListener<K, V> extends FintEhCacheEventListener<K, V> {

        private final Consumer<FintCacheEvent<K, V>> eventConsumer;

        TestEventListener(Consumer<FintCacheEvent<K, V>> eventConsumer) {
            this.eventConsumer = eventConsumer;
        }

        @Override
        public void onEvent(FintCacheEvent<K, V> event) {
            eventConsumer.accept(event);
        }
    }

    @Override
    protected <K, V> FintCacheEventListener<K, V> createEventListener(CacheEventObserver<K, V> observer) {
        return new TestEventListener<>(observer::consume);
    }

}
