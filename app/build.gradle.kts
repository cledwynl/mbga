import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    autowire(libs.plugins.android.application)
    autowire(libs.plugins.kotlin.android)
    autowire(libs.plugins.kotlin.ksp)
    autowire(libs.plugins.kotlin.linter)
}
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
android {
    namespace = property.project.app.packageName
    compileSdk = property.project.android.compileSdk
    androidResources.additionalParameters += listOf(
        "--allow-reserved-package-id",
        "--package-id",
        "0x51"
    )

    defaultConfig {
        applicationId = property.project.app.packageName
        minSdk = property.project.android.minSdk
        targetSdk = property.project.android.targetSdk
        versionName = property.project.app.versionName
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
            var name = "${property.project.name} ${defaultConfig.versionName}"
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
    // TODO Please visit https://highcapable.github.io/YukiHookAPI/en/api/special-features/host-inject
    // TODO 请参考 https://highcapable.github.io/YukiHookAPI/zh-cn/api/special-features/host-inject
    // androidResources.additionalParameters += listOf("--allow-reserved-package-id", "--package-id", "0x64")
}

dependencies {
    implementation("androidx.preference:preference-ktx:1.2.1")
    compileOnly(de.robv.android.xposed.api)
    implementation(com.highcapable.yukihookapi.api)
    ksp(com.highcapable.yukihookapi.ksp.xposed)
    implementation(com.github.duanhong169.drawabletoolbox)
    implementation(androidx.core.core.ktx)
    implementation(androidx.appcompat.appcompat)
    implementation(com.google.android.material.material)
    implementation(androidx.constraintlayout.constraintlayout)
    implementation(com.google.code.gson.gson)
    implementation(androidx.preference.preference.ktx)
    implementation(com.hendraanggrian.material.collapsingtoolbarlayout.subtitle)
    testImplementation(junit.junit)
    androidTestImplementation(androidx.test.ext.junit)
    androidTestImplementation(androidx.test.espresso.espresso.core)
}

tasks.register<LintTask>("ktLint") {
    group = "verification"
    source(files("src"))
}

tasks.register<FormatTask>("ktFormat") {
    group = "formatting"
    source(files("src"))
}
