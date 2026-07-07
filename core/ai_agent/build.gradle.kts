import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
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

android {
    namespace = "com.dukkan.ai_agent"
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
            "OLLAMA_BASE_URL",
            "\"${gradleOrLocalProperty("ollamaBaseUrl", "https://ollama.com/api")}\""
        )
        buildConfigField(
            "String",
            "OLLAMA_MODEL",
            "\"${gradleOrLocalProperty("ollamaModel", "gpt-oss:120b-cloud")}\""
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    debugImplementation(libs.firebase.appcheck.debug)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
