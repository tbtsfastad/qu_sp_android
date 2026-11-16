plugins {
    id("com.android.library") version "9.1.1"
    `maven-publish`
}

group = "com.github.tbtsfastad"
version = System.getenv("VERSION") ?: "1.0.0-SNAPSHOT"

android {
    namespace = "com.example.qu_sp"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                groupId = project.group.toString()
                artifactId = "qu_sp_android"
                version = project.version.toString()
            }
        }
    }
}
