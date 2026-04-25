plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "de.dh.apstest.core.api"
    compileSdk = 37

    defaultConfig {
        minSdk = 35
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
}