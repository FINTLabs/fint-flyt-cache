package no.novari.cache

import java.time.Duration

/** Unset values fall back to the cache manager's defaults. */
data class FintCacheOptions(
    val timeToLive: Duration? = null,
    val heapSize: Long? = null,
)
