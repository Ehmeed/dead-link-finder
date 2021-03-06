plugins {
    kotlin("multiplatform") version "1.4.31"
}

group = "org.ehmeed"
version = "1.1.0"

val kotlinVersion = "1.5.0"
// ktor 1.5 fails on mutability of frozen client
val ktorVersion = "1.4.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            dependsOn("createProperties")
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
            }
        }
        tasks.withType<Jar> {
            manifest {
                attributes["Main-Class"] = "JvmMainKt"
            }
            from({
                configurations["jvmRuntimeClasspath"].filter { it.name.endsWith("jar") }.map { zipTree(it) }
            })
        }
    }

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS $hostOs is not supported in Kotlin/Native.")
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

tasks.register("createProperties") {
    doFirst {
        val version = project.version.toString()
        val props = File("$buildDir/../src/commonMain/kotlin/version.kt")
        props.bufferedWriter().use {
                it.append("""const val VERSION = "$version"""")
            }
    }
}

// example how to declare common dependency for all modules
// dependencies {
//    commonMainImplementation("com.github.ajalt.clikt:clikt:3.0.1")
// }
