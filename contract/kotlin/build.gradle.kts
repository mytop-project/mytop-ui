plugins {
    kotlin("multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Answers are JSON; the library only (no serialization plugin or generated code).
            api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
    }
}

// The fixtures every implementation shares are read from a file, so that test is JVM-only (jvmTest).
tasks.named<Test>("jvmTest") {
    systemProperty("contract.fixtures", rootProject.file("contract/fixtures/answers.json").absolutePath)
}
