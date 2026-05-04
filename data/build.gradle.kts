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
        namespace = "xyz.ksharma.dhruva.location.data"
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
            api(projects.state)
            implementation(libs.compose.runtime)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.kotlinx.coroutines.play.services)
            api(libs.play.services.location)
        }
        commonTest.dependencies {
            implementation(libs.test.kotlin)
            implementation(libs.test.coroutines)
            implementation(libs.test.turbine)
        }
    }
}

