package no.novari.cache;

import no.novari.cache.ehcache.FintEhCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@AutoConfiguration
public class FintCacheConfiguration {

    @Value("${novari.cache.defaultCacheEntryTimeToLive:6d}")
    Duration defaultCacheEntryTimeToLive;

    @Value("${novari.cache.defaultCacheHeapSize:1000000}")
    Long defaultCacheHeapSize;

    @Bean
    public FintCacheManager fintCacheManager() {
        return new FintEhCacheManager(
                FintCacheOptions.builder()
                        .timeToLive(this.defaultCacheEntryTimeToLive)
                        .heapSize(this.defaultCacheHeapSize)
                        .build()
        );
    }
}
