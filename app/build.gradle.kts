import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.logging.LogFactory.release

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    alias(libs.plugins.crashlytics)

}

android {
    namespace = "com.geinzz.geinzwork"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.geinzz.geinzwork"
        minSdk = 24
        targetSdk = 34
        versionCode = 27
        versionName = "1.14.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
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
        viewBinding = true

    }
    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/NOTICE")
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.leanback)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.recyclerview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.compose.material:material:1.5.4")
    implementation("com.hbb20:ccp:2.7.0")
    implementation("com.google.firebase:firebase-firestore")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.android.material:material:1.11.0")
    implementation("me.relex:circleindicator:2.1.6")
    implementation("org.imaginativeworld.whynotimagecarousel:whynotimagecarousel:2.1.0")
    implementation("id.zelory:compressor:3.0.1")
    implementation("com.airbnb.android:lottie:6.4.1")
    implementation("com.google.firebase:firebase-dynamic-links-ktx:21.1.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("io.getstream:photoview:1.0.2")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.github.MikeOrtiz:TouchImageView:3.6")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.firebase:firebase-crashlytics:2.9.5")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0-RC")
    implementation("org.quanqi:android-holo-graph:0.1.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("org.quanqi:android-holo-graph:0.1.0")
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("com.google.firebase:firebase-ai")
    implementation("androidx.biometric:biometric:1.1.0")

}