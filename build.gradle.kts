// File: build.gradle.kts (Project: Winly)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false // Wajib pakai apply false!
    alias(libs.plugins.kotlin.compose) apply false
}