

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.ihsan.memorieswithimagevideo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ihsan.memorieswithimagevideo"
        minSdk = 24
        targetSdk = 33
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
    externalNativeBuild {

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //glide
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    implementation("jp.wasabeef.transformers:glide:1.0.6")
    // Use the GPU Filters
    implementation("jp.wasabeef.transformers:glide-gpu:1.0.6")

    implementation("jp.wasabeef:glide-transformations:4.3.0")
    // If you want to use the GPU Filters
    implementation("jp.co.cyberagent.android:gpuimage:2.1.0")

    //kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.2")

//    implementation("fileTree(dir: 'libs', include: ['*.jar'])")
//    implementation("com.writingminds:FFmpegAndroid:0.3.2")
//
//    implementation("com.theartofdev.edmodo:android-image-cropper:2.8.0")

//    implementation("nl.bravobit:android-ffmpeg:1.1.7")

//    implementation ("com.github.SimformSolutionsPvtLtd:SSffmpegVideoOperation:1.0.8")

    implementation ("com.arthenica:mobile-ffmpeg-full:4.4")
    implementation ("org.florescu.android.rangeseekbar:rangeseekbar-library:0.3.0")


}