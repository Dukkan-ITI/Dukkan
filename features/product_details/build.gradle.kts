plugins {
    id("dukkan.feature")
}

android {
    namespace = "com.dukkan.product_details"
}

dependencies {
    implementation(project(":core:design_system"))
    implementation(project(":core:domain"))
    implementation(project(":core:navigation"))

    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.material.icons.extended)
}
