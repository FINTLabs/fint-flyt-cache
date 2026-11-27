package no.novari.flyt.cache

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration
import org.springframework.boot.micrometer.metrics.autoconfigure.export.simple.SimpleMetricsExportAutoConfiguration
import org.springframework.boot.test.context.FilteredClassLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class FlytCacheAutoConfigurationTest {
    private val withMetrics =
        ApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(
                    MetricsAutoConfiguration::class.java,
                    SimpleMetricsExportAutoConfiguration::class.java,
                    FlytCacheAutoConfiguration::class.java,
                ),
            )

    private val withoutMetricsSetup =
        ApplicationContextRunner().withConfiguration(AutoConfigurations.of(FlytCacheAutoConfiguration::class.java))

    private fun ApplicationContextRunner.withDuplicateAliases() =
        withBean("cache1", FlytCache::class.java, { FlytCache<String, Int>("person") })
            .withBean("cache2", FlytCache::class.java, { FlytCache<String, Int>("person") })

    @Test
    fun `registers metrics for every cache bean`() {
        withMetrics
            .withBean("personCache", FlytCache::class.java, { FlytCache<String, Int>("person") })
            .withBean("arkivressursCache", FlytCache::class.java, { FlytCache<String, Int>("arkivressurs") })
            .run { context ->
                @Suppress("UNCHECKED_CAST")
                val personCache = context.getBean("personCache") as FlytCache<String, Int>
                personCache.put("key", 1)
                personCache.getOrNull("key")
                personCache.getOrNull("missingKey")
                val registry = context.getBean<MeterRegistry>()

                assertThat(
                    registry
                        .get("cache.gets")
                        .tags("cache", "person", "result", "hit")
                        .functionCounter()
                        .count(),
                ).isEqualTo(1.0)
                assertThat(
                    registry
                        .get("cache.gets")
                        .tags("cache", "person", "result", "miss")
                        .functionCounter()
                        .count(),
                ).isEqualTo(1.0)
                assertThat(
                    registry
                        .get("cache.size")
                        .tags("cache", "arkivressurs")
                        .gauge()
                        .value(),
                ).isEqualTo(0.0)
            }
    }

    @Test
    fun `fails at startup when two caches share an alias`() {
        withMetrics.withDuplicateAliases().run { assertThat(it).hasFailed().failure.hasMessageContaining("[person]") }
    }

    @Test
    fun `fails at startup on a shared alias without Micrometer`() {
        withoutMetricsSetup
            .withClassLoader(FilteredClassLoader(MeterBinder::class.java))
            .withDuplicateAliases()
            .run { assertThat(it).hasFailed().failure.hasMessageContaining("[person]") }
    }

    @Test
    fun `fails at startup on a shared alias without Spring Boot's metrics setup`() {
        withoutMetricsSetup.withDuplicateAliases().run { assertThat(it).hasFailed().failure.hasMessageContaining("[person]") }
    }

    @Test
    fun `registers no metrics without Micrometer`() {
        withoutMetricsSetup
            .withClassLoader(FilteredClassLoader(MeterBinder::class.java))
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context).doesNotHaveBean("flytCacheMeterBinder")
            }
    }
}
