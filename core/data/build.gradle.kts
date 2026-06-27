plugins {
    alias(libs.plugins.android.library)
    id("com.apollographql.apollo").version("5.0.1")
}

apollo {
    service("service") {
        packageName.set("com.dukkan")

        introspection {
            endpointUrl.set("https://mad46-and5.myshopify.com/api/2026-04/graphql.json")
            headers.put("X-Shopify-Storefront-Access-Token",
                providers.gradleProperty("shopifyStorefrontToken").get()
            )
            schemaFile.set(file("src/main/graphql/com/dukkan/schema.json"))
        }
    }

}

android {
    namespace = "com.dukkan.data"
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

}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation("com.apollographql.apollo:apollo-runtime:5.0.1")
}