plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.publish)
    alias(libs.plugins.dokka)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidLibrary {
        namespace = "xyz.ksharma.dhruva.location.state"
        compileSdk = libs.versions.android.compile.sdk.get().toInt()
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    jvm()

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Pure Kotlin. No runtime deps.
        }
        commonTest.dependencies {
            implementation(libs.test.kotlin)
        }
    }
}

