plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a")) // 正确的写法，使用 addAll 添加 ABI 过滤器
            // 或者使用 abiFilters += listOf("armeabi-v7a", "arm64-v8a")
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

    sourceSets.getByName("main") {
        jniLibs.srcDirs("libs")
    }
    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    //implementation(files("libs\\Msc.jar"))
    //implementation(files("..\\libs\\Msc.jar"))
    //implementation(libs.media3.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
}