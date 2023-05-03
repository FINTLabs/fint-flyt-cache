package no.fintlabs.cache;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@Builder
public class FintCacheInfo {
    private final String alias;
    private final long numberOfEntries;
    private final long numberOfDistinctEntries;
}
