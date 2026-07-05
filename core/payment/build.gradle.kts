plugins {
    id("dukkan.feature")
    id("dukkan.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.dukkan.payment"
    defaultConfig {
        buildConfigField(
            "String",
            "PAYMOB_SECRET_KEY",
            "\"${providers.gradleProperty("paymobClientSecret").getOrElse("")}\""
        )
        buildConfigField(
            "String",
            "PAYMOB_PUBLIC_KEY",
            "\"${providers.gradleProperty("paymobPublicKey").getOrElse("")}\""
        )
        buildConfigField(
            "int",
            "PAYMOB_INTEGRATION_ID",
            "${providers.gradleProperty("paymobIntegrationId").getOrElse("0")}"
        )
    }


    buildFeatures {
        // Required by Paymob native SDK
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:navigation"))
    implementation(project(":core:design_system"))

    // SETUP: Download the Paymob Android SDK .aar from https://docs.paymob.com/docs/android-sdk
    implementation(":paymob-sdk@aar")

    // Networking — Retrofit + OkHttp for backend calls (NOT direct Paymob calls)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Paymob (required since AAR doesn't fetch them)
    api(libs.sdp)
    api(libs.ssp)
    api(libs.androidx.navigation.fragment.ktx)
    api(libs.androidx.navigation.ui.ktx)
    api(libs.koin.android)
    api(libs.timber)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)


}
