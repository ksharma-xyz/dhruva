plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidLibrary {
        namespace = "xyz.ksharma.dhruva.sample"
        compileSdk = libs.versions.android.compile.sdk.get().toInt()
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }

    listOf(iosArm64(), iosSimulatorArm64(), iosX64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "DhruvaSample"
            isStatic = true
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.dhruvaData)
            implementation(projects.dhruvaState)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}
