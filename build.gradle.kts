// MIGRATION: Root build.gradle.kts — equivalent to the root package.json scripts section.
// Applies plugins to subprojects without configuring them here.
plugins {
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.composeMultiplatform).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.kotlinSerialization).apply(false)
    alias(libs.plugins.sqldelight).apply(false)
}
