plugins {
    id("dukkan.feature")
}

android {
    namespace = "com.dukkan.search"
}

dependencies {
    implementation(project(":core:design_system"))
    implementation(project(":core:domain"))
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.material.icons.core)
}
