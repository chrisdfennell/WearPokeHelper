plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // alias(libs.plugins.ksp) // Uncomment if you enable KSP (see Moshi codegen below)
}

android {
    namespace = "com.fennell.wearpokehelper"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fennell.wearpokehelper"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
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

    // Compose must be explicitly enabled
    buildFeatures {
        compose = true
    }

    // Compose compiler extension; align with your Compose libs
    composeOptions {
        kotlinCompilerExtensionVersion = "1.7.3"
    }

    // Java/Kotlin toolchains
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    kotlin {
        jvmToolchain(17)
    }

    // Packaging rules to avoid META-INF conflicts
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- Your existing, catalog-driven dependencies ---
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)       // Expecting this to be androidx.wear.compose:compose-material
    implementation(libs.compose.foundation)     // Expecting this to be androidx.wear.compose:compose-foundation
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)

    // --- Add Material3 for TextField / TextFieldDefaults ---
    implementation("androidx.compose.material3:material3:1.3.0")

    // --- Lifecycle / ViewModel (Compose friendly) ---
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // --- Networking stack ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // --- Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // --- Moshi ---
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    // If you want Moshi codegen, enable KSP plugin above and uncomment this:
    // ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")

    // --- Wear Input helpers (rotary, etc.) ---
    implementation("androidx.wear:wear-input:1.2.0")

    // --- Tooling / Testing ---
    implementation(libs.wear.tooling.preview)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}