plugins {
    id("dukkan.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.dukkan.payment"
    defaultConfig {
        buildConfigField(
            "String",
            "PAYMENT_BACKEND_URL",
            "\"${providers.gradleProperty("paymentBackendUrl").getOrElse("http://10.0.2.2:8080/api/v1/")}\""
        )
        buildConfigField(
            "String",
            "PAYMOB_PUBLIC_KEY",
            "\"${providers.gradleProperty("paymobPublicKey").getOrElse("missing_key")}\""
        )
        buildConfigField(
            "String",
            "PAYMOB_CLIENT_SECRET",
            "\"${providers.gradleProperty("paymobClientSecret").getOrElse("missing_secret")}\""
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
