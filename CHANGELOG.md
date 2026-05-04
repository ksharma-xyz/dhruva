# Changelog

All notable changes to Dhruva are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and the project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial library scaffolding.
- `Location`, `LocationConfig`, `LocationPriority`, `LocationError` value types.
- `LocationTracker` interface with `getCurrentLocation` and `startTracking`.
- Android implementation using `FusedLocationProviderClient`.
- iOS implementation using `CLLocationManager`.
- `dhruva-di-koin` Koin module factory.
- Android sample app demonstrating one-shot and continuous tracking.
- MkDocs Material docs site with concepts, recipes, and publishing guide.
