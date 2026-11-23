rootProject.name = "compose-native-spike"

// A patched build of compose-native (mytop-project/compose-native, branch mytop, attached to its releases
// as a Maven repository) is used when CN_REPO names the unpacked repository and CN_VERSION its version;
// without them every dev.brahmkshatriya artifact is upstream's, from Maven Central.
pluginManagement {
    val patchedRepo: String? = System.getenv("CN_REPO")
    val patchedVersion: String? = System.getenv("CN_VERSION")
    repositories {
        if (patchedRepo != null) {
            maven {
                url = java.io.File(patchedRepo).toURI()
                content { includeGroupByRegex("dev\\.brahmkshatriya.*") }
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (patchedVersion != null && requested.id.id == "dev.brahmkshatriya.compose") useVersion(patchedVersion)
        }
    }
}

val patchedRepo: String? = System.getenv("CN_REPO")

dependencyResolutionManagement {
    repositories {
        if (patchedRepo != null) {
            maven {
                url = java.io.File(patchedRepo).toURI()
                content { includeGroupByRegex("dev\\.brahmkshatriya.*") }
            }
        }
        mavenCentral()
        google()
        // Official Skiko 0.156.1 for the JVM/iOS/web closure comes from JetBrains' dev repository
        // (compose-native PUBLISHING.md); the native fork of Skiko is on Maven Central.
        maven("https://packages.jetbrains.team/maven/p/cmp/dev") {
            content { includeGroup("org.jetbrains.skiko") }
        }
    }
}
