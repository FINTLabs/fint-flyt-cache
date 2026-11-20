package no.novari.flyt.cache

import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Checks at startup that every [FlytCache] bean has a unique alias. With Micrometer on the classpath, it also registers
 * the Caffeine metrics (`cache.gets`, `cache.size`, `cache.evictions`, ...) tagged with `cache=<alias>` for every
 * [FlytCache] bean. Spring Boot binds [MeterBinder] beans to the meter registry.
 */
@AutoConfiguration
class FlytCacheAutoConfiguration {
    @Bean
    fun flytCacheAliasCheck(caches: ObjectProvider<FlytCache<*, *>>): SmartInitializingSingleton =
        SmartInitializingSingleton {
            val duplicateAliases =
                caches
                    .orderedStream()
                    .toList()
                    .groupBy { it.alias }
                    .filterValues { it.size > 1 }
                    .keys
            check(duplicateAliases.isEmpty()) { "FlytCache aliases must be unique, but these are not: $duplicateAliases" }
        }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(MeterBinder::class)
    class Metrics {
        @Bean
        fun flytCacheMeterBinder(caches: ObjectProvider<FlytCache<*, *>>): MeterBinder =
            MeterBinder { registry ->
                caches.orderedStream().forEach { CaffeineCacheMetrics.monitor(registry, it.nativeCache, it.alias) }
            }
    }
}
