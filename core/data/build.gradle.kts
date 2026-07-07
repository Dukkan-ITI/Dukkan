plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.apollo)
    id("dukkan.hilt")
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

        buildConfigField(
            "String",
            "SHOPIFY_STOREFRONT_TOKEN",
            "\"${providers.gradleProperty("shopifyStorefrontToken").getOrElse("")}\""
        )
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }

}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation(libs.apollo.runtime)
}

// Room
dependencies {
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.coroutines.core)
}

// DataStore
dependencies {
    implementation(libs.androidx.datastore.preferences)
}

//firebase dependencies
dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.kotlinx.serialization.json)
}
