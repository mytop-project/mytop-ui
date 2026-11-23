// The notice colours (README.md): common Kotlin with no platform types, so they are tested with no device and no SDK, on
// the JVM and on every native target. Products compile these sources into their own modules (droidtop:
// runtime-common's srcDir, notices/src/commonMain/kotlin) or use the jvm artifact.
plugins {
    kotlin("multiplatform")
}
