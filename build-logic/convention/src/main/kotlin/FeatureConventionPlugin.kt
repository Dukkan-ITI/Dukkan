import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.library")
            apply("org.jetbrains.kotlin.plugin.compose")
            apply("dukkan.hilt")
        }

        extensions.configure<LibraryExtension> {
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

            buildFeatures {
                compose = true
            }
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

        dependencies {
            "implementation"(libs.findLibrary("androidx-appcompat").get())
            "implementation"(libs.findLibrary("androidx-core-ktx").get())
            "implementation"(libs.findLibrary("material").get())

            // Compose
            val composeBom = platform(libs.findLibrary("androidx-compose-bom").get())
            "implementation"(composeBom)
            "implementation"(libs.findLibrary("androidx-compose-ui").get())
            "implementation"(libs.findLibrary("androidx-compose-ui-graphics").get())
            "implementation"(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            "implementation"(libs.findLibrary("androidx-compose-material3").get())
            "implementation"(libs.findLibrary("androidx-compose-foundation").get())
            "implementation"(libs.findLibrary("androidx-compose-material-icons-extended").get())

            // Navigation
            "implementation"(libs.findLibrary("navigation-compose").get())
            "implementation"(libs.findLibrary("hilt-navigation-compose").get())

            // Lifecycle
            "implementation"(libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
            "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
            "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())

            // Tooling
            "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())

            // Unit tests
            "testImplementation"(libs.findLibrary("junit").get())

            // Instrumented tests
            "androidTestImplementation"(composeBom)
            "androidTestImplementation"(libs.findLibrary("androidx-compose-ui-test-junit4").get())
            "androidTestImplementation"(libs.findLibrary("androidx-espresso-core").get())
            "androidTestImplementation"(libs.findLibrary("androidx-junit").get())
        }
    }
}
