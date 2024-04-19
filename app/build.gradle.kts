plugins {
    alias(libs.plugins.androidApplication)
//    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.sqlite)
    kotlin("android")
}

android {
    namespace = "fr.vinetos.tranquille"
    compileSdk = 34

    defaultConfig {
        applicationId = "fr.vinetos.tranquille"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "0.0.1"
        multiDexEnabled = true

        javaCompileOptions {
            annotationProcessorOptions {
                argument("eventBusIndex", "fr.vinetos.tranquille.EventBusIndex")
            }
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

    lint {
        abortOnError = false
        lintConfig = file("lint.xml")
    }
    buildFeatures {
        buildConfig = true
    }

    applicationVariants.all {
        val variant = this
        variant.resValue("string", "app_id", variant.applicationId)
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("fr.vinetos.tranquille.data")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.recyclerview.selection)
    implementation(libs.androidx.paging)
    implementation(libs.androidx.multidex)
    implementation(libs.google.material)
    implementation(libs.slf4j)
    implementation(libs.conscrypt)
    implementation(libs.okhttp)
    implementation(libs.lib.phone.number.info)
    implementation(libs.commons.csv)
    implementation(libs.sqlite.driver)
    implementation(libs.sqlite.coroutines)
    implementation(libs.eventbus)

    // todo add kotlin coriutines dependency
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    annotationProcessor(libs.eventbus.annotation.processor)

    runtimeOnly(libs.logback)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}