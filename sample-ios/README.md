# Dhruva iOS Sample

Drop these Swift files into a fresh iOS app target in Xcode and link the Dhruva
shared framework via SPM/CocoaPods following [JetBrains' Compose Multiplatform iOS guide](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-ios-storyboard.html).

The sample is intentionally not a checked-in `.xcodeproj` so we can avoid carrying
machine-specific Xcode metadata in version control. It demonstrates one-shot and
continuous tracking via `CLLocationManager` directly; substitute the equivalent
Dhruva calls from a shared KMP module to integrate the library.
