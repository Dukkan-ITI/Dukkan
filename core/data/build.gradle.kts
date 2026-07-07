import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.apollo)
    id("dukkan.hilt")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun gradleOrLocalProperty(name: String, defaultValue: String = ""): String =
    providers.gradleProperty(name).orNull
        ?: localProperties.getProperty(name)
        ?: defaultValue

apollo {
    service("service") {
        packageName.set("com.dukkan")
        srcDir(file("src/main/graphql/com/dukkan"))

        introspection {
            endpointUrl.set("https://mad46-and5.myshopify.com/api/2026-04/graphql.json")
            headers.put(
                "X-Shopify-Storefront-Access-Token",
                providers.gradleProperty("shopifyStorefrontToken").get()
            )
            schemaFile.set(file("src/main/graphql/com/dukkan/schema.json"))
        }
    }

    service("admin") {
        packageName.set("com.dukkan.admin")
        srcDir(file("src/main/graphql/admin"))

        introspection {
            endpointUrl.set("https://mad46-and5.myshopify.com/admin/api/2026-04/graphql.json")
            headers.put(
                "X-Shopify-Access-Token",
                providers.gradleProperty("shopifyAdminToken").get()
            )
            schemaFile.set(file("src/main/graphql/com/dukkan/admin/schema.json"))
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
        buildConfigField(
            "String",
            "SHOPIFY_ADMIN_TOKEN",
            "\"${providers.gradleProperty("shopifyAdminToken").getOrElse("")}\""
        )
        buildConfigField(
            "String",
            "LOCATION_IQ_API_KEY",
            "\"${providers.gradleProperty("locationIqApiKey").getOrElse("")}\""
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
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ai_agent"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
}
