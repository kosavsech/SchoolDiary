plugins {
	id("com.android.application")
	id("kotlin-android")
	id("kotlin-kapt")
	id("kotlin-parcelize")
	id("com.google.dagger.hilt.android")
	id("org.jetbrains.kotlin.android")
	id("org.jetbrains.kotlin.plugin.serialization")
	id("com.google.devtools.ksp")
}

android {
	namespace = "com.kxsv.schooldiary"
	
	defaultConfig {
		applicationId = "com.kxsv.schooldiary"
		minSdk = 28
		compileSdk = 34
		//noinspection OldTargetApi
		targetSdk = 33
		versionCode = 5
		versionName = "0.1.2:alpha"
		
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		vectorDrawables {
			useSupportLibrary = true
		}
	}
	
	buildTypes {
		release {
			isMinifyEnabled = true
			isDebuggable = false
			proguardFiles(
				getDefaultProguardFile("proguard-android.txt"),
				"proguard-rules.pro"
			)
		}
	}
	buildFeatures {
		compose = true
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
	}
	composeOptions {
		kotlinCompilerExtensionVersion = "1.4.7"
	}
	packagingOptions {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
}

dependencies {
	implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
	implementation("androidx.startup:startup-runtime:1.1.1")
	implementation("androidx.core:core-ktx:1.10.1")
	implementation("androidx.appcompat:appcompat:1.6.1")
	implementation("androidx.activity:activity-compose:1.7.2")
	implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
	
	implementation(project(":dialogs-core"))
	implementation(project(":dialogs-datetime"))
	implementation(project(":ychart-mod"))
	implementation(project(":segmentedprogressbar"))
	implementation(project(":appupdater-core"))
	
	// Compose dependencies
	val composeVersion = "1.5.0"
	implementation("androidx.compose.ui:ui:$composeVersion")
	implementation("androidx.compose.material:material:$composeVersion")
	debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
	implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
	androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")
	debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.0")
	implementation("androidx.navigation:navigation-compose:2.7.0")
	implementation("androidx.compose.material3:material3:1.1.1")
	implementation("androidx.compose.material:material-icons-extended:1.5.0")
	
	// Lifecycle dependencies
	implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
	implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
	implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
	
	// Coroutines
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
	
	//Dagger - Hilt
	implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
	implementation("com.google.dagger:hilt-android:2.47")
	implementation("androidx.hilt:hilt-work:1.0.0")
	kapt("androidx.hilt:hilt-compiler:1.0.0")
	kapt("com.google.dagger:hilt-compiler:2.47")
	
	// Room
	val roomVersion = "2.5.2"
	implementation("androidx.room:room-runtime:$roomVersion")
	ksp("androidx.room:room-compiler:$roomVersion")
	
	// Kotlin Extensions and Coroutines support for Room
	implementation("androidx.room:room-ktx:2.5.2")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
	
	// https://mvnrepository.com/artifact/com.google.code.gson/gson
	implementation("com.google.code.gson:gson:2.10.1")
	
	// https://github.com/aclassen/ComposeReorderable/releases/latest
//	implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")
	
	// Jsoup
	implementation("org.jsoup:jsoup:1.16.1")
	
	// Proto DataStore
	implementation("androidx.datastore:datastore:1.1.0-alpha04")
	implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
	
	// The compose calendar library
	implementation("com.kizitonwose.calendar:compose:2.4.0-beta01")
	
	// WorkManager
	val workManagerVersion = "2.9.0-alpha02"
	implementation("androidx.work:work-runtime:$workManagerVersion")
	
	// compose-destinations
	implementation("io.github.raamcosta.compose-destinations:core:1.9.51")
	ksp("io.github.raamcosta.compose-destinations:ksp:1.9.51")
	
	testImplementation("junit:junit:4.13.2")
	androidTestImplementation("androidx.test.ext:junit:1.1.5")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
kapt {
	correctErrorTypes = true
}
ksp {
	arg("room.schemaLocation", "$projectDir/schemas")
}