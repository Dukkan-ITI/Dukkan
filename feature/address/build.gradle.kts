plugins {
    id("dukkan.feature")
}

android {
    namespace = "com.dukkan.address"
}

dependencies {
    implementation(project(":core:design_system"))
    implementation(project(":core:domain"))

    implementation(libs.androidx.compose.material.icons.core)
}