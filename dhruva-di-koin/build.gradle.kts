plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.publish)
    alias(libs.plugins.dokka)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidLibrary {
        namespace = "xyz.ksharma.dhruva.location.di"
        compileSdk = libs.versions.android.compile.sdk.get().toInt()
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.dhruvaState)
            api(projects.dhruvaData)
            api(libs.koin.core)
            implementation(libs.koin.compose)
        }
    }
}

