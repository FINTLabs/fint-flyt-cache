package no.fintlabs.cache.ehcache;

import no.fintlabs.cache.FintCacheManager;
import no.fintlabs.cache.FintCacheManagerTest;
import no.fintlabs.cache.FintCacheOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FintEhCacheManagerTest extends FintCacheManagerTest {

    @Override
    protected FintCacheManager createCacheManager(FintCacheOptions defaultCacheOptions) {
        return new FintEhCacheManager(defaultCacheOptions);
    }

    @Test
    void shouldBeInstanceOfFintEhCacheManager() {
        assertInstanceOf(FintEhCacheManager.class, fintCacheManager);
    }

    @Test
    void shouldCreateFintEhCache() {
        assertInstanceOf(FintEhCache.class, fintCacheManager.createCache("testCache", String.class, Integer.class));
    }
}
