package no.novari.cache

import no.novari.cache.ehcache.FintEhCacheManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import java.time.Duration

@AutoConfiguration
class FintCacheConfiguration {
    @Bean
    fun fintCacheManager(
        @Value("\${novari.cache.defaultCacheEntryTimeToLive:6d}") defaultCacheEntryTimeToLive: Duration,
        @Value("\${novari.cache.defaultCacheHeapSize:1000000}") defaultCacheHeapSize: Long,
    ): FintCacheManager = FintEhCacheManager(FintCacheOptions(defaultCacheEntryTimeToLive, defaultCacheHeapSize))
}
