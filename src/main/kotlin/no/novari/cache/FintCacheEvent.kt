package no.novari.cache

data class FintCacheEvent<K, V>(
    val type: EventType,
    val key: K,
    val oldValue: V?,
    val newValue: V?,
) {
    enum class EventType { CREATED, UPDATED, REMOVED, EXPIRED, EVICTED }

    override fun toString() = "Sensitive data omitted"
}
