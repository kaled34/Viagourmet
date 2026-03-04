plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.viagourmet"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.viagourmet"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
        // Core Android
        implementation("androidx.core:core-ktx:1.12.0")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
        implementation("androidx.activity:activity-compose:1.8.0")

        // Compose UI
        implementation(platform("androidx.compose:compose-bom:2024.02.00"))
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.ui:ui-graphics")
        implementation("androidx.compose.ui:ui-tooling-preview")
        implementation("androidx.compose.material3:material3")

        // Navigation
        implementation("androidx.navigation:navigation-compose:2.7.6")

        // ViewModel
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

        // Retrofit (para API)
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

        // Dagger Hilt (inyección de dependencias)
        implementation("com.google.dagger:hilt-android:2.48")
        implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

        // Corrutinas
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

        // Coil (para cargar imágenes)
        implementation("io.coil-kt:coil-compose:2.5.0")

        // CameraX (para escáner QR)
        implementation("androidx.camera:camera-camera2:1.3.0")
        implementation("androidx.camera:camera-lifecycle:1.3.0")
        implementation("androidx.camera:camera-view:1.3.0")

        // MLKit (para detección de QR)
        implementation("com.google.mlkit:barcode-scanning:17.2.0")
    }