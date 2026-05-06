# Dhruva iOS Sample

A native iOS app that hosts the **shared Compose Multiplatform** sample from
the `:sample` module. Same UI as the Android sample, written once in Kotlin
and rendered on iOS via Compose Multiplatform.

## Run it

1. Open the Xcode project:
   ```bash
   open iosApp/iosApp.xcodeproj
   ```
2. Pick a Simulator (iPhone 15 Pro or similar) or a connected device.
3. Hit ⌘R.

The build phase invokes `./gradlew :sample:embedAndSignAppleFrameworkForXcode`
to compile the shared Compose code into a `.framework`, embeds it in the app,
and Swift's `ContentView.swift` hosts the resulting `UIViewController` from
the Kotlin side via `IosEntryKt.SampleViewController()`.

## What you should see

- The same single screen as the Android sample: an animated polestar at the
  centre that pulses when a new fix arrives, live coordinates with monospace
  font, accuracy / speed / bearing stats, and a recent-trail mini-canvas
  showing the last few positions plotted relative to the latest fix.
- Two buttons: a one-shot "Get my location" and "Start continuous tracking"
  for streaming. Streaming flips into "Stop streaming" when active, with a
  blinking status pill in the header.
- Errors (denied permission, services off, timeout, platform error) surface
  as a banner card explaining the `LocationError` type.

## Note on permissions

Dhruva intentionally doesn't ship a permission flow. The Info.plist requests
`NSLocationWhenInUseUsageDescription`, but the system prompt is triggered by
the underlying `CLLocationManager` on the first call. If permission is
denied, the sample shows a banner suggesting you pair Dhruva with
[Aagya](https://github.com/ksharma-xyz/aagya) for the permission flow, or
grant manually in Settings.

## Troubleshooting

- **"Cannot find 'IosEntryKt' in scope"**: the framework wasn't built. Force
  the gradle build phase to run by cleaning (⇧⌘K) and rebuilding (⌘B).
- **"Failed to find or build framework DhruvaSample"**: run
  `./gradlew :sample:linkDebugFrameworkIosSimulatorArm64` from the repo root
  by hand and look at the gradle output for the real error.
- **Code signing errors**: set `TEAM_ID` in
  `iosApp/Configuration/Config.xcconfig` to your Apple Developer team, or
  switch to a Simulator (which doesn't need signing).

## Configuration

- Bundle ID, marketing version, deployment target: `iosApp/Configuration/Config.xcconfig`.
- App Info plist (with `NSLocationWhenInUseUsageDescription`): `iosApp/Info.plist`.
