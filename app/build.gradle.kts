import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

val projectName = "MBGA"
val androidCompileSdk = 34
val androidMinSdk = 27
val androidTargetSdk = 34
val appPackageName = "top.trangle.mbga"
val appVersionName = "1.2.0"

fun gitBranch(): String {
    val envBranch = System.getenv("BRANCH_NAME")
    if (envBranch != null) {
        return envBranch
    }
    val os = org.apache.commons.io.output.ByteArrayOutputStream()
    project.exec {
        commandLine = "git rev-parse --abbrev-ref HEAD".split(" ")
        standardOutput = os
    }
    return String(os.toByteArray()).trim()
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven { url = uri("https://api.xposed.info/") }
    maven { url = uri("https://jitpack.io") }
}
plugins {
    id("com.android.application") version "8.6.1"
    id("org.jetbrains.kotlin.android") version "2.0.0"
    id("com.google.devtools.ksp") version "2.0.0-1.0.22"
    id("org.jmailen.kotlinter") version "4.2.0"
}
android {
    namespace = appPackageName
    compileSdk = androidCompileSdk
    androidResources.additionalParameters += listOf(
        "--allow-reserved-package-id",
        "--package-id",
        "0x51"
    )

    defaultConfig {
        applicationId = appPackageName
        minSdk = androidMinSdk
        targetSdk = androidTargetSdk
        versionName = appVersionName
        versionCode = System.getenv("BUILD_ID")?.toIntOrNull() ?: 1
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    applicationVariants.all {
        applicationVariants.all {
            val variant = this
            variant.outputs
                .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                .forEach { output ->
                    output.outputFileName = "$applicationId-v$versionCode($versionName).apk"
                }
        }
    }

    val keystorePath = System.getenv("KEYSTORE")
    if (keystorePath != null) {
        signingConfigs {
            create("config") {
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
                storeFile = file(keystorePath)
                storePassword = System.getenv("STORE_PASSWORD")
            }
        }
    }
    buildTypes {
        debug {
            versionNameSuffix = "_debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("config")
        }
        create("feature") {
            initWith(getByName("release"))
            versionNameSuffix = "_feature-" + gitBranch().split("/").last()
        }
        all {
            var name = "$projectName ${defaultConfig.versionName}"
            if (versionNameSuffix != null) {
                name += versionNameSuffix
            }
            resValue("string", "app_name_with_version", name)
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xno-param-assertions", "-Xno-call-assertions", "-Xno-receiver-assertions"
        )
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    lint { checkReleaseBuilds = false }
}

dependencies {
    implementation("androidx.preference:preference-ktx:1.2.1")
    compileOnly("de.robv.android.xposed:api:82")
    implementation("com.highcapable.yukihookapi:api:1.2.1")
    ksp("com.highcapable.yukihookapi:ksp-xposed:1.2.1")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.hendraanggrian.material:collapsingtoolbarlayout-subtitle:1.5.0")
}

tasks.register<LintTask>("ktLint") {
    group = "verification"
    source(files("src"))
}

tasks.register<FormatTask>("ktFormat") {
    group = "formatting"
    source(files("src"))
}
