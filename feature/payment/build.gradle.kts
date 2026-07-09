plugins {
    id("dukkan.feature")
    id("dukkan.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.apollo)
}

apollo {
    service("admin") {
        packageName.set("com.dukkan.payment.admin")

        introspection {
            endpointUrl.set("https://mad46-and5.myshopify.com/admin/api/2024-10/graphql.json")
            headers.put(
                "X-Shopify-Access-Token",
                providers.gradleProperty("shopifyAdminToken").get()
            )
            schemaFile.set(file("src/main/graphql/com/dukkan/payment/admin/schema.json"))
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

android {
    namespace = "com.dukkan.payment"
    defaultConfig {
        buildConfigField(
            "String",
            "PAYMOB_SECRET_KEY",
            "\"${providers.gradleProperty("paymobClientSecret").getOrElse("")}\""
        )
        buildConfigField(
            "String",
            "PAYMOB_PUBLIC_KEY",
            "\"${providers.gradleProperty("paymobPublicKey").getOrElse("")}\""
        )
        buildConfigField(
            "int",
            "PAYMOB_INTEGRATION_ID",
            "${providers.gradleProperty("paymobIntegrationId").getOrElse("0")}"
        )
        buildConfigField(
            "String",
            "SHOPIFY_ADMIN_TOKEN",
            "\"${providers.gradleProperty("shopifyAdminToken").getOrElse("")}\""
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:navigation"))
    implementation(project(":core:design_system"))
    implementation(project(":core:address_ui"))

    implementation(libs.play.services.maps)

    implementation(":paymob-sdk@aar")

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    api(libs.sdp)
    api(libs.ssp)
    api(libs.androidx.navigation.fragment.ktx)
    api(libs.androidx.navigation.ui.ktx)
    api(libs.koin.android)
    api(libs.timber)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.apollo.runtime)

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)


}
