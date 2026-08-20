plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.vanniktech.publish) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

allprojects {
    group = providers.gradleProperty("GROUP").get()
    version = providers.gradleProperty("VERSION_NAME").get()
}

// Detekt was declared in the version catalog but never applied to any module, so
// `./gradlew detekt` failed with "Task 'detekt' not found" on every CI run. The
// step was marked continue-on-error, so that failure was reported and discarded
// for the lifetime of the workflow. Applying it here makes the task real.
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        // Every source set, not just the JVM ones: this is a KMP project and the
        // iOS/common code is where most of the logic lives.
        source.setFrom(files("src"))
        parallel = true
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        jvmTarget = "21"
        reports {
            html.required.set(true)
            xml.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }
}

// Coverage is host-only by construction: Kover instruments JVM bytecode, so it sees
// commonTest and androidUnitTest and has nothing to say about iosSimulatorArm64Test.
// Kotlin/Native is not supported upstream. The iOS actuals in :dhruva-data therefore read as
// uncovered even when the iOS suite exercises them - read the number as "coverage of the
// shared logic", not of the library as a whole.
//
// Modules are enrolled by whether they actually have test sources, not by a hand-kept list,
// so a new module with tests is measured without anyone remembering to add it here. Modules
// with no tests are deliberately left out: including them would report their production code
// as 0% covered, which is true but drowns the signal from the modules that do have suites.
//
// The plugin has to be applied to each measured module as well as the root - the root
// `kover` configuration resolves a `kover`-usage variant that only a Kover-enabled project
// publishes. Aggregating a module without it fails with "No matching variant".
val coverageTestSourceDirs = listOf("commonTest", "androidUnitTest", "androidHostTest", "jvmTest")

subprojects {
    val hasTestSources = coverageTestSourceDirs.any { dir ->
        file("src/$dir").let { it.isDirectory && it.walkTopDown().any { f -> f.extension == "kt" } }
    }
    if (!hasTestSources) return@subprojects

    apply(plugin = "org.jetbrains.kotlinx.kover")
    rootProject.dependencies.add("kover", this)
}
