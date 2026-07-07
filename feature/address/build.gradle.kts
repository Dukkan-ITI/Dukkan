plugins {
    id("dukkan.feature")
    alias(libs.plugins.secrets)
}

android {
    namespace = "com.dukkan.address"
}

dependencies {
    implementation(project(":core:design_system"))
    implementation(project(":core:domain"))

    implementation(libs.androidx.compose.material.icons.core)

    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
}