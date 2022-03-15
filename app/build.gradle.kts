plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("com.apollographql.apollo3").version("3.1.0")
    id("com.mikepenz.aboutlibraries.plugin").version("10.0.0-rc02")
}
android {
    compileSdk = 31
    defaultConfig {
        applicationId = "com.fastaccess.github.revival"
        minSdk = 21
        targetSdk = 31
        versionCode = 468
        versionName = "4.6.8"
        buildConfigField("String", "GITHUB_APP_ID", "\"com.fastaccess.github.revival.debug\"")
        buildConfigField("String", "GITHUB_CLIENT_ID", "\"473e333123519beadd63\"")
        buildConfigField("String", "GITHUB_SECRET", "\"b2d158f949d3615078eaf570ff99eba81cfa1ff9\"")
        buildConfigField("String", "IMGUR_CLIENT_ID", "\"5fced7f255e1dc9\"")
        buildConfigField("String", "IMGUR_SECRET", "\"03025033403196a4b68b48f0738e67ef136ad64f\"")
        buildConfigField("String", "REST_URL", "\"https://api.github.com/\"")
        buildConfigField("String", "IMGUR_URL", "\"https://api.imgur.com/3/\"")
        buildConfigField("String", "GITHUB_STATUS_URL", "\"https://www.githubstatus.com/\"")
        buildConfigField("String", "GITHUB_STATUS_COMPONENTS_PATH", "\"api/v2/components.json\"")
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        getByName("debug") {
            storeFile = file("${rootProject.projectDir}\\debug.keystore")
        }
    }
    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    lint {
        htmlReport = true
        xmlReport = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets {
        getByName("main") {
            res.srcDirs(
                "src/main/res/",
                "src/main/res/layouts/main_layouts",
                "src/main/res/layouts/row_layouts",
                "src/main/res/layouts/other_layouts",
                "src/main/res/translations"
            )
        }
    }
    viewBinding {
        enable = true
    }
}

kapt {
    keepJavacAnnotationProcessors = true
}

apollo {
    packageName.set("com.fastaccess.github")
//    generateKotlinModels.set(false)
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // androidx
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.fragment:fragment-ktx:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.legacy:legacy-preference-v14:1.0.0")
    implementation("androidx.browser:browser:1.4.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.compose.ui:ui:1.1.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.core:core-splashscreen:1.0.0-beta01")

    // thirtyinch
    implementation("com.github.Grandcentrix.ThirtyInch:thirtyinch:v1.0.1")
    implementation("com.github.Grandcentrix.ThirtyInch:thirtyinch-rx2:v1.0.1")
    implementation("com.github.Grandcentrix.ThirtyInch:thirtyinch-kotlin:v1.0.1")
    implementation("com.github.Grandcentrix.ThirtyInch:thirtyinch-kotlin-coroutines:v1.0.1")

    // retrofit2
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // glide
    implementation("com.github.bumptech.glide:glide:4.13.1")

    // ShapedImageView
    implementation("cn.gavinliu:ShapedImageView:0.8.7")
//    implementation("io.woong.shapedimageview:shapedimageview:1.4.3")

    // butterknife
    implementation("com.jakewharton:butterknife:10.2.3")
    kapt("com.jakewharton:butterknife-compiler:10.2.3")

    // bottom-navigation
    implementation("it.sephiroth.android.library.bottomnavigation:bottom-navigation:3.0.0")

    // rx2
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // autodispose
//    implementation("com.uber.autodispose2:autodispose:2.1.1")
//    implementation("com.uber.autodispose2:autodispose-lifecycle:2.1.1")
//    implementation("com.uber.autodispose2:autodispose-android:2.1.1")
//    implementation("com.uber.autodispose2:autodispose-androidx-lifecycle:2.1.1")

    // okhttp3
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.9.3"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // stream
    implementation("com.annimon:stream:1.2.2")

    // Toasty
    implementation("com.github.GrenderG:Toasty:1.5.2")

    // RetainedDateTimePickers
    implementation("com.github.k0shk0sh:RetainedDateTimePickers:1.0.2")

    // material-about-library
    implementation("com.github.daniel-stoneuk:material-about-library:2.1.0")

    // requery
    implementation("io.requery:requery:1.6.0")
    implementation("io.requery:requery-android:1.6.0")
//    kapt("io.requery:requery-processor:1.6.0")

    // about lib
    implementation("com.mikepenz:aboutlibraries-core:10.0.0")
    implementation("com.mikepenz:aboutlibraries:10.0.0")
    implementation("com.mikepenz:aboutlibraries-compose:10.0.0")

    // HtmlSpanner
    implementation("com.github.NightWhistler:HtmlSpanner:0.4")
    // htmlcleaner
    implementation("net.sourceforge.htmlcleaner:htmlcleaner:2.2")


    // commonmark
    implementation("com.atlassian.commonmark:commonmark:0.17.0")
    implementation("com.atlassian.commonmark:commonmark-ext-autolink:0.17.0")
    implementation("com.atlassian.commonmark:commonmark-ext-gfm-strikethrough:0.17.0")
    implementation("com.atlassian.commonmark:commonmark-ext-gfm-tables:0.17.0")
    implementation("com.atlassian.commonmark:commonmark-ext-ins:0.17.0")
    implementation("com.atlassian.commonmark:commonmark-ext-yaml-front-matter:0.17.0")

    // firebase
    implementation("com.google.firebase:firebase-core:20.1.0")
    implementation("com.google.firebase:firebase-messaging:23.0.0")
    implementation("com.google.firebase:firebase-database:20.0.3")
//    implementation("com.firebase:firebase-jobdispatcher:0.8.6")
//    implementation("com.google.android.gms:play-services-base:18.0.1")

    // rx billing
    implementation("com.github.miguelbcr:RxBillingService:0.0.3")

    // kotlin std
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10")

    // jsoup
    implementation("org.jsoup:jsoup:1.14.3")

    // state
    implementation("com.evernote:android-state:1.4.1")
    kapt("com.evernote:android-state-processor:1.4.1")

    // color picker
    implementation("com.github.kristiyanP:colorpicker:v1.1.10")

    // apollo3
    implementation("com.apollographql.apollo3:apollo-runtime:3.1.0")
    implementation("com.apollographql.apollo3:apollo-rx2-support:3.1.0")

    // device name
    implementation("com.jaredrummler:android-device-names:2.1.0")

    // keyboard
    implementation("net.yslibrary.keyboardvisibilityevent:keyboardvisibilityevent:2.1.0")

    // lottie
    implementation("com.airbnb.android:lottie:5.0.3")

    // mmkv
    implementation("com.tencent:mmkv:1.2.12")

    // androidx javax annotation
    implementation("org.glassfish:javax.annotation:10.0-b28")
    implementation("androidx.annotation:annotation:1.3.0")


    // shortbread
    implementation("com.github.matthiasrobbers:shortbread:1.4.0")
//    kapt("com.github.matthiasrobbers:shortbread-compiler:1.4.0")

    // bugly
    implementation("com.tencent.bugly:crashreport:4.0.0")


    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.4.0")
    testImplementation("org.assertj:assertj-core:3.22.0")
    androidTestImplementation("org.mockito:mockito-core:4.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    // 泄漏检测
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.8.1")
}


java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}