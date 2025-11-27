package no.novari.cache;

import no.novari.cache.exceptions.NoSuchCacheException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public abstract class FintCacheManagerTest {

    protected FintCacheManager fintCacheManager;

    @BeforeEach
    void setUp() {
        this.fintCacheManager = createCacheManager(
                FintCacheOptions.builder()
                        .timeToLive(Duration.of(1000L, ChronoUnit.MILLIS))
                        .heapSize(10L)
                        .build()
        );
    }

    protected abstract FintCacheManager createCacheManager(FintCacheOptions defaultCacheOptions);

    @Test
    void shouldReturnCacheByAlias() {
        FintCache<String, Integer> createCacheResult = fintCacheManager.createCache("testAlias", String.class, Integer.class);
        createCacheResult.put("entryKey", 1);

        FintCache<String, Integer> getCacheResult = fintCacheManager.getCache("testAlias", String.class, Integer.class);

        assertEquals(createCacheResult.getAlias(), getCacheResult.getAlias());
        assertEquals(createCacheResult.getNumberOfEntries(), getCacheResult.getNumberOfEntries());
        assertEquals(createCacheResult.get("entryKey"), getCacheResult.get("entryKey"));
    }

    @Test
    void shouldThrowExceptionWhenCreatingCacheWithAliasThatAlreadyExists() {
        fintCacheManager.createCache("testAlias", String.class, Integer.class);
        assertThrows(IllegalArgumentException.class, () ->
                fintCacheManager.createCache("testAlias", String.class, Integer.class)
        );
    }

    @Test
    void shouldThrowExceptionWhenGettingCacheWithAliasThatDoesNotExist() {
        assertThrows(NoSuchCacheException.class, () ->
                fintCacheManager.getCache("testAlias", String.class, Integer.class)
        );
    }
}
