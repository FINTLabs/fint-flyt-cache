# FLYT Cache

In-memory caches for FLYT services, backed by [Caffeine](https://github.com/ben-manes/caffeine).

```kotlin
@Bean
fun personResourceCache(): FlytCache<String, PersonResource> = FlytCache("person")

cache.put(href, resource)
cache.getOrNull(href) // or get(href), which throws NoSuchCacheEntryException
```

Entries expire 6 days after they were last written, and each cache holds at most 1 000 000 entries. Override with
`FlytCache("person", timeToLive = Duration.ofHours(1), maximumSize = 10_000)`. Aliases of `FlytCache` beans must be unique,
or the app fails at startup.

Requires Spring Boot 4.2 or newer and Java 25 or newer (the published bytecode is Java 25).

The architecture is described in [docs/architecture.md](docs/architecture.md).

## Metrics

With Micrometer on the classpath, every `FlytCache` bean gets the standard Caffeine metrics (`cache.gets` with
`result=hit|miss`, `cache.size`, `cache.evictions`, ...) tagged with `cache=<alias>`. Caches that are not beans get no
metrics.

## Upgrading from 3.x to 4.0

- Spring Boot 4.2 and Java 25 are required, and the value type must be non-null (`<V : Any>`).
- `FintCache` is renamed to `FlytCache`, and the package `no.novari.cache` to `no.novari.flyt.cache`.
- `FintCacheManager` is removed. Create caches with `FlytCache<K, V>(alias)` in `@Bean` methods instead of
  `fintCacheManager.createCache(alias, K::class.java, V::class.java)`.
- `novari.cache.*` properties and `FintCacheOptions` are removed. Pass `timeToLive` and `maximumSize` to the
  constructor.
- Renamed: `getOptional(key)` to `getOrNull(key)`, `getAll()` to `values()`, `getAllDistinct()` to `distinctValues()`,
  and `getNumberOfEntries()` to `size`.
- Removed: cache events and listeners, `getNumberOfDistinctValues()`, `NoSuchCacheException` and `FintCacheInfo`.
  Remove the `no.novari.cache.ehcache.FintEhCacheEventLogger` log level from `application.yaml`.
- `get(keys)` leaves out keys that are missing instead of returning `null` for them.

## Dependency Locking

The build uses [Gradle dependency locking](https://docs.gradle.org/current/userguide/dependency_locking.html), so every build resolves exactly the same direct and transitive versions. The locked versions live in the committed `gradle.lockfile` files. A pull request therefore shows every transitive version change it causes. Gradle plugins are not locked, because Dependabot only updates `gradle.lockfile`; their versions are in `gradle/libs.versions.toml` instead.

**Automatic (Dependabot):** Dependabot updates the lockfiles in its own pull requests, so no manual step is needed. If two Dependabot pull requests conflict in a lockfile, Dependabot rebases the remaining one after the first is merged. If it does not, comment `@dependabot recreate` on the pull request.

**Manual:** Any change you make by hand that affects dependency resolution requires regenerating the lockfiles. That covers versions, constraints, and adding or removing a dependency:

```bash
./gradlew dependencies --write-locks
```

Commit the updated lockfiles together with the change. If you forget, the build fails both locally and in CI with an error saying the resolved version "is not part of the dependency lock state".
