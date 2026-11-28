plugins {
    kotlin("multiplatform") version "2.4.20"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20"
    id("org.jetbrains.compose") version "1.13.0-beta01"
    id("dev.brahmkshatriya.compose") version "1.13.0-beta01"
    // The Android library target. mytop-ui's own build: it does not depend on droidtop's Android Gradle plugin.
    id("com.android.kotlin.multiplatform.library") version "9.5.2"
}

// The patched build's version (CN_VERSION, settings.gradle.kts) or upstream's.
val composeNativeVersion = System.getenv("CN_VERSION") ?: "1.13.0-beta01"
val composeVersion = "1.13.0-beta01"
val uiLibVersion = "0.5.2" // mytop-ui's own version; the composite build ignores it

// The Compose components of mytop-ui (mytop-project/tracker#867 step 3): droidtop's pure-Compose pieces of ui-shared,
// with the same packages and the same APIs, written once in commonMain. Official Compose serves every target that has
// it; compose-native's fork serves Linux and Windows native.
kotlin {
    androidLibrary {
        namespace = "dev.droidtop.components"
        // Compose 1.13's AARs need compileSdk 37.1.
        compileSdk {
            version = release(37) { minorApiLevel = 1 }
        }
        minSdk = 26
    }
    jvm {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
    desktopNative {
        binaries.executable {
            entryPoint = "dev.droidtop.components.gallery.main"
        }
    }

    sourceSets {
        commonMain.dependencies {
            api("org.jetbrains.compose.ui:ui:$composeVersion")
            api("org.jetbrains.compose.foundation:foundation:$composeVersion")
            api("org.jetbrains.compose.material3:material3:$composeVersion")
            api("dev.droidtop.lib.ui:tokens:$uiLibVersion")
            api("dev.droidtop.lib.ui:notices:$uiLibVersion")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
        named("desktopNativeMain") {
            dependencies {
                implementation("dev.brahmkshatriya.compose.ui:ui:$composeNativeVersion")
                implementation("dev.brahmkshatriya.compose.foundation:foundation:$composeNativeVersion")
                implementation("dev.brahmkshatriya.compose.material3:material3:$composeNativeVersion")
                implementation("dev.brahmkshatriya.compose.desktop:desktop-native:$composeNativeVersion")
            }
        }
    }
}

composeNativeApplication {
    applicationName.set("MytopComponentsGallery")
    packageName.set("dev.droidtop.components")
    executableName.set("components-gallery")
}

// A test binary of a Compose Native target links the window library (SDL3) as the executable does; the plugin gives
// it to executables only, so the Linux test binary gets it here.
kotlin.targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
    .matching { it.name == "linuxX64" }
    .configureEach {
        binaries.withType<org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable>().configureEach {
            System.getenv("LINUX_X64_NATIVE_LIBRARY_DIRS")?.let { linkerOpts("-L$it") }
            linkerOpts("-lSDL3")
        }
    }
