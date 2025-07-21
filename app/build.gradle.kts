
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    alias(libs.plugins.crashlytics)

}

android {
    namespace = "com.geinzz.geinzwork"
    compileSdk = 35


    defaultConfig {
        applicationId = "com.geinzz.geinzwork"
        minSdk = 24
        targetSdk = 35
        versionCode = 28
        versionName = "1.15.1"

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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.glide.v4160)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.material)
    implementation(libs.ccp)
    implementation("com.google.firebase:firebase-firestore")
    implementation(libs.circleimageview)
    implementation(libs.shimmer)
    implementation("com.google.firebase:firebase-messaging")
    implementation(libs.material)
    implementation(libs.circleindicator)
    implementation(libs.whynotimagecarousel)
    implementation(libs.compressor)
    implementation(libs.lottie)
    implementation("com.google.firebase:firebase-dynamic-links-ktx:21.1.0")
    implementation(libs.play.services.location)
    implementation(libs.core)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0") {
        exclude(group = "com.android.support", module = "support-v4")
    }
    implementation(libs.photoview)
    implementation(libs.picasso)
    implementation(libs.touchimageview)
    implementation(libs.androidx.core.splashscreen)
    implementation("com.google.firebase:firebase-crashlytics:2.9.5")
    implementation("com.google.firebase:firebase-config")
    implementation(libs.google.auth.library.oauth2.http)
    implementation(libs.okhttp)

    implementation("org.quanqi:android-holo-graph:0.1.0") {
        exclude(group = "com.android.support", module = "support-v4")
    }
    implementation(libs.mpandroidchart)
    implementation(libs.androidx.activity.ktx)
    implementation("com.google.firebase:firebase-ai")
    implementation(libs.androidx.biometric)
    implementation(libs.chrisbanes.photoview)
    implementation(libs.ucrop)
//courutinas
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)
    //live data
    implementation(libs.androidx.lifecycle.livedata.ktx)
}