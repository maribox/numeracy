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
            "    const val VERSION_NAME = \"0.$c\"\n" +
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
        versionCode = 1
        versionName = "1.0"
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
            val ksFile = rootProject.file("release.jks")
            if (ksFile.exists()) {
                storeFile = ksFile
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "numeracy123"
                keyAlias = System.getenv("KEY_ALIAS") ?: "numeracy"
                keyPassword = System.getenv("KEY_PASSWORD") ?: "numeracy123"
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = if (rootProject.file("release.jks").exists())
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
