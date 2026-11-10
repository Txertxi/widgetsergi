plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "cat.sergibonet.widgets"
    compileSdk = 34

    defaultConfig {
        applicationId = "cat.sergibonet.widgets"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"
    }

    // Dues versions de la mateixa app. Cada una genera el seu APK:
    //   app-sergi-debug.apk  (la teva; mateix applicationId, s'instal·la a sobre de l'actual)
    //   app-lluis-debug.apk  (per al Lluís; applicationId diferent, no es barreja amb la teva)
    // Les diferències (festius i textos) són a app/src/lluis/: allò que hi ha allà
    // substitueix el que hi ha a app/src/main per a la versió del Lluís.
    flavorDimensions += "persona"
    productFlavors {
        create("sergi") {
            dimension = "persona"
        }
        create("lluis") {
            dimension = "persona"
            applicationIdSuffix = ".lluis"
        }
    }

    // Clau de signatura fixa (inclosa al repo): així cada APK nou
    // s'instal·la a sobre de l'anterior sense desinstal·lar.
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
