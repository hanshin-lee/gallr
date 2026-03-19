import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File

// Locate the NMapsMap.xcframework resolved by Xcode's SPM.
// Returns the path to the correct slice directory for a given target name.
fun nmapsFrameworkSlice(slice: String): String {
    val derivedData = File(System.getProperty("user.home"), "Library/Developer/Xcode/DerivedData")
    val xcframework = derivedData.walkTopDown()
        .maxDepth(12)
        .firstOrNull { it.isDirectory && it.name == "NMapsMap.xcframework" && !it.path.contains("__MACOSX") }
        ?: error(
            "NMapsMap.xcframework not found in DerivedData.\n" +
            "Add the SPM package 'https://github.com/navermaps/SPM-NMapsMap' in Xcode " +
            "and do one Xcode build to resolve it."
        )
    return xcframework.resolve(slice).absolutePath
}

// Returns the SDK sysroot via xcrun so cinterop uses the correct system headers.
fun xcrunSdkPath(sdk: String): String =
    ProcessBuilder("xcrun", "--sdk", sdk, "--show-sdk-path")
        .start()
        .inputStream
        .bufferedReader()
        .readLine()
        ?.trim()
        ?: error("xcrun failed to locate SDK: $sdk")

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64 {
        binaries.framework { baseName = "composeApp"; isStatic = true }
        compilations.getByName("main") {
            val NMapsMap by cinterops.creating {
                definitionFile.set(project.file("src/nativeInterop/cinterop/NMapsMap.def"))
                compilerOpts("-F", nmapsFrameworkSlice("ios-arm64_x86_64-simulator"), "-isysroot", xcrunSdkPath("iphonesimulator"), "-fno-modules")
            }
        }
    }
    iosArm64 {
        binaries.framework { baseName = "composeApp"; isStatic = true }
        compilations.getByName("main") {
            val NMapsMap by cinterops.creating {
                definitionFile.set(project.file("src/nativeInterop/cinterop/NMapsMap.def"))
                compilerOpts("-F", nmapsFrameworkSlice("ios-arm64"), "-isysroot", xcrunSdkPath("iphoneos"), "-fno-modules")
            }
        }
    }
    iosSimulatorArm64 {
        binaries.framework { baseName = "composeApp"; isStatic = true }
        compilations.getByName("main") {
            val NMapsMap by cinterops.creating {
                definitionFile.set(project.file("src/nativeInterop/cinterop/NMapsMap.def"))
                compilerOpts("-F", nmapsFrameworkSlice("ios-arm64_x86_64-simulator"), "-isysroot", xcrunSdkPath("iphonesimulator"), "-fno-modules")
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.kotlinx.datetime)
            implementation(project(":shared"))
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.activity.compose)
            implementation(libs.datastore.preferences.core)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.naver.map.sdk)
            implementation(libs.naver.map.compose)
        }
    }
}

android {
    namespace = "com.gallr.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.gallr.app"
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

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
