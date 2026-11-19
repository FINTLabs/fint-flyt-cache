package no.novari.cache.exceptions

class NoSuchCacheEntryException(
    key: String,
) : RuntimeException("No cache entry with key='$key'")
