

plugins {
    kotlin("multiplatform") version "1.4.20"
}

group = "me.ehmeed"
version = "1.0-SNAPSHOT"

val kotlinVersion = "1.4.20"
val ktorVersion = "1.4.2"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            kotlinOptions { jvmTarget = "1.8" }
        }
    }

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("com.github.ajalt.clikt:clikt:3.0.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:$kotlinVersion")
            }
        }
        val nativeMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-apache:$ktorVersion")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
            }
        }
    }
}

// example how to declare common dependency for all modules
//dependencies {
//    commonMainImplementation("com.github.ajalt.clikt:clikt:3.0.1")
//}
