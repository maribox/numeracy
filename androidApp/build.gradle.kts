import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

// Play accepts an upload only when the version code is higher than the last one, so it counts the
// commits on the branch. The name carries the same number, as Settings shows it.
val commitCount = providers.exec { commandLine("git", "rev-list", "--count", "HEAD") }
    .standardOutput.asText.map { it.trim() }

// The release key's passwords come from the environment and nowhere else: a default written here
// would be the password of the key that signs the published app, sitting in a public repository.
val keystoreFile = rootProject.file("release.jks")
val signingSecrets = listOf("KEYSTORE_PASSWORD", "KEY_ALIAS", "KEY_PASSWORD")
    .associateWith { System.getenv(it) }
val canSignRelease = keystoreFile.exists() && signingSecrets.values.all { !it.isNullOrBlank() }

// Building a release without them would quietly produce an APK signed with the debug key, which
// installs over nothing and is refused by Play, so a release build stops instead.
gradle.taskGraph.whenReady {
    val releasing = allTasks.any { it.project == project && (it.name == "assembleRelease" || it.name == "bundleRelease") }
    if (releasing && !canSignRelease) {
        val missing = signingSecrets.filterValues { it.isNullOrBlank() }.keys
        throw GradleException(
            if (!keystoreFile.exists()) "release.jks is missing, so this release cannot be signed."
            else "release.jks is present but ${missing.joinToString(", ")} is not set."
        )
    }
}

android {
    namespace = "it.bosler.numeracy"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "it.bosler.numeracy"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = commitCount.get().toInt()
        versionName = "1.0.${commitCount.get()}"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            if (canSignRelease) {
                storeFile = keystoreFile
                storePassword = signingSecrets["KEYSTORE_PASSWORD"]
                keyAlias = signingSecrets["KEY_ALIAS"]
                keyPassword = signingSecrets["KEY_PASSWORD"]
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = if (canSignRelease) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        allWarningsAsErrors.set(true)
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}
