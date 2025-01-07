import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream
import java.util.Properties;

var properties = Properties()
properties.load(FileInputStream("local.properties"))

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.giftbox"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.giftbox"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        addManifestPlaceholders(mapOf("NAVER_MAP_CLIENT_ID" to gradleLocalProperties(rootDir, providers).getProperty("NAVER_MAP_CLIENT_ID")))
        buildConfigField("String", "KAKAO_REST_API_KEY", properties.getProperty("KAKAO_REST_API_KEY"))
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
        isCoreLibraryDesugaringEnabled = true   // LocalDateTime 이 Api 26 이하 지원을 위해 추가

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        // 뷰 바인딩 활성화
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // retrofit2
    implementation(libs.converter.gson)
    implementation(libs.retrofit)

    // location
    implementation(libs.play.services.location)

    // naver map SDK
    implementation(libs.map.sdk)

    // fragment
    implementation(libs.androidx.fragment.ktx)

    // viewbinding
    implementation(libs.androidx.ui.viewbinding)

    // appcompat
    implementation(libs.androidx.appcompat)

    // constraintlayout
    implementation(libs.androidx.constraintlayout)

    // LocalDateTime Api 26 lower
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    // gson
    implementation("com.google.code.gson:gson:2.10.1")

    // coil
    implementation(libs.coil.compose)

    // hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // credentials
    implementation("androidx.credentials:credentials:1.2.2>")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")

    // firebase
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.1")
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}