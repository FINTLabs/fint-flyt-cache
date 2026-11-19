# FINT Flyt Cache

In-memory caches for FLYT services, backed by [Ehcache](https://www.ehcache.org/). Spring Boot 4 auto-configures a
`FintCacheManager`.

```kotlin
val cache: FintCache<String, ArkivressursResource> =
    fintCacheManager.createCache("arkivressurs", String::class.java, ArkivressursResource::class.java)

cache.put(href, resource)
cache.getOrNull(href) // or get(href), which throws NoSuchCacheEntryException
```

| Property                                   | Default   | Used when `FintCacheOptions` leaves it unset |
| ------------------------------------------ | --------- | -------------------------------------------- |
| `novari.cache.defaultCacheEntryTimeToLive` | `6d`      | Time to live for each entry                  |
| `novari.cache.defaultCacheHeapSize`        | `1000000` | Maximum number of entries per cache          |

## Upgrading from 3.x to 4.0

- Spring Boot 4 is required.
- `getOptional(key)` is replaced by `getOrNull(key)`.
- `FintCacheOptions.builder()` is replaced by `FintCacheOptions(timeToLive = ..., heapSize = ...)`.
- `FintCacheInfo` is removed. It was not used.
- `get(keys)` leaves out keys that are missing instead of returning `null` for them.

## Dependency Locking

The build uses [Gradle dependency locking](https://docs.gradle.org/current/userguide/dependency_locking.html), so every build resolves exactly the same direct and transitive versions. The locked versions live in the committed `gradle.lockfile` files. A pull request therefore shows every transitive version change it causes. Gradle plugins are not locked, because Dependabot only updates `gradle.lockfile`; their versions are in `gradle/libs.versions.toml` instead.

**Automatic (Dependabot):** Dependabot updates the lockfiles in its own pull requests, so no manual step is needed. If two Dependabot pull requests conflict in a lockfile, Dependabot rebases the remaining one after the first is merged. If it does not, comment `@dependabot recreate` on the pull request.

**Manual:** Any change you make by hand that affects dependency resolution requires regenerating the lockfiles. That covers versions, constraints, and adding or removing a dependency:

```bash
./gradlew dependencies --write-locks
```

Commit the updated lockfiles together with the change. If you forget, the build fails both locally and in CI with an error saying the resolved version "is not part of the dependency lock state".
