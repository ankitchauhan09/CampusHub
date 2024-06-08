    plugins {
        alias(libs.plugins.androidApplication)
        alias(libs.plugins.jetbrainsKotlinAndroid)
        id("com.google.gms.google-services")
        id("kotlin-kapt")
        id("com.google.dagger.hilt.android")
    }

    android {
        namespace = "com.example.campushub"
        compileSdk = 34

        defaultConfig {
            applicationId = "com.example.campushub"
            minSdk = 26
            targetSdk = 34
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
        buildFeatures{
            viewBinding = true
            dataBinding = true
        }
        packagingOptions {
            exclude("META-INF/DEPENDENCIES")
            exclude("META-INF/LICENSE")
            exclude("META-INF/LICENSE.txt")
            exclude("META-INF/LICENSE.md")
            exclude("META-INF/license.txt")
            exclude("META-INF/NOTICE")
            exclude("META-INF/NOTICE.md")
            exclude("META-INF/NOTICE.txt")
            exclude("META-INF/notice.txt")
            exclude("META-INF/ASL2.0")
            exclude("META-INF/*.kotlin_module")
        }
    }

    dependencies {

        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.firebase.auth)
        implementation(libs.play.services.auth)
        implementation(libs.androidx.navigation.runtime.ktx)
        implementation(libs.firebase.database)
        implementation(libs.androidx.navigation.fragment.ktx)
        implementation(libs.androidx.navigation.ui.ktx)
        implementation(libs.firebase.storage)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        implementation("com.github.ybq:Android-SpinKit:1.4.0")
        implementation("com.airbnb.android:lottie:6.4.0")
        implementation("com.github.Spikeysanju:MotionToast:1.4")
        implementation("com.google.firebase:firebase-bom:32.8.0")
        implementation("com.facebook.android:facebook-login:latest.release")
        implementation("com.etebarian:meow-bottom-navigation:1.2.0")
        implementation("com.google.dagger:hilt-android:2.51.1")
        kapt("com.google.dagger:hilt-compiler:2.51.1")
        implementation("com.squareup.picasso:picasso:+")
        implementation("com.github.1902shubh:SendMail:1.0.0")
        implementation("commons-codec:commons-codec:1.11")
        implementation("com.github.raheemadamboev:image-radio-button-android:1.0.7")
        implementation("io.agora.rtc:full-sdk:4.3.0")
        implementation("de.hdodenhof:circleimageview:3.1.0")
        implementation("com.github.bumptech.glide:glide:4.16.0")
        implementation("com.github.denzcoskun:ImageSlideshow:0.1.2")
        implementation("com.smparkworld.parkdatetimepicker:parkdatetimepicker:1.1.0")
    }

    kapt {
        correctErrorTypes = true
    }