package no.novari.cache.ehcache

import no.novari.cache.FintCacheEvent
import no.novari.cache.FintCacheEventListener
import org.ehcache.event.CacheEvent
import org.ehcache.event.CacheEventListener
import org.ehcache.event.EventType

abstract class FintEhCacheEventListener<K, V> :
    FintCacheEventListener<K, V>,
    CacheEventListener<K, V> {
    override fun onEvent(event: CacheEvent<out K, out V>) =
        onEvent(FintCacheEvent(event.type.toFintEventType(), event.key, event.oldValue, event.newValue))

    private fun EventType.toFintEventType() =
        when (this) {
            EventType.CREATED -> FintCacheEvent.EventType.CREATED
            EventType.UPDATED -> FintCacheEvent.EventType.UPDATED
            EventType.REMOVED -> FintCacheEvent.EventType.REMOVED
            EventType.EXPIRED -> FintCacheEvent.EventType.EXPIRED
            EventType.EVICTED -> FintCacheEvent.EventType.EVICTED
        }
}
