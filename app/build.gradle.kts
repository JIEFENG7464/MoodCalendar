plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

import java.util.Properties

android {
    namespace = "com.moodcalendar.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.moodcalendar.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1.0"
    }

    val keystorePropsFile = rootProject.file("../keystore.properties")
    val hasSigning = keystorePropsFile.exists() || rootProject.file("keystore.properties").exists()
    var signingMap: Map<String, String>? = null
    if (hasSigning) {
        val props = Properties()
        val f = if (keystorePropsFile.exists()) keystorePropsFile else rootProject.file("keystore.properties")
        f.inputStream().use { props.load(it) }
        signingMap = mapOf(
            "storeFile" to props.getProperty("storeFile"),
            "storePassword" to props.getProperty("storePassword"),
            "keyAlias" to props.getProperty("keyAlias"),
            "keyPassword" to props.getProperty("keyPassword")
        )
    }

    signingConfigs {
        if (signingMap != null) {
            create("release") {
                storeFile = rootProject.file(signingMap["storeFile"]!!)
                storePassword = signingMap["storePassword"]!!
                keyAlias = signingMap["keyAlias"]!!
                keyPassword = signingMap["keyPassword"]!!
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (signingMap != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.caverock:androidsvg-aar:1.4")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
