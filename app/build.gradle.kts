plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.uttarooque73.netguard"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.uttarooque73.netguard"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.1.3"
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("NETGUARD_KEYSTORE_PATH")
            val keystorePassword = System.getenv("NETGUARD_KEYSTORE_PASSWORD")
            val keyAlias = System.getenv("NETGUARD_KEY_ALIAS")

            if (!keystorePath.isNullOrBlank() &&
                !keystorePassword.isNullOrBlank() &&
                !keyAlias.isNullOrBlank()
            ) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keystorePassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            val releaseKeystorePath = System.getenv("NETGUARD_KEYSTORE_PATH")
            val releaseKeystorePassword = System.getenv("NETGUARD_KEYSTORE_PASSWORD")
            val releaseKeyAlias = System.getenv("NETGUARD_KEY_ALIAS")
            signingConfig = if (!releaseKeystorePath.isNullOrBlank() &&
                !releaseKeystorePassword.isNullOrBlank() &&
                !releaseKeyAlias.isNullOrBlank()
            ) {
                signingConfigs.getByName("release")
            } else {
                null
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    lint {
        // Disable known incompatible third-party lint detectors with the
        // AGP 8.7.3 lint/UAST toolchain. Other lint checks remain enabled.
        disable += setOf(
            "NullSafeMutableLiveData",
            "FrequentlyChangingValue",
            "RememberInComposition"
        )
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.fragment:fragment-ktx:1.9.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.biometric:biometric:1.1.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
