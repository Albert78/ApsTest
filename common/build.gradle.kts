plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "de.dh.raaps.core.api"
    compileSdk = 37

    defaultConfig {
        minSdk = 35
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
}