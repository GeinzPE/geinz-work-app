import org.gradle.kotlin.dsl.implementation
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.kotlinCompose)
//    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin)
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
}
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

val mapboxToken = localProperties.getProperty("MAPBOX_ACCESS_TOKEN")
    ?: throw GradleException("MAPBOX_ACCESS_TOKEN not found in local.properties")
android {
    namespace = "com.geinzz.geinzwork"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.geinzz.geinzwork"
        minSdk = 24
        targetSdk = 35
        versionCode = 54
        versionName = "1.40.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "MAPBOX_ACCESS_TOKEN",
            "\"$mapboxToken\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true

    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/LICENSE.md",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/NOTICE.md",
                "META-INF/versions/**",
                "META-INF/OSGI-INF/**",
                "META-INF/MANIFEST.MF"
            )
        }
    }

}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation(libs.androidx.compose.foundation)
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

    implementation(libs.play.services.maps)

    implementation(libs.volley)
    implementation(libs.androidx.core.i18n)
    implementation(libs.androidx.compose.material3)
    implementation(libs.identity.jvm)

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
    implementation(libs.coil.core)
    implementation(libs.coil3.coil.network.okhttp)
    // Coil 3.x para Compose
//    implementation("io.coil-kt:coil-compose:3.3.0")
//    implementation("io.coil-kt:coil:3.3.0") // si necesitas ImageLoader o requests
//    implementation("io.coil-kt:coil-okhttp3:3.3.0") // soporte con OkHttp

    //lotti file
    implementation(libs.lottie.compose)
    //dependecia compouse live data
    implementation(libs.androidx.runtime.livedata)

    implementation(libs.androidx.animation)
    implementation(libs.accompanist.navigation.animation)


    implementation(libs.androidx.compose.foundation.layout)
    //navigation componets
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.maps.compose)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)


    implementation("io.github.joelkanyi:komposecountrycodepicker:1.4.4")

    implementation("com.google.android.gms:play-services-auth:21.4.0")

    implementation("androidx.datastore:datastore-preferences:1.1.7")

    implementation(libs.androidx.material.icons.extended)

//    implementation("com.github.mukeshsolanki:compose-country-code-picker:2.0.2")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    implementation("androidx.palette:palette:1.0.0")

    // Algolia Kotlin API Client v2
    implementation("com.algolia:algoliasearch-client-kotlin:2.1.2")

    // Motor HTTP Ktor para Android
    implementation("io.ktor:ktor-client-okhttp:2.0.1")
    implementation("com.firebase:geofire-android:3.2.0")

    implementation("com.valentinilk.shimmer:compose-shimmer:1.3.3")

    implementation("io.github.panpf.zoomimage:zoomimage-compose:1.4.0")

    implementation("com.google.code.gson:gson:2.10.1")


    implementation("io.github.dautovicharis:charts-android:2.0.1")
    implementation("com.mapbox.maps:android-ndk27:11.22.0")
    implementation("com.mapbox.extension:maps-compose-ndk27:11.22.0")
    implementation(libs.androidx.foundation)
//    implementation("com.github.SmartToolFactory:Compose-Cropper:1.2.3")
    implementation("androidx.media3:media3-exoplayer:1.9.3")
    implementation("androidx.media3:media3-exoplayer-dash:1.9.3")
    implementation("androidx.media3:media3-ui:1.9.3")
    implementation("androidx.media3:media3-ui-compose:1.9.3")
    implementation("com.google.firebase:firebase-functions-ktx:20.2.0")

    implementation("androidx.browser:browser:1.8.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

}