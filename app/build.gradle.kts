plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.schulmanager"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.schulmanager"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    // Android Core
    implementation(libs.core.ktx)
// ViewPager2 und Fragments
    implementation(libs.viewpager2)
    implementation(libs.fragment)
// RecyclerView
// CardView
    implementation(libs.cardview)
// Gson für JSON-Serialisierung
    implementation(libs.gson)
// Lifecycle (optional für zukünftige Erweiterungen)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}