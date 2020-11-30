plugins {
    kotlin("multiplatform") version "1.4.20"
}

group = "me.ehmeed"
version = "1.0-SNAPSHOT"
val ktorVersion = "1.4.2"

repositories {
    mavenCentral()
//    maven { setUrl("https://dl.bintray.com/kotlin/kotlinx") }
//    maven { setUrl("https://dl.bintray.com/kotlin/ktor") }
}

kotlin {
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
        val nativeMain by getting
        val nativeTest by getting
    }
}

dependencies {
    commonMainImplementation("io.ktor:ktor-client-core:$ktorVersion")
    commonMainImplementation("io.ktor:ktor-client-curl:$ktorVersion")
    commonMainImplementation("com.github.ajalt.clikt:clikt:3.0.1")
}
