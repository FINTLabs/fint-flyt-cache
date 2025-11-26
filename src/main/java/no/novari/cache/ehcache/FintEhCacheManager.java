package no.novari.cache.ehcache;

import no.novari.cache.FintCacheManager;
import no.novari.cache.FintCacheOptions;
import no.novari.cache.exceptions.NoSuchCacheException;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.time.Duration;
import java.util.Optional;

public class FintEhCacheManager implements FintCacheManager {

    private final CacheManager cacheManager;
    private final FintCacheOptions defaultCacheOptions;

    public FintEhCacheManager(FintCacheOptions defaultCacheOptions) {
        this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
        this.defaultCacheOptions = defaultCacheOptions;
    }

    public <K, V> FintEhCache<K, V> createCache(String alias, Class<K> keyClass, Class<V> valueClass) {
        return createCache(alias, keyClass, valueClass, FintCacheOptions.builder().build());
    }

    public <K, V> FintEhCache<K, V> createCache(String alias, Class<K> keyClass, Class<V> valueClass, FintCacheOptions cacheOptions) {
        CacheConfiguration<K, V> cacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        keyClass,
                        valueClass,
                        ResourcePoolsBuilder.heap(
                                cacheOptions.heapSize != null
                                        ? cacheOptions.heapSize
                                        : this.defaultCacheOptions.heapSize
                        ).build()
                )
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMillis(
                                cacheOptions.timeToLive != null
                                        ? cacheOptions.timeToLive.toMillis()
                                        : this.defaultCacheOptions.timeToLive.toMillis()
                        ))
                ).build();

        FintEhCache<K, V> cache = new FintEhCache<>(
                alias,
                this.cacheManager.createCache(
                        alias,
                        cacheConfiguration
                )
        );
        cache.addEventListener(new FintEhCacheEventLogger<>(alias));
        return cache;
    }

    public <K, V> FintEhCache<K, V> getCache(String alias, Class<K> keyClass, Class<V> valueClass) {
        return Optional.ofNullable(this.cacheManager.getCache(alias, keyClass, valueClass))
                .map(cache -> new FintEhCache<>(alias, cache))
                .orElseThrow(() -> new NoSuchCacheException(alias));
    }

    @Override
    public <K, V> void removeCache(String alias) {
        this.cacheManager.removeCache(alias);
    }

}
