plugins {
    kotlin("multiplatform") version "2.4.20" apply false
    kotlin("plugin.serialization") version "2.4.20" apply false
    // Every module's public API is a checked-in dump (<module>/api/<module>.api, the JVM target's); apiCheck fails on any
    // difference (Droidtop/tracker#867: stable library interfaces). scripts/check-api-compat.sh adds the version rule.
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.18.2"
}

subprojects {
    group = "dev.droidtop.lib.ui"
    version = rootProject.property("lib.version") as String
}

// One target set for every module (Droidtop/tracker#958): the JVM (Android consumers use it, or compile notices as
// sources), and Kotlin/Native for the compose-native app: Linux, Windows and macOS. Code lives in commonMain.
subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
            jvm {
                compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
            linuxX64()
            linuxArm64()
            mingwX64()
            macosArm64()
            macosX64()
            sourceSets.getByName("commonTest").dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
