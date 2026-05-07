plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    id("jacoco")
}

kotlin {
    jvmToolchain(17)
}

val voyagerBackendBaseUrl: String =
    (project.findProperty("VOYAGER_BACKEND_BASE_URL") as? String)?.trim()?.let { if (it.endsWith("/")) it else "$it/" }
        ?: "http://10.0.2.2:8080/api/v1/"

val voyagerAiBaseUrl: String =
    (project.findProperty("VOYAGER_AI_BASE_URL") as? String)?.trim()?.let { if (it.endsWith("/")) it else "$it/" }
        ?: "http://10.0.2.2:8000/api/v1/"

android {
    namespace = "com.voyager.tourism"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.voyager.tourism"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // Spring Boot core (context-path /api/v1). Sobrescribe en gradle.properties: VOYAGER_BACKEND_BASE_URL=https://tu-api.com/api/v1/
        buildConfigField("String", "BACKEND_BASE_URL", "\"${voyagerBackendBaseUrl.replace("\"", "\\\"")}\"")
        // FastAPI AI (prefijo /api/v1). Sobrescribe: VOYAGER_AI_BASE_URL=https://tu-ia.com/api/v1/
        buildConfigField("String", "AI_SERVICE_BASE_URL", "\"${voyagerAiBaseUrl.replace("\"", "\\\"")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            enableUnitTestCoverage = true
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Compose runtime lint + UAST en headless/CI: NPE en varios detectores al analizar AiAssistantScreen.kt.
    lint {
        disable += setOf(
            "MutableCollectionMutableState",
            "AutoboxingStateCreation",
        )
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    ksp("com.google.dagger:hilt-compiler:2.51")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Image Loading (placeholder for future use)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.robolectric:robolectric:4.13")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51")
    kspAndroidTest("com.google.dagger:hilt-compiler:2.51")

    debugImplementation(composeBom)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Allow references to generated code
ksp {
    arg("room.incremental", "true")
}

jacoco {
    toolVersion = "0.8.12"
}

// Informe JaCoCo unificado (excluye UI Compose, Hilt y código generado para acercar el % a lógica de negocio)
val jacocoExcluded = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*_HiltModules*",
    "**/Hilt_*",
    "**/*_Factory*",
    "**/*_MembersInjector*",
    "**/dagger/**",
    "**/hilt/**",
    "**/presentation/ui/**",
    "**/presentation/MainActivity*",
    "**/presentation/navigation/**",
    "**/di/**",
    "**/*Composable*",
)

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Genera informe HTML/XML de cobertura tras testDebugUnitTest"

    val debugTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        exclude(jacocoExcluded)
    }
    val javaTree = fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes")) {
        exclude(jacocoExcluded)
    }
    classDirectories.setFrom(files(debugTree, javaTree))

    val mainSrc = "${project.projectDir}/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))

    executionData.setFrom(
        fileTree(layout.buildDirectory.asFile.get()) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec",
            )
        },
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/html"))
    }

    dependsOn("testDebugUnitTest")
}
