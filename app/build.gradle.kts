plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin)

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        compose = true

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
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.text)
    implementation(libs.play.services.maps)
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
    implementation(libs.constaints.layout)
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
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //coil
    implementation(libs.coil.compose)
    implementation(libs.coil3.coil.network.okhttp)
    //lotti file
    implementation(libs.lottie.compose)
    //dependecia compouse live data
    implementation(libs.androidx.runtime.livedata)

    implementation(libs.androidx.animation)
    implementation(libs.accompanist.navigation.animation)

    implementation(libs.androidx.foundation)


    //navigation componets
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation("com.google.accompanist:accompanist-pager:0.30.1")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.30.1")


}