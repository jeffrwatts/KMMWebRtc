import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    kotlin("plugin.serialization")
    id("com.android.library")
}

version = "1.0"

kotlin {
    android()
    //iosX64()
    //iosArm64()
    //iosSimulatorArm64() sure all ios dependencies support this target

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
        }
    }

    ios {
        binaries
            .filterIsInstance<Framework>()
            .forEach {
                it.transitiveExport = true
                it.export("com.shepeliev:webrtc-kmp:0.89.4")
            }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt"){
                    version {
                        strictly("1.6.0-native-mt")
                    }
                }
                api("com.shepeliev:webrtc-kmp:0.89.4")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
                implementation("io.ktor:ktor-client-core:1.6.7")
                implementation("io.ktor:ktor-client-serialization:1.6.7")
                implementation("dev.gitlive:firebase-firestore:1.4.3")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:1.6.7")
                implementation("io.ktor:ktor-client-okhttp:1.6.7")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-ios:1.6.7")
            }
        }
        val iosTest by getting
        //val iosX64Main by getting
        //val iosArm64Main by getting
        //val iosSimulatorArm64Main by getting
        //val iosMain by creating {
        //    dependsOn(commonMain)
        //    iosX64Main.dependsOn(this)
        //    iosArm64Main.dependsOn(this)
        //    //iosSimulatorArm64Main.dependsOn(this)
        //}
        //val iosX64Test by getting
        //val iosArm64Test by getting
        //val iosSimulatorArm64Test by getting
        //val iosTest by creating {
        //    dependsOn(commonTest)
        //    iosX64Test.dependsOn(this)
        //    iosArm64Test.dependsOn(this)
        //    //iosSimulatorArm64Test.dependsOn(this)
        //}
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 30
        targetSdk = 31
    }
}