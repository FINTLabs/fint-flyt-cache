package no.novari.cache

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.convert.ApplicationConversionService
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class FintCacheConfigurationTest {
    // SpringApplication registers this in apps; it converts "6d" to a Duration.
    private val contextRunner =
        ApplicationContextRunner()
            .withInitializer { it.beanFactory.conversionService = ApplicationConversionService.getSharedInstance() }
            .withConfiguration(AutoConfigurations.of(FintCacheConfiguration::class.java))

    @Test
    fun `registers a cache manager with default properties`() {
        contextRunner.run { assertThat(it).hasSingleBean(FintCacheManager::class.java) }
    }

    @Test
    fun `registers a cache manager with overridden properties`() {
        contextRunner
            .withPropertyValues("novari.cache.defaultCacheEntryTimeToLive=1h", "novari.cache.defaultCacheHeapSize=10")
            .run { assertThat(it).hasSingleBean(FintCacheManager::class.java) }
    }
}
