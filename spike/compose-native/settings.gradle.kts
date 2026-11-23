rootProject.name = "compose-native-spike"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        // Official Skiko 0.156.1 for the JVM/iOS/web closure comes from JetBrains' dev repository
        // (compose-native PUBLISHING.md); the native fork of Skiko is on Maven Central.
        maven("https://packages.jetbrains.team/maven/p/cmp/dev") {
            content { includeGroup("org.jetbrains.skiko") }
        }
    }
}
