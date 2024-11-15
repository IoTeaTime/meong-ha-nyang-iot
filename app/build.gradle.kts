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
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "AWS_ACCESS_KEY", properties["AWS_ACCESS_KEY"].toString())
        buildConfigField("String", "AWS_PRIVATE_KEY", properties["AWS_PRIVATE_KEY"].toString())
        buildConfigField("String", "AWS_REGION", properties["AWS_REGION"].toString())
        buildConfigField("String", "MQTT_END_POINT", properties["MQTT_END_POINT"].toString())
        buildConfigField("String", "AWS_KEYSTORE_PW", properties["AWS_KEYSTORE_PW"].toString())
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
    implementation (libs.amazonaws.aws.android.sdk.mobile.client)
    implementation(libs.bcprov.jdk15on)
}