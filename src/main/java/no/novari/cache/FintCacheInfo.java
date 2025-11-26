package no.novari.cache;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Builder
public class FintCacheInfo {
    private final String alias;
    private final long numberOfEntries;
    private final long numberOfDistinctEntries;
}
