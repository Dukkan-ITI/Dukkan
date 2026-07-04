plugins {
    id("dukkan.feature")
}

android {
    namespace = "com.dukkan.categories"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }
    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:design_system"))
    implementation(project(":core:navigation"))
    implementation(project(":core:domain"))

    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.coil.compose)
}
