plugins {
	id("com.android.library")
	id("org.jetbrains.kotlin.android")
}

android {
	namespace = "com.pouyaheydari.appupdater.core"
	compileSdk = 33
	
	defaultConfig {
		minSdk = 27
		
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}
	
	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
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

dependencies {
	
	implementation("androidx.appcompat:appcompat:1.6.1")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
	
	testImplementation("junit:junit:4.13.2")
	androidTestImplementation("androidx.test.ext:junit:1.1.5")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}