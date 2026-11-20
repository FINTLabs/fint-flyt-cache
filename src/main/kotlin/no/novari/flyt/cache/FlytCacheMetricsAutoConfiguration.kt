package no.novari.flyt.cache

import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean

/**
 * Registers Micrometer cache metrics (`cache.gets`, `cache.size`, `cache.evictions`, ...) tagged with
 * `cache=<alias>` for every [FlytCache] bean. Spring Boot binds [MeterBinder] beans to the meter registry.
 */
@AutoConfiguration
@ConditionalOnClass(MeterBinder::class)
class FlytCacheMetricsAutoConfiguration {
    @Bean
    fun fintCacheMeterBinder(caches: ObjectProvider<FlytCache<*, *>>): MeterBinder =
        MeterBinder { registry ->
            val fintCaches = caches.orderedStream().toList()
            val duplicateAliases = fintCaches.groupBy { it.alias }.filterValues { it.size > 1 }.keys
            check(duplicateAliases.isEmpty()) { "FlytCache aliases must be unique, but these are not: $duplicateAliases" }
            fintCaches.forEach { CaffeineCacheMetrics.monitor(registry, it.nativeCache, it.alias) }
        }
}
