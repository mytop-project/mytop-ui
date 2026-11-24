plugins {
    kotlin("multiplatform") version "2.4.20"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20"
    id("org.jetbrains.compose") version "1.13.0-beta01"
    id("dev.brahmkshatriya.compose") version "1.13.0-beta01"
}

// The patched build's version (CN_VERSION, settings.gradle.kts) or upstream's.
val composeNativeVersion = System.getenv("CN_VERSION") ?: "1.13.0-beta01"
val composeVersion = "1.13.0-beta01"
val uiLibVersion = "0.4.0" // mytop-ui's own version; the composite build ignores it

// The Compose components of mytop-ui (mytop-project/tracker#867 step 3): droidtop's pure-Compose pieces of ui-shared,
// with the same packages and the same APIs, written once in commonMain. Official Compose serves every target that has
// it; compose-native's fork serves Linux and Windows native.
kotlin {
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
