package no.novari.cache.ehcache

import no.novari.cache.FintCacheEvent
import no.novari.cache.FintCacheEvent.EventType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FintEhCacheEventLogger<K, V>(
    private val cacheAlias: String,
) : FintEhCacheEventListener<K, V>() {
    override fun onEvent(event: FintCacheEvent<K, V>) =
        when (event.type) {
            EventType.CREATED -> {
                log.info("Cache entry in '{}' with key='{}' created with value={}", cacheAlias, event.key, event.newValue)
            }

            EventType.UPDATED -> {
                log.info(
                    "Cache entry in '{}' with key='{}' updated from {} to {}",
                    cacheAlias,
                    event.key,
                    event.oldValue,
                    event.newValue,
                )
            }

            EventType.REMOVED -> {
                log.info("Cache entry in '{}' with key='{}' removed", cacheAlias, event.key)
            }

            EventType.EVICTED -> {
                log.info("Cache entry in '{}' with key='{}' evicted", cacheAlias, event.key)
            }

            EventType.EXPIRED -> {
                log.info("Cache entry in '{}' with key='{}' expired", cacheAlias, event.key)
            }
        }

    private companion object {
        private val log: Logger = LoggerFactory.getLogger(FintEhCacheEventLogger::class.java)
    }
}
