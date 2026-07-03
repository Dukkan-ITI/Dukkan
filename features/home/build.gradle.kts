plugins {
    id("dukkan.feature")
}

android {
    namespace = "com.dukkan.home"
}

dependencies {
    implementation(project(":core:design_system"))
    implementation(project(":core:domain"))
    implementation(project(":feature:ads"))


    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.coil.compose)
}
