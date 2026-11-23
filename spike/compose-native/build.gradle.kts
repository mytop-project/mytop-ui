plugins {
    kotlin("multiplatform") version "2.4.20"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20"
    id("org.jetbrains.compose") version "1.13.0-beta01"
    id("dev.brahmkshatriya.compose") version "1.13.0-beta01"
}

// The patched build's version (CN_VERSION, settings.gradle.kts) or upstream's.
val composeNativeVersion = System.getenv("CN_VERSION") ?: "1.13.0-beta01"
val composeVersion = "1.13.0-beta01"

kotlin {
    desktopNative {
        binaries.executable {
            entryPoint = "dev.mytop.spike.main"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.compose.ui:ui:$composeVersion")
            implementation("org.jetbrains.compose.foundation:foundation:$composeVersion")
            implementation("org.jetbrains.compose.material3:material3:$composeVersion")
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
    applicationName.set("Mytop Compose Native Spike")
    packageName.set("dev.mytop.spike")
    executableName.set("mytop-spike")
}
