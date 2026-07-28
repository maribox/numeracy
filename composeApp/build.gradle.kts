import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

// Generate BuildConfig with git info
val gitHashProvider = providers.exec { commandLine("git", "rev-parse", "--short", "HEAD") }
    .standardOutput.asText.map { it.trim() }
val gitCommitCountProvider = providers.exec { commandLine("git", "rev-list", "--count", "HEAD") }
    .standardOutput.asText.map { it.trim() }
val buildDateProvider = providers.exec { commandLine("date", "+%Y-%m-%d %H:%M:%S") }
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
    val releasing = allTasks.any { it.name == "assembleRelease" || it.name == "bundleRelease" }
    if (releasing && !canSignRelease) {
        val missing = signingSecrets.filterValues { it.isNullOrBlank() }.keys
        throw GradleException(
            if (!keystoreFile.exists()) "release.jks is missing, so this release cannot be signed."
            else "release.jks is present but ${missing.joinToString(", ")} is not set."
        )
    }
}

// One version string for the store listing, the APK and the About screen, counted from the commits
// on the branch: Play refuses an upload whose code is not higher than the last one.
val appVersionCode = gitCommitCountProvider.get().toInt()
val appVersionName = "1.0.$appVersionCode"

val generateBuildConfig = tasks.register("generateBuildConfig") {
    val outputDir = layout.buildDirectory.dir("generated/buildconfig")
    val hash = gitHashProvider
    val count = gitCommitCountProvider
    val date = buildDateProvider
    outputs.dir(outputDir)
    inputs.property("gitHash", hash)
    inputs.property("commitCount", count)
    inputs.property("buildDate", date)
    doLast {
        val h = hash.get()
        val c = count.get()
        val d = date.get()
        val dir = outputDir.get().asFile.resolve("it/bosler/numeracy")
        dir.mkdirs()
        dir.resolve("BuildConfig.kt").writeText(
            "package it.bosler.numeracy\n\n" +
            "object BuildConfig {\n" +
            "    const val GIT_HASH = \"$h\"\n" +
            "    const val BUILD_NUMBER = \"$c\"\n" +
            "    const val VERSION_NAME = \"1.0.$c\"\n" +
            "    const val BUILD_TIMESTAMP = \"$d\"\n" +
            "}\n"
        )
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm()
    
    js {
        browser()
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    
    sourceSets.commonMain {
        kotlin.srcDir(layout.buildDirectory.dir("generated/buildconfig"))
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

android {
    namespace = "it.bosler.numeracy"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "it.bosler.numeracy"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        // Play accepts an upload only when the code is higher than the last one, so it counts
        // commits rather than being typed. The name is the same number, so the version in Settings
        // and the version in the store are one string.
        versionCode = appVersionCode
        versionName = appVersionName
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        getByName("debug") {
            // Uses default debug keystore
        }
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
            signingConfig = if (canSignRelease)
                signingConfigs.getByName("release")
            else
                signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

tasks.matching { it.name.startsWith("compileKotlin") || it.name.startsWith("compile") && it.name.contains("Kotlin") }
    .configureEach { dependsOn("generateBuildConfig") }

// Writes what the renderer needs on its class path, so make-renders.sh can start several JVMs at
// once: Compose draws one screen at a time in a process, and there are a hundred and thirty of them.
tasks.register("galleryClasspath") {
    group = "numeracy"
    description = "Write the renderer's class path to build/gallery-classpath.txt"
    val jvmMain = kotlin.targets.getByName("jvm").compilations.getByName("main")
    dependsOn(jvmMain.compileAllTaskName)
    val output = layout.buildDirectory.file("gallery-classpath.txt")
    val entries = jvmMain.output.allOutputs + configurations.getByName("jvmRuntimeClasspath")
    outputs.file(output)
    doLast { output.get().asFile.writeText(entries.asPath) }
}

// Draws every screen off-screen to build/gallery/*.png, which is what docs/model shows.
tasks.register<JavaExec>("renderGallery") {
    group = "numeracy"
    description = "Render every view and state to build/gallery/*.png"
    val jvmMain = kotlin.targets.getByName("jvm").compilations.getByName("main")
    dependsOn(jvmMain.compileAllTaskName)
    classpath = jvmMain.output.allOutputs + configurations.getByName("jvmRuntimeClasspath")
    mainClass.set("it.bosler.numeracy.gallery.GalleryKt")
    systemProperty("gallery.out", layout.buildDirectory.dir("gallery").get().asFile.absolutePath)
    systemProperty("gallery.homes", layout.buildDirectory.dir("gallery-home").get().asFile.absolutePath)
    // Draw one screen, or one shape, while iterating on it: -Ponly=practice -Pshapes=phone
    (findProperty("only") as String?)?.let { systemProperty("gallery.only", it) }
    (findProperty("shapes") as String?)?.let { systemProperty("gallery.shapes", it) }
    (findProperty("themes") as String?)?.let { systemProperty("gallery.themes", it) }
    (findProperty("frames") as String?)?.let { systemProperty("gallery.frames", it) }
    (findProperty("phoneHeight") as String?)?.let { systemProperty("gallery.phoneHeight", it) }
    systemProperty("java.awt.headless", "true")
    systemProperty("skiko.renderApi", "SOFTWARE")
}

compose.desktop {
    application {
        mainClass = "it.bosler.numeracy.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "it.bosler.numeracy"
            packageVersion = "1.0.0"
        }
    }
}
