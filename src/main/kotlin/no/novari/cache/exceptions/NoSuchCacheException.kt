package no.novari.cache.exceptions

class NoSuchCacheException(
    alias: String,
) : RuntimeException("No cache with alias='$alias'")
