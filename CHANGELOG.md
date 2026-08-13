# Changelog

All notable changes to Dhruva are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and the project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed - BREAKING
- **Group ID is now `xyz.ksharma`**, was `io.github.ksharma-xyz`. It now matches
  the `xyz.ksharma.*` package names the library has always shipped. Maven Central
  coordinates cannot be moved, so 0.1.0 and 0.1.1 remain published under the old
  group and 0.2.0 onward is under the new one. Update the group when you upgrade;
  artifact names and the API are unchanged.

  ```diff
  - implementation("io.github.ksharma-xyz:dhruva-data:0.1.1")
  + implementation("xyz.ksharma:dhruva-data:0.2.0")
  ```

### Added
- Snapshot builds of `main` are now published to the Central Portal snapshot
  repository, so downstream apps can consume unreleased changes without waiting
  for a tagged release. See `docs/publishing.md` for the repository URL and the
  caching caveat.

## [0.1.1] - 2026-05-05

### Fixed
- KLIB resolver "duplicated unique_name" error when consumers depend on both
  Aagya and Dhruva on iOS. Both libraries previously published modules named
  `:state` and `:data`, which produced colliding klib unique_names like
  `io.github.ksharma-xyz:data`. The Gradle modules are now renamed to
  `:dhruva-state`, `:dhruva-data`, `:dhruva-di-koin` so each klib has a
  globally unique name.

### Changed
- `AndroidLocationTracker` no longer ships a `resolveContextActivity` helper.
  `rememberLocationTracker()` reads `LocalActivity.current`
  (`androidx.activity.compose` 1.10+) directly. Activity-compose was already
  a transitive dependency, so no change is required for consumers.

## [0.1.0] - 2026-05-04

### Added
- Initial library scaffolding.
- `Location`, `LocationConfig`, `LocationPriority`, `LocationError` value types.
- `LocationTracker` interface with `getCurrentLocation` and `startTracking`.
- Android implementation using `FusedLocationProviderClient`.
- iOS implementation using `CLLocationManager`.
- `dhruva-di-koin` Koin module factory.
- Android sample app demonstrating one-shot and continuous tracking.
- MkDocs Material docs site with concepts, recipes, and publishing guide.
