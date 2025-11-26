package no.novari.cache;

public interface FintCacheEventListener<K, V> {

    void onEvent(FintCacheEvent<K, V> event);

}
