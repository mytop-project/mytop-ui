// The one formatter for every size, duration, count and time a person reads (droidtop SPEC "Values"). Common Kotlin;
// the only dependency is kotlinx-datetime, for the zone a clock time is read in.
plugins {
    kotlin("multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
        }
    }
}
