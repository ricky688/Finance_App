plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

val phoneBuild = rootProject.file("app/build.gradle.kts").readText()
android {
    namespace = "com.example.vibefinance.wear"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.ricky688.vibefinance"
        minSdk = 36
        targetSdk = 37
        versionCode = Regex("versionCode = (\\d+)").find(phoneBuild)!!.groupValues[1].toInt()
        versionName = Regex("versionName = \"([^\"]+)\"").find(phoneBuild)!!.groupValues[1]
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("phone") {
            val phone = project(":app").extensions.getByType<com.android.build.api.dsl.ApplicationExtension>()
                .signingConfigs.getByName("release")
            storeFile = phone.storeFile
            storePassword = phone.storePassword
            keyAlias = phone.keyAlias
            keyPassword = phone.keyPassword
        }
    }
    buildTypes {
        debug { signingConfig = signingConfigs.getByName("phone") }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("phone")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests.isIncludeAndroidResources = false }
}
kotlin { jvmToolchain(17) }
tasks.withType<Test>().configureEach {
    javaLauncher.set(javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(21) })
}
dependencies {
    implementation(project(":wear-contract"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("androidx.wear.compose:compose-material3:1.7.1")
    implementation("androidx.wear.compose:compose-foundation:1.7.1")
    implementation("androidx.wear.compose:compose-ui-tooling:1.7.1")
    implementation("androidx.wear.tiles:tiles:1.6.2")
    implementation("androidx.wear.protolayout:protolayout:1.4.2")
    implementation("androidx.concurrent:concurrent-futures:1.3.0")
    implementation("com.google.android.gms:play-services-wearable:20.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.16.1")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    // API 37 removed InputManager.getInstance(), still used by stable Espresso 3.7.
    androidTestImplementation("androidx.test.espresso:espresso-core:3.8.0-alpha03")
    androidTestImplementation("androidx.test:runner:1.8.0-alpha03")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
