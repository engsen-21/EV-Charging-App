plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin)
    id("kotlin-kapt")
}

android {
    namespace = "smartcharge.master"
    compileSdk = 34

    defaultConfig {
        applicationId = "smartcharge.master"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.database)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Import the BoM for the Firebase platform to handle the version for all the firebase file
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-analytics")
    //Firebase authentication
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    //Firebase Messaging Cloud
    implementation ("com.google.firebase:firebase-messaging:24.0.0")
    //Google Maps SDK
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    // Google Location Services
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // Google Place
    implementation ("com.google.android.libraries.places:places:3.5.0")

    // Retrofit for network operations
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson converter for Retrofit to parse JSON
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    //HTTP
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")

    //Profile Circle Image View
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    //RecyclerView
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    // Material
    implementation ("com.google.android.material:material:1.8.0")

    //Glide for image loading and caching library
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    kapt ("com.github.bumptech.glide:compiler:4.15.1")
}