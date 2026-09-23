import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
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
            "    const val VERSION_NAME = \"1.0.$c\"\n" +
            "    const val BUILD_TIMESTAMP = \"$d\"\n" +
            "}\n"
        )
    }
}

kotlin {
    compilerOptions {
        // expect/actual classes are Beta; FileStorage is one per platform and is what the flag is for.
        freeCompilerArgs.add("-Xexpect-actual-classes")
        // A warning is a finding: the build fails on one rather than letting them pile up unread.
        allWarningsAsErrors.set(true)
    }

    // The shared code as an Android library; the app itself, with its manifest, icons and signing,
    // is the androidApp module.
    android {
        namespace = "it.bosler.numeracy.composeapp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        androidResources {
            enable = true
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
    
    // Handing over the task rather than its directory makes every compilation, on every target,
    // wait for BuildConfig to be written.
    sourceSets.commonMain {
        kotlin.srcDir(generateBuildConfig)
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
            implementation(libs.kotlinx.coroutinesTest)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

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
