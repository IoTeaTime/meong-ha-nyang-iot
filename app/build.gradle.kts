plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.iotest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.iotest"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.aws.android.sdk.iot)
    implementation(libs.amazonaws.aws.android.sdk.mobile.client)
    implementation(libs.bcprov.jdk15on)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.runtime.livedata)

    implementation (libs.androidx.lifecycle.runtime.ktx.v261)

    val awsVersion = "2.75.0"
    implementation("com.amazonaws:aws-android-sdk-kinesisvideo:$awsVersion@aar") { isTransitive = true }
    implementation("com.amazonaws:aws-android-sdk-kinesisvideo-signaling:$awsVersion@aar") { isTransitive = true }
    implementation("com.amazonaws:aws-android-sdk-kinesisvideo-webrtcstorage:$awsVersion@aar") { isTransitive = true }
    implementation("com.amazonaws:aws-android-sdk-mobile-client:$awsVersion@aar") { isTransitive = true }

    // ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.1.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.1.1")
    implementation("androidx.media3:media3-ui:1.1.1")

}
