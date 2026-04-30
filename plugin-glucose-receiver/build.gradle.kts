plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "de.dh.apstest.plugin.glucose.receiver"
    compileSdk = 37

    defaultConfig {
        minSdk = 35
    }
}

dependencies {
    implementation(project(":core-api"))
    implementation(libs.androidx.core.ktx)
}