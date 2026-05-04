# Contributing to Dhruva

Thanks for considering a contribution.

## Before you start work

1. **Open an issue first.** For anything beyond a typo, please open an issue before writing code so we can agree on the shape of the change.
2. Read the [architecture doc](docs/concepts/architecture.md) to understand the lifecycle and threading guarantees.

## Local setup

```bash
git clone https://github.com/ksharma-xyz/dhruva.git
cd dhruva
./gradlew build
```

Sample app:

```bash
./gradlew :sample-android:installDebug
```

## What goes where

- **`state/`**, pure Kotlin types (`Location`, `LocationConfig`, `LocationError`). No platform imports.
- **`data/`**, `LocationTracker` and its Android / iOS implementations.
- **`di-koin/`**, optional Koin DI integration.
- **`sample-android/`, `sample-ios/`**, runnable apps that exercise the public API.
- **`docs/`**, MkDocs Material site source.

## Coding style

- Detekt enforced in CI.
- Public API: KDoc on every public symbol, with at least one example.
- No `Throwable` across the public API. Use sealed `LocationError`.

## Testing

- `./gradlew check` runs unit tests + lint + detekt.
- The Android sample exercises `FusedLocationProviderClient` end-to-end on a real device.

## Pull requests

- Title: imperative mood, scoped (e.g. `data: seed startTracking from last known location on iOS`).
- Description: link the issue, describe the user-visible change, call out breaking changes.
- Keep PRs small.

## Releasing (maintainers only)

See [docs/publishing.md](docs/publishing.md).
