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
        buildConfigField(
            "String",
            "OLLAMA_BASE_URL",
            "\"${gradleOrLocalProperty("ollamaBaseUrl", "https://ollama.com/api")}\""
        )
        buildConfigField(
            "String",
            "OLLAMA_MODEL",
            "\"${gradleOrLocalProperty("ollamaModel", "qwen3")}\""
        )
        buildConfigField(
            "String",
            "OLLAMA_API_KEY",
            "\"${gradleOrLocalProperty("ollamaApiKey")}\""
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
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
}
