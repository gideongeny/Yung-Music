import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.ui)
    implementation(compose.foundation)
    implementation(compose.components.resources)
    
    implementation(libs.coroutines.swing)

    // Networking (for streaming URL resolution)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.serialization.json)
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Image loading
    // (Coil removed temporarily to debug UnsatisfiedLinkError)

    // Audio Playback (VLC when installed; bundled FFmpeg otherwise)
    implementation("uk.co.caprica:vlcj:4.8.2")
    implementation("org.bytedeco:javacv:1.5.11")
    implementation("org.bytedeco:ffmpeg-platform:7.1-1.5.11")
    // Backend modules
    implementation(project(":innertube"))
    implementation(project(":kugou"))
    implementation(project(":lrclib"))
    implementation(project(":betterlyrics"))
    implementation(project(":simpmusic"))
}

compose.desktop {
    application {
        mainClass = "com.gideongeng.music.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "YungMusic"
            packageVersion = "1.0.2"
            includeAllModules = true
            modules("jdk.httpserver", "jdk.unsupported", "jdk.crypto.ec")
            windows {
                iconFile.set(project.file("src/main/resources/icon.ico"))
            }
            macOS {
                iconFile.set(project.file("src/main/resources/icon.png"))
            }
            linux {
                iconFile.set(project.file("src/main/resources/icon.png"))
            }
        }
        buildTypes.release.proguard {
            isEnabled.set(false)
        }
    }
}
