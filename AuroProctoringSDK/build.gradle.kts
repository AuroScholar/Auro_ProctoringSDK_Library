plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.example.auroproctoringsdk"
    compileSdk = 33

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8" //1.6
    }

    publishing {
        singleVariant("release") {
            // if you don't want sources/javadoc, remove these lines
            withSourcesJar()
            withJavadocJar()
        }
    }
    dexOptions {
        incremental = true
        javaMaxHeapSize = "4g"
        preDexLibraries = true
        dexInProcess = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0") // 1.6.0
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("androidx.activity:activity-compose:1.8.0") //1.7.2

    implementation ("com.google.mlkit:pose-detection-common:17.0.0")
    implementation ("com.google.mlkit:pose-detection:17.0.0")
//    implementation("com.google.mlkit:object-detection:17.0.0")
    implementation("com.google.mlkit:face-detection:16.1.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation ("com.google.mlkit:image-labeling:17.0.7")
    implementation ("com.google.mlkit:image-labeling-custom:17.0.1")


    // CameraX core library using the camera2 implementation
   /* val camerax_version = "1.0.0-alpha06"*//*"1.4.0-alpha01"*//*
    val camerax_version2 = "1.4.0-alpha01"
    // The following line is optional, as the core library is included indirectly by camera-camera2
    implementation("androidx.camera:camera-core:${camerax_version2}")
    implementation("androidx.camera:camera-camera2:${camerax_version2}")
    // If you want to additionally use the CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:${camerax_version2}")
    // If you want to additionally use the CameraX VideoCapture library
    implementation("androidx.camera:camera-video:${camerax_version2}")
    // If you want to additionally use the CameraX View class
    implementation("androidx.camera:camera-view:${camerax_version2}")
    // If you want to additionally add CameraX ML Kit Vision Integration
    implementation("androidx.camera:camera-mlkit-vision:${camerax_version2}")
    // If you want to additionally use the CameraX Extensions library
    implementation("androidx.camera:camera-extensions:${camerax_version2}")*/


//    implementation("com.google.android.gms:play-services-vision-common:19.1.3")
//    implementation("com.google.android.gms:play-services-vision:20.1.3")
//    implementation("com.google.android.gms:play-services-mlkit-face-detection:17.1.0")


/*    implementation ("org.tensorflow:tensorflow-lite-task-vision:0.4.0")
    // Import the GPU delegate plugin Library for GPU inference
    implementation ("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.4.0")
    implementation ("org.tensorflow:tensorflow-lite-gpu:2.9.0")*/

}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.AuroScholar"
                artifactId = "Auro_Proctoring_Library"
                version = "0.0.4"
            }
        }
    }
}
