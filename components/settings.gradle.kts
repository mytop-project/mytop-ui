rootProject.name = "mytop-ui-components"

// The patched compose-native (mytop-project/compose-native, attached to its releases as a Maven repository) is used when
// CN_REPO names the unpacked repository and CN_VERSION its version; without them every dev.brahmkshatriya artifact is
// upstream's, from Maven Central.
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

// mytop-ui's own modules (tokens, notices) are consumed from source: this build sits inside the repository, so the
// components are always built against the tokens of the same commit.
includeBuild("..") {
    dependencySubstitution {
        for (name in listOf("tokens", "notices")) {
            substitute(module("dev.droidtop.lib.ui:$name")).using(project(":$name"))
        }
    }
}
