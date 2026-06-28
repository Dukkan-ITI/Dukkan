plugins {
    alias(libs.plugins.android.library)
    id("com.apollographql.apollo").version("5.0.1")
    id("com.google.devtools.ksp")
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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


    //firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    val room_version = "2.8.4"
    // Room
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
}