plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "de.dh.raaps.plugin.glucose"
    compileSdk = 37

    defaultConfig {
        minSdk = 35
    }
}

dependencies {
    implementation(project(":common"))
    implementation(libs.androidx.core.ktx)
}