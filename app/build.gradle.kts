plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.translator"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.translator"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Fragment and Navigation
    implementation("androidx.fragment:fragment:1.6.2")
    implementation("androidx.navigation:navigation-fragment:2.7.6")
    implementation("androidx.navigation:navigation-ui:2.7.6")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime:2.7.0")

    // Room database
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // ML Kit - Updated versions with multiple text recognizers
    implementation("com.google.mlkit:translate:17.0.2")
    implementation("com.google.mlkit:language-id:17.0.4")

    // Text Recognition for different scripts
    implementation("com.google.mlkit:text-recognition:16.0.0") // Latin script
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0") // Chinese script
    implementation("com.google.mlkit:text-recognition-devanagari:16.0.0") // Devanagari script
    implementation("com.google.mlkit:text-recognition-japanese:16.0.0") // Japanese script
    implementation("com.google.mlkit:text-recognition-korean:16.0.0") // Korean script

    // Network connectivity
    implementation("androidx.core:core:1.12.0")

    // Google Play Services (Required for ML Kit)
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-chinese:16.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-devanagari:16.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-japanese:16.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-korean:16.0.0")
    implementation("com.google.android.gms:play-services-mlkit-language-id:17.0.0")

    // Gson for JSON parsing (if needed)
    implementation("com.google.code.gson:gson:2.10.1")
}