// The palette a theme resolves to (ThemePalette, @Serializable as droidtop stores it), the focus ring's inks and the
// notice colours of a palette. Common Kotlin, tested with no device and no SDK.
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":notices"))
            api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
        }
        commonTest.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
    }
}
