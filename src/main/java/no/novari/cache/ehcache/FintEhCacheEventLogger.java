package no.novari.cache.ehcache;

import lombok.extern.slf4j.Slf4j;
import no.novari.cache.FintCacheEvent;

@Slf4j
public class FintEhCacheEventLogger<K, V> extends FintEhCacheEventListener<K, V> {

    private final String cacheAlias;

    public FintEhCacheEventLogger(String cacheAlias) {
        this.cacheAlias = cacheAlias;
    }

    @Override
    public void onEvent(FintCacheEvent<K, V> event) {
        switch (event.getType()) {
            case CREATED -> log.info("Cache entry in '{}' with key='{}' created with value={}",
                    this.cacheAlias, event.getKey(), event.getNewValue());
            case UPDATED -> log.info("Cache entry in '{}' with key='{}' updated from {} to {}",
                    this.cacheAlias, event.getKey(), event.getOldValue(), event.getNewValue());
            case REMOVED -> log.info("Cache entry in '{}' with key='{}' removed", this.cacheAlias, event.getKey());
            case EVICTED -> log.info("Cache entry in '{}' with key='{}' evicted", this.cacheAlias, event.getKey());
            case EXPIRED -> log.info("Cache entry in '{}' with key='{}' expired", this.cacheAlias, event.getKey());
        }
    }
}
