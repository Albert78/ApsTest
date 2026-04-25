plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "de.dh.apstest.plugin.cgm"
    compileSdk = 37

    defaultConfig {
        minSdk = 37
    }
}

dependencies {
    implementation(project(":core-api"))
    implementation(libs.androidx.core.ktx)
}