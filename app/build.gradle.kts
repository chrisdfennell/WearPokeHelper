plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // alias(libs.plugins.ksp) // <-- This should now work after editing the project-level file
}

android {
    namespace = "com.fennell.wearpokehelper"
    compileSdk = 36 // Use 36 to align with dependencies if possible

    defaultConfig {
        applicationId = "com.fennell.wearpokehelper"
        minSdk = 30 // Required minSdk for latest Wear Compose
        targetSdk = 36 // Target latest SDK
        versionCode = 1
        versionName = "1.0"

        // Vector drawables needed for some Compose icons
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false // Keep false for now
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    // Use Java 17 toolchain
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    // Apply Java 17 toolchain for Kotlin compilation
    kotlin {
        jvmToolchain(17)
    }
    // Removed duplicate kotlin block

    // Packaging options needed for some libraries (like OkHttp/Coroutines)
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- Original Dependencies ---
    implementation(libs.play.services.wearable) // Still needed
    implementation(platform(libs.compose.bom)) // BOM for version alignment
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material) // Wear Compose Material
    implementation(libs.compose.foundation) // Wear Compose Foundation
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)

    // --- Added Dependencies (from wearpokecounter) ---
    // Lifecycle & ViewModel for MVVM
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4") // Use specific version
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4") // ViewModel Compose integration

    // Retrofit & OkHttp for Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // For network logging

    // Coroutines for asynchronous operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Moshi for JSON parsing (with Kotlin support)
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    // ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1") // This line will now work

    // Wear Input (might be needed for rotary input or other specific interactions, good to have)
    implementation("androidx.wear:wear-input:1.2.0")

    // --- Tooling/Testing (Originals) ---
    implementation(libs.wear.tooling.preview) // Wear specific preview tools
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}